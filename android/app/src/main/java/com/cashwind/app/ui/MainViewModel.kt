package com.cashwind.app.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.cashwind.app.database.CashwindDatabase
import com.cashwind.app.database.entity.BillEntity
import com.cashwind.app.database.entity.TransactionEntity
import com.cashwind.app.model.Bill
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainViewModel(private val database: CashwindDatabase) : ViewModel() {
    private val billDao = database.billDao()
    private val transactionDao = database.transactionDao()

    private val bills: LiveData<List<Bill>> = billDao.getAllBillsLive().map { billEntities ->
        billEntities.map { it.toBill() }
    }

    private val sortOption = MutableLiveData(SortOption.DUE_DATE)
    private val statusFilter = MutableLiveData(StatusFilter.ALL)

    val sortedBills: LiveData<List<Bill>> = MediatorLiveData<List<Bill>>().apply {
        fun update() {
            val currentBills = bills.value ?: emptyList()
            val currentSort = sortOption.value ?: SortOption.DUE_DATE
            val filtered = filter(currentBills, statusFilter.value ?: StatusFilter.ALL)
            value = sort(filtered, currentSort)
        }
        addSource(bills) { update() }
        addSource(sortOption) { update() }
        addSource(statusFilter) { update() }
    }

    val totals: LiveData<Totals> = bills.map { list ->
        val now = Calendar.getInstance()
        val weekAhead = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 7) }
        val monthEnd = (now.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, now.getActualMaximum(Calendar.DAY_OF_MONTH)) }
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        var total = 0
        var paid = 0
        var unpaid = 0
        var dueThisWeek = 0
        var dueThisMonth = 0

        var totalAmount = 0.0
        var paidAmount = 0.0
        var unpaidAmount = 0.0
        var dueThisWeekAmount = 0.0
        var dueThisMonthAmount = 0.0

        list.forEach { bill ->
            val billTotal = bill.amount + bill.pastDueAmount
            
            total += 1
            totalAmount += billTotal
            if (bill.isPaid) paid++ else unpaid++
            if (bill.isPaid) paidAmount += billTotal else unpaidAmount += billTotal

            try {
                val due = sdf.parse(bill.dueDate)
                if (due != null) {
                    val cal = Calendar.getInstance().apply { time = due }
                    if (cal >= now && cal <= weekAhead) {
                        dueThisWeek++
                        dueThisWeekAmount += billTotal
                    }
                    if (cal.get(Calendar.MONTH) == now.get(Calendar.MONTH) && cal.get(Calendar.YEAR) == now.get(Calendar.YEAR)) {
                        dueThisMonth++
                        dueThisMonthAmount += billTotal
                    }
                }
            } catch (_: Exception) {
                // Ignore parse errors
            }
        }

        Totals(
            total = total,
            paid = paid,
            unpaid = unpaid,
            dueThisWeek = dueThisWeek,
            dueThisMonth = dueThisMonth,
            totalAmount = totalAmount,
            paidAmount = paidAmount,
            unpaidAmount = unpaidAmount,
            dueThisWeekAmount = dueThisWeekAmount,
            dueThisMonthAmount = dueThisMonthAmount
        )
    }

    fun loadBills() {
        viewModelScope.launch(Dispatchers.IO) {
            // Bills are loaded automatically via LiveData from billDao.getAllBillsLive()
        }
    }

    fun updateSort(option: SortOption) {
        sortOption.value = option
    }

    fun updateStatusFilter(filter: StatusFilter) {
        statusFilter.value = filter
    }

    fun togglePaid(bill: Bill) {
        viewModelScope.launch(Dispatchers.IO) {
            val billEntity = bill.toEntity()
            val newIsPaid = !bill.isPaid
            val nowStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Calendar.getInstance().time)
            val updatedBill = billEntity.copy(
                isPaid = newIsPaid,
                lastPaidAt = if (newIsPaid) nowStr else billEntity.lastPaidAt
            )
            billDao.updateBill(updatedBill)
            
            // Create transaction when bill is marked as paid
            if (newIsPaid && billEntity.accountId != null) {
                val transaction = TransactionEntity(
                    id = (System.currentTimeMillis() / 1000).toInt(),
                    userId = 1,
                    amount = bill.amount,
                    type = "expense",
                    category = bill.category ?: "Bill",
                    description = bill.name,
                    date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().time),
                    accountId = billEntity.accountId,
                    isRecurring = false,
                    frequency = null,
                    createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Calendar.getInstance().time)
                )
                transactionDao.insertTransaction(transaction)
            }
        }
    }

    fun deleteBill(bill: Bill) {
        viewModelScope.launch(Dispatchers.IO) {
            billDao.deleteBill(bill.toEntity())
        }
    }

    private fun BillEntity.toBill(): Bill {
        return Bill(
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
            accountId = accountId,
            hasPastDue = hasPastDue,
            pastDueAmount = pastDueAmount,
            createdAt = createdAt
        )
    }

    private fun Bill.toEntity(): BillEntity {
        return BillEntity(
            id = id,
            userId = if (userId == 0) 1 else userId,
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
            accountId = accountId,
            createdAt = createdAt
        )
    }

    private fun sort(list: List<Bill>, option: SortOption): List<Bill> {
        return when (option) {
            SortOption.DUE_DATE -> list.sortedWith(compareBy<Bill> { it.dueDate }.thenBy { it.name })
            SortOption.AMOUNT_DESC -> list.sortedByDescending { it.amount }
            SortOption.NAME -> list.sortedBy { it.name.lowercase(Locale.getDefault()) }
        }
    }

    private fun filter(list: List<Bill>, status: StatusFilter): List<Bill> {
        return when (status) {
            StatusFilter.ALL -> list
            StatusFilter.PAID -> list.filter { it.isPaid }
            StatusFilter.UNPAID -> list.filter { !it.isPaid }
        }
    }

    enum class SortOption { DUE_DATE, AMOUNT_DESC, NAME }

    enum class StatusFilter { ALL, PAID, UNPAID }

    data class Totals(
        val total: Int,
        val paid: Int,
        val unpaid: Int,
        val dueThisWeek: Int,
        val dueThisMonth: Int,
        val totalAmount: Double,
        val paidAmount: Double,
        val unpaidAmount: Double,
        val dueThisWeekAmount: Double,
        val dueThisMonthAmount: Double
    )
}
