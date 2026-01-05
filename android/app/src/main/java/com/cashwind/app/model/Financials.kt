package com.cashwind.app.model

data class Bill(
    val id: Int,
    val userId: Int,
    val name: String,
    val amount: Double,
    val dueDate: String,
    val isPaid: Boolean,
    val category: String? = null,
    val recurring: Boolean = false,
    val frequency: String? = null,
    val notes: String? = null,
    val webLink: String? = null,
    val accountId: Int? = null,
    val createdAt: String? = null
)

data class CreateBillRequest(
    val name: String,
    val amount: Double,
    val dueDate: String,
    val category: String? = null,
    val recurring: Boolean = false,
    val frequency: String? = null,
    val notes: String? = null,
    val webLink: String? = null
)

data class Account(
    val id: Int,
    val userId: Int,
    val type: String,
    val name: String,
    val balance: Double,
    val accountType: String? = null,
    val creditLimit: Double? = null,
    val interestRate: Double? = null,
    val minimumPayment: Double? = null,
    val dueDay: Int? = null,
    val createdAt: String? = null
)

data class CreateAccountRequest(
    val type: String,
    val name: String,
    val balance: Double,
    val accountType: String? = null
)

data class Transaction(
    val id: Int,
    val userId: Int,
    val amount: Double,
    val type: String,
    val category: String,
    val description: String? = null,
    val tags: List<String> = emptyList(),
    val isRecurring: Boolean = false,
    val frequency: String? = null,
    val date: String,
    val accountId: Int,
    val account: AccountSummary? = null,
    val createdAt: String? = null
)

data class AccountSummary(
    val name: String,
    val type: String
)

data class CreateTransactionRequest(
    val amount: Double,
    val type: String,
    val category: String,
    val description: String? = null,
    val tags: List<String> = emptyList(),
    val isRecurring: Boolean = false,
    val frequency: String? = null,
    val date: String,
    val accountId: Int
)

data class TransactionAnalytics(
    val income: Double,
    val expenses: Double,
    val byCategory: Map<String, Double>
)
