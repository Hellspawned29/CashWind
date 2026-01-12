package com.cashwind.app.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.cashwind.app.database.CashwindDatabase
import com.cashwind.app.database.entity.BillEntity
import com.cashwind.app.database.entity.GoalEntity
import com.cashwind.app.database.entity.PaycheckSettingsEntity
import com.cashwind.app.database.entity.TransactionEntity
import com.cashwind.app.model.Bill
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PaycheckViewModel(private val db: CashwindDatabase) : ViewModel() {
    private val billDao = db.billDao()
    private val goalDao = db.goalDao()
    private val paycheckSettingsDao = db.paycheckSettingsDao()
    private val transactionDao = db.transactionDao()
    private val userId = 1 // Default user (no auth)

    val allBills: LiveData<List<Bill>> = billDao.getAllBillsLive().map { billEntities ->
        billEntities.map { it.toBill() }
    }

    val allGoals: LiveData<List<GoalEntity>> = goalDao.getAllGoalsLive(userId)

    // Load recurring income transactions (paychecks)
    val recurringIncome: LiveData<List<TransactionEntity>> = transactionDao.getRecurringIncomeLive(userId)

    // Load persisted paycheck settings
    val paycheckSettings: LiveData<PaycheckSettingsEntity?> = paycheckSettingsDao.getPaycheckSettingsLive(userId)

    // Calculate paycheck amount from recurring income transactions
    val paycheckAmount: LiveData<Double> = recurringIncome.map { transactions ->
        transactions.firstOrNull()?.amount ?: 0.0
    }

    private val paycheckFrequencyDays = MutableLiveData(14) // Default: biweekly
    private val nextPayDateStr = MutableLiveData<String?>(null)

    init {
        // Load settings when view model is created (frequency and next pay date only)
        viewModelScope.launch {
            val settings = paycheckSettingsDao.getPaycheckSettings(userId)
            if (settings != null) {
                // paycheckAmount now comes from recurring income transactions
                paycheckFrequencyDays.value = settings.frequencyDays
                nextPayDateStr.value = settings.nextPayDate
            }
        }
    }

    // Calculate total monthly bills divided by number of paychecks
    val monthlyBillsPerPaycheck: LiveData<Double> = MediatorLiveData<Double>().apply {
        value = 0.0
        
        addSource(allBills) { bills ->
            value = calculateMonthlyBillsPerPaycheck(bills, paycheckFrequencyDays.value ?: 14)
        }
        
        addSource(paycheckFrequencyDays) { frequencyDays ->
            value = calculateMonthlyBillsPerPaycheck(allBills.value ?: emptyList(), frequencyDays)
        }
    }

    // Legacy: bills within next pay period only
    val upcomingBillsTotal: LiveData<Double> = MediatorLiveData<Double>().apply {
        value = 0.0
        
        addSource(allBills) { bills ->
            value = calculateUpcomingBills(bills, paycheckFrequencyDays.value ?: 14)
        }
        
        addSource(paycheckFrequencyDays) { frequencyDays ->
            value = calculateUpcomingBills(allBills.value ?: emptyList(), frequencyDays)
        }

        addSource(nextPayDateStr) {
            value = calculateUpcomingBills(allBills.value ?: emptyList(), paycheckFrequencyDays.value ?: 14)
        }
    }

    val goalsAllocation: LiveData<Double> = MediatorLiveData<Double>().apply {
        value = 0.0
        
        addSource(allGoals) { goals ->
            value = calculateGoalsAllocation(goals, paycheckFrequencyDays.value ?: 14, nextPayDateStr.value)
        }
        
        addSource(paycheckFrequencyDays) { frequencyDays ->
            value = calculateGoalsAllocation(allGoals.value ?: emptyList(), frequencyDays, nextPayDateStr.value)
        }

        addSource(nextPayDateStr) {
            value = calculateGoalsAllocation(allGoals.value ?: emptyList(), paycheckFrequencyDays.value ?: 14, nextPayDateStr.value)
        }
    }

    val totalAllocation: LiveData<Double> = MediatorLiveData<Double>().apply {
        value = 0.0
        
        addSource(upcomingBillsTotal) { billsTotal ->
            value = (billsTotal ?: 0.0) + (goalsAllocation.value ?: 0.0)
        }
        
        addSource(goalsAllocation) { goalsTotal ->
            value = (upcomingBillsTotal.value ?: 0.0) + (goalsTotal ?: 0.0)
        }
    }

    val allocationPerPaycheck: LiveData<Double> = totalAllocation

    val remainingAfterBills: LiveData<Double> = MediatorLiveData<Double>().apply {
        value = 0.0
        
        addSource(paycheckAmount) { amount ->
            value = (amount ?: 0.0) - (totalAllocation.value ?: 0.0)
        }
        
        addSource(totalAllocation) { total ->
            value = (paycheckAmount.value ?: 0.0) - (total ?: 0.0)
        }
    }

    // Past due bills monitoring
    val pastDueBills: LiveData<List<Bill>> = allBills.map { bills ->
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val today = sdf.format(Calendar.getInstance().time)
        bills.filter { bill ->
            !bill.isPaid && bill.dueDate < today
        }
    }

    val pastDueBillsTotal: LiveData<Double> = pastDueBills.map { bills ->
        bills.sumOf { it.amount + it.pastDueAmount }
    }

    val pastDueBillsCount: LiveData<Int> = pastDueBills.map { it.size }

    // Bills with past due balances (separate from overdue bills)
    val billsWithPastDueBalance: LiveData<List<Bill>> = allBills.map { bills ->
        bills.filter { bill ->
            bill.hasPastDue && bill.pastDueAmount > 0.0
        }
    }

    val pastDueBalanceTotal: LiveData<Double> = billsWithPastDueBalance.map { bills ->
        bills.sumOf { it.pastDueAmount }
    }

    val pastDueBalanceCount: LiveData<Int> = billsWithPastDueBalance.map { it.size }

    // No longer needed - paycheck amount comes from recurring transactions
    // fun setPaycheckAmount(amount: Double) {
    //     paycheckAmount.value = amount
    // }

    fun setPaycheckFrequencyDays(days: Int) {
        paycheckFrequencyDays.value = days
    }

    fun setNextPayDate(dateStr: String) {
        nextPayDateStr.value = dateStr
    }

    fun savePaycheckSettings() {
        viewModelScope.launch {
            val settings = PaycheckSettingsEntity(
                userId = userId,
                amount = paycheckAmount.value ?: 0.0,
                frequencyDays = paycheckFrequencyDays.value ?: 14,
                nextPayDate = nextPayDateStr.value,
                updatedAt = System.currentTimeMillis()
            )
            paycheckSettingsDao.savePaycheckSettings(settings)
        }
    }

    private fun calculateUpcomingBills(bills: List<Bill>, frequencyDays: Int): Double {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        val today = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val start = Calendar.getInstance().apply { time = today.time }

        nextPayDateStr.value?.let { dateStr ->
            try {
                val parsed = sdf.parse(dateStr)
                if (parsed != null) {
                    start.time = parsed
                    start.set(Calendar.HOUR_OF_DAY, 0)
                    start.set(Calendar.MINUTE, 0)
                    start.set(Calendar.SECOND, 0)
                    start.set(Calendar.MILLISECOND, 0)
                    // Clamp to today so stale past dates do not hide bills
                    if (start.before(today)) {
                        start.time = today.time
                    }
                }
            } catch (_: Exception) {
                // fallback to today
            }
        }

        val lookAhead = (start.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, frequencyDays)
        }

        var total = 0.0
        bills.forEach { bill ->
            if (!bill.isPaid) {
                try {
                    val due = sdf.parse(bill.dueDate)
                    if (due != null) {
                        val dueDate = Calendar.getInstance().apply { 
                            time = due
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        
                        // Bill is upcoming if: due date is on/after start, AND on/before lookAhead
                        if (!dueDate.before(start) && !dueDate.after(lookAhead)) {
                            total += bill.amount
                        }
                    }
                } catch (_: Exception) {
                    // Ignore parse errors
                }
            }
        }
        return total
    }

    private fun calculateMonthlyBillsPerPaycheck(bills: List<Bill>, frequencyDays: Int): Double {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Get current month bounds
        val monthStart = (today.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val monthEnd = (monthStart.clone() as Calendar).apply {
            add(Calendar.MONTH, 1)
            add(Calendar.DAY_OF_MONTH, -1)
        }

        // Calculate total bills due this month (unpaid only)
        var monthlyTotal = 0.0
        bills.forEach { bill ->
            if (!bill.isPaid) {
                try {
                    val due = sdf.parse(bill.dueDate)
                    if (due != null) {
                        val dueDate = Calendar.getInstance().apply {
                            time = due
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        
                        // Bill is in current month if due date is between month start and end
                        if (!dueDate.before(monthStart) && !dueDate.after(monthEnd)) {
                            monthlyTotal += bill.amount
                        }
                    }
                } catch (_: Exception) {
                    // Ignore parse errors
                }
            }
        }

        // Calculate number of paychecks in this month based on frequency
        val paychecksThisMonth = when (frequencyDays) {
            7 -> 4.0    // Weekly = ~4 paychecks/month
            14 -> 2.0   // Biweekly = ~2 paychecks/month
            26 -> 2.0   // Semi-monthly = 2 paychecks/month
            30 -> 1.0   // Monthly = 1 paycheck/month
            else -> 2.0 // Default to biweekly
        }

        return if (paychecksThisMonth > 0) monthlyTotal / paychecksThisMonth else 0.0
    }

    private fun calculateGoalsAllocation(goals: List<GoalEntity>, frequencyDays: Int, nextPayDateStr: String?): Double {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val today = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val start = Calendar.getInstance().apply { time = today.time }
        nextPayDateStr?.let { dateStr ->
            try {
                val parsed = sdf.parse(dateStr)
                if (parsed != null) {
                    start.time = parsed
                    start.set(Calendar.HOUR_OF_DAY, 0)
                    start.set(Calendar.MINUTE, 0)
                    start.set(Calendar.SECOND, 0)
                    start.set(Calendar.MILLISECOND, 0)
                    if (start.before(today)) {
                        start.time = today.time
                    }
                }
            } catch (_: Exception) {}
        }

        // Calculate paychecks per month (assuming 30 days)
        val paychecksPerMonth = 30.0 / frequencyDays

        var total = 0.0
        goals.forEach { goal ->
            // Skip completed goals
            if (goal.status == "completed") return@forEach

            val remaining = goal.targetAmount - goal.currentAmount
            if (remaining <= 0) return@forEach

            // Use monthly contribution if set, otherwise calculate based on target date
            val allocationThisPaycheck = if (goal.monthlyContribution != null && goal.monthlyContribution > 0) {
                // Use the monthly contribution divided by paychecks per month
                goal.monthlyContribution / paychecksPerMonth
            } else {
                // Calculate based on target date
                try {
                    val targetDate = sdf.parse(goal.targetDate)
                    if (targetDate != null) {
                        val target = Calendar.getInstance().apply {
                            time = targetDate
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }

                        if (target.after(start)) {
                            val daysUntilTarget = ((target.timeInMillis - start.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
                            val paychecksUntilTarget = Math.max(1, daysUntilTarget / frequencyDays)
                            remaining / paychecksUntilTarget
                        } else {
                            // Target date passed, allocate all remaining
                            remaining
                        }
                    } else {
                        0.0
                    }
                } catch (_: Exception) {
                    0.0
                }
            }

            total += allocationThisPaycheck
        }
        return total
    }

    private fun BillEntity.toBill(): Bill = Bill(
        id = id,
        userId = userId,
        name = name,
        amount = amount,
        dueDate = dueDate,
        isPaid = isPaid,
        lastPaidAt = lastPaidAt,
        category = category,
        recurring = recurring,
        frequency = frequency,
        notes = notes,
        webLink = webLink,
        hasPastDue = hasPastDue,
        pastDueAmount = pastDueAmount,
        createdAt = createdAt
    )

    fun allocateToPastDueBalance(billId: Int, amount: Double) {
        viewModelScope.launch {
            try {
                val bill = billDao.getBillById(billId)
                if (bill != null && bill.hasPastDue) {
                    // Update the bill's past due amount
                    val updatedPastDueAmount = (bill.pastDueAmount - amount).coerceAtLeast(0.0)
                    val updatedBill = bill.copy(pastDueAmount = updatedPastDueAmount)
                    billDao.updateBill(updatedBill)
                }
            } catch (e: Exception) {
                // Log error
            }
        }
    }
}
