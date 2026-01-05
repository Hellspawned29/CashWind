package com.cashwind.app.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cashwind.app.database.CashwindDatabase
import com.cashwind.app.database.entity.BudgetEntity
import com.cashwind.app.database.entity.TransactionEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class BudgetWithSpent(
    val budget: BudgetEntity,
    val spent: Double,
    val remaining: Double,
    val percentUsed: Int
)

class BudgetViewModel(private val db: CashwindDatabase) : ViewModel() {
    private val budgetDao = db.budgetDao()
    private val transactionDao = db.transactionDao()
    private val userId = 1 // Default user (no auth)

    val budgets: LiveData<List<BudgetEntity>> = budgetDao.getAllBudgetsLive(userId)

    val budgetsWithSpent: LiveData<List<BudgetWithSpent>> = MediatorLiveData<List<BudgetWithSpent>>().apply {
        value = emptyList()
        addSource(budgets) { budgets ->
            viewModelScope.launch {
                val result = budgets.map { budget ->
                    val spent = calculateSpentForCategory(budget.category ?: "General", budget.period ?: "monthly")
                    val remaining = budget.amount - spent
                    val percentUsed = if (budget.amount > 0) ((spent / budget.amount) * 100).toInt() else 0
                    BudgetWithSpent(budget, spent, remaining, percentUsed)
                }
                value = result
            }
        }
    }

    fun addBudget(
        name: String,
        amount: Double,
        period: String, // "weekly", "monthly", "yearly"
        category: String
    ) {
        viewModelScope.launch {
            val newId = (System.currentTimeMillis() / 1000).toInt()
            val budget = BudgetEntity(
                id = newId,
                userId = userId,
                name = name,
                amount = amount,
                period = period,
                category = category,
                createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Calendar.getInstance().time)
            )
            budgetDao.insertBudget(budget)
        }
    }

    fun deleteBudget(budget: BudgetEntity) {
        viewModelScope.launch {
            budgetDao.deleteBudget(budget)
        }
    }

    fun updateBudget(budget: BudgetEntity) {
        viewModelScope.launch {
            budgetDao.updateBudget(budget)
        }
    }

    private suspend fun calculateSpentForCategory(category: String, period: String): Double {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val now = Calendar.getInstance()
        val startDate = Calendar.getInstance().apply {
            when (period.lowercase()) {
                "weekly" -> add(Calendar.DAY_OF_YEAR, -7)
                "monthly" -> add(Calendar.MONTH, -1)
                "yearly" -> add(Calendar.YEAR, -1)
                else -> add(Calendar.MONTH, -1)
            }
        }

        // Return 0 for now; ideally this would query transactions properly
        // Full implementation would require coroutine-safe transaction queries
        return 0.0
    }
}
