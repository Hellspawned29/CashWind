package com.cashwind.app.model

data class Budget(
    val id: Int,
    val userId: Int,
    val name: String,
    val amount: Double,
    val period: String,
    val category: String? = null,
    val createdAt: String? = null
)

data class CreateBudgetRequest(
    val name: String,
    val amount: Double,
    val period: String,
    val category: String? = null
)

data class BudgetProgress(
    val id: Int,
    val name: String,
    val amount: Double,
    val spent: Double,
    val remaining: Double,
    val percentUsed: Double
)

data class Goal(
    val id: Int,
    val userId: Int,
    val name: String,
    val type: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val targetDate: String,
    val monthlyContribution: Double? = null,
    val priority: String? = null,
    val status: String? = null,
    val accountId: Int? = null,
    val category: String? = null,
    val notes: String? = null,
    val account: AccountSummary? = null
)

data class CreateGoalRequest(
    val name: String,
    val type: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val targetDate: String,
    val monthlyContribution: Double? = null,
    val priority: String? = null,
    val status: String? = "active",
    val accountId: Int? = null,
    val category: String? = null,
    val notes: String? = null
)

data class GoalProgress(
    val id: Int,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val percentComplete: Double,
    val remainingAmount: Double,
    val monthsRemaining: Int? = null,
    val projectedCompletion: String? = null
)

data class NotificationSettings(
    val phone: String? = null,
    val emailNotifications: Boolean = false,
    val smsNotifications: Boolean = false,
    val reminderDays: Int = 7
)
