package com.cashwind.app.model

data class SpendingByCategory(
    val category: String,
    val amount: Double,
    val percentage: Float
)

data class MonthlyTrend(
    val month: String,
    val income: Double,
    val expenses: Double,
    val net: Double
)

data class BudgetPerformance(
    val category: String,
    val budgetLimit: Double,
    val actualSpent: Double,
    val percentageUsed: Float,
    val isOverBudget: Boolean
)

// Use existing AccountSummary and GoalProgress models in Financials.kt and Goals.kt

data class AnalyticsSummary(
    val totalIncome: Double,
    val totalExpenses: Double,
    val netCashFlow: Double,
    val savingsRate: Float,
    val largestExpenseCategory: String,
    val upcomingBillsCount: Int,
    val upcomingBillsTotal: Double,
    val activeGoalsCount: Int,
    val goalsOnTrack: Int
)
