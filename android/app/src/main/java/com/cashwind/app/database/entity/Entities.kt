package com.cashwind.app.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: Int,
    val email: String,
    val name: String,
    val syncedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "bills")
data class BillEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val name: String,
    val amount: Double,
    val dueDate: String,
    val isPaid: Boolean = false,
    val lastPaidAt: String? = null,
    val category: String? = null,
    val recurring: Boolean = false,
    val frequency: String? = null,
    val notes: String? = null,
    val webLink: String? = null,
    val accountId: Int? = null,  // Account where bill payment comes from
    val linkedTransactionId: Int? = null,  // Transaction created when bill is paid
    val createdAt: String? = null,
    val syncedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val type: String,
    val name: String,
    val balance: Double,
    val accountType: String? = null,
    val creditLimit: Double? = null,
    val interestRate: Double? = null,
    val minimumPayment: Double? = null,
    val dueDay: Int? = null,
    val createdAt: String? = null,
    val syncedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val id: Int,
    val userId: Int,
    val amount: Double,
    val type: String,
    val category: String,
    val description: String? = null,
    val tags: String = "", // Comma-separated
    val isRecurring: Boolean = false,
    val frequency: String? = null,
    val date: String,
    val accountId: Int,
    val createdAt: String? = null,
    val syncedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey
    val id: Int,
    val userId: Int,
    val name: String,
    val amount: Double,
    val period: String,
    val category: String? = null,
    val createdAt: String? = null,
    val syncedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey
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
    val syncedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "paycheck_settings")
data class PaycheckSettingsEntity(
    @PrimaryKey
    val userId: Int,
    val amount: Double = 0.0,
    val frequencyDays: Int = 14,
    val nextPayDate: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)
@Entity(tableName = "bill_reminders")
data class BillReminderEntity(
    @PrimaryKey
    val id: Int,
    val billId: Int,
    val daysBeforeDue: Int = 1, // Remind N days before due date
    val reminderTime: String = "09:00", // HH:mm format
    val isEnabled: Boolean = true,
    val createdAt: String? = null,
    val syncedAt: Long = System.currentTimeMillis()
)