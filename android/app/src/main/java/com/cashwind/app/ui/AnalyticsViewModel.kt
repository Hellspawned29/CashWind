package com.cashwind.app.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cashwind.app.database.CashwindDatabase
import com.cashwind.app.model.AnalyticsSummary
import com.cashwind.app.model.BudgetPerformance
import com.cashwind.app.model.MonthlyTrend
import com.cashwind.app.model.SpendingByCategory
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AnalyticsViewModel(private val db: CashwindDatabase) : ViewModel() {
    private val transactionDao = db.transactionDao()
    private val budgetDao = db.budgetDao()
    private val billDao = db.billDao()
    private val goalDao = db.goalDao()
    private val accountDao = db.accountDao()
    private val userId = 1 // Default user

    private val _spendingByCategory = MutableLiveData<List<SpendingByCategory>>()
    val spendingByCategory: LiveData<List<SpendingByCategory>> = _spendingByCategory

    private val _monthlyTrends = MutableLiveData<List<MonthlyTrend>>()
    val monthlyTrends: LiveData<List<MonthlyTrend>> = _monthlyTrends

    private val _budgetPerformance = MutableLiveData<List<BudgetPerformance>>()
    val budgetPerformance: LiveData<List<BudgetPerformance>> = _budgetPerformance


    private val _analyticsSummary = MutableLiveData<AnalyticsSummary>()
    val analyticsSummary: LiveData<AnalyticsSummary> = _analyticsSummary

    init {
        loadAnalytics()
    }

    fun loadAnalytics() {
        viewModelScope.launch {
            calculateSpendingByCategory()
            calculateMonthlyTrends()
            calculateBudgetPerformance()
            calculateAnalyticsSummary()
        }
    }

    private suspend fun calculateSpendingByCategory() {
        val transactions = transactionDao.getAllTransactions(userId).first()
        val expenses = transactions.filter { it.type == "expense" }
        
        val categoryTotals = expenses.groupBy { it.category ?: "Uncategorized" }
            .mapValues { it.value.sumOf { tx -> tx.amount } }
        
        val totalExpenses = categoryTotals.values.sum()
        
        val spendingList = categoryTotals.map { (category, amount) ->
            SpendingByCategory(
                category = category,
                amount = amount,
                percentage = if (totalExpenses > 0) (amount / totalExpenses * 100).toFloat() else 0f
            )
        }.sortedByDescending { it.amount }
        
        _spendingByCategory.postValue(spendingList)
    }

    private suspend fun calculateMonthlyTrends() {
        val transactions = transactionDao.getAllTransactions(userId).first()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val monthFormat = SimpleDateFormat("MMM yyyy", Locale.US)
        
        val monthlyData = transactions.groupBy { tx ->
            try {
                val date = sdf.parse(tx.date)
                if (date != null) monthFormat.format(date) else "Unknown"
            } catch (e: Exception) {
                "Unknown"
            }
        }.mapValues { (_, txList) ->
            val income = txList.filter { it.type == "income" }.sumOf { it.amount }
            val expenses = txList.filter { it.type == "expense" }.sumOf { it.amount }
            MonthlyTrend(
                month = "",
                income = income,
                expenses = expenses,
                net = income - expenses
            )
        }
        
        val trends = monthlyData.map { (month, trend) ->
            trend.copy(month = month)
        }.sortedBy { it.month }
        
        _monthlyTrends.postValue(trends.takeLast(6)) // Last 6 months
    }

    private suspend fun calculateBudgetPerformance() {
        val budgets = budgetDao.getAllBudgets(userId).first()
        val transactions = transactionDao.getAllTransactions(userId).first()
        
        val performance = budgets.map { budget ->
            val spent = transactions.filter { 
                it.type == "expense" && it.category == (budget.category ?: "")
            }.sumOf { it.amount }
            
            val percentageUsed = if (budget.amount > 0) {
                (spent / budget.amount * 100).toFloat()
            } else 0f
            
            BudgetPerformance(
                category = budget.category ?: "",
                budgetLimit = budget.amount,
                actualSpent = spent,
                percentageUsed = percentageUsed,
                isOverBudget = spent > budget.amount
            )
        }.sortedByDescending { it.percentageUsed }
        
        _budgetPerformance.postValue(performance)
    }


    private suspend fun calculateAnalyticsSummary() {
        val transactions = transactionDao.getAllTransactions(userId).first()
        val bills = billDao.getAllBills(userId).first()
        val goals = goalDao.getAllGoals(userId).first()
        
        val totalIncome = transactions.filter { it.type == "income" }.sumOf { it.amount }
        val totalExpenses = transactions.filter { it.type == "expense" }.sumOf { it.amount }
        val netCashFlow = totalIncome - totalExpenses
        val savingsRate = if (totalIncome > 0) ((netCashFlow / totalIncome) * 100).toFloat() else 0f
        
        val categoryExpenses = transactions.filter { it.type == "expense" }
            .groupBy { it.category.ifBlank { "Uncategorized" } }
            .mapValues { entry -> entry.value.sumOf { tx -> tx.amount } }
        val largestCategory = categoryExpenses.maxByOrNull { it.value }?.key ?: "None"
        
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val today = Calendar.getInstance()
        val next30Days = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 30) }
        
        val upcomingBills = bills.filter { bill ->
            !bill.isPaid && try {
                val dueDate = sdf.parse(bill.dueDate)
                if (dueDate != null) {
                    val dueCal = Calendar.getInstance().apply { time = dueDate }
                    dueCal.after(today) && dueCal.before(next30Days)
                } else false
            } catch (e: Exception) {
                false
            }
        }
        
        val activeGoals = goals.filter { it.status != "completed" }
        val goalsOnTrack = activeGoals.count { 
            it.currentAmount >= it.targetAmount * 0.5 // At least 50% funded
        }
        
        val summary = AnalyticsSummary(
            totalIncome = totalIncome,
            totalExpenses = totalExpenses,
            netCashFlow = netCashFlow,
            savingsRate = savingsRate,
            largestExpenseCategory = largestCategory,
            upcomingBillsCount = upcomingBills.size,
            upcomingBillsTotal = upcomingBills.sumOf { it.amount },
            activeGoalsCount = activeGoals.size,
            goalsOnTrack = goalsOnTrack
        )
        
        _analyticsSummary.postValue(summary)
    }
}
