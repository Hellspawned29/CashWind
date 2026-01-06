package com.cashwind.app.repository

import androidx.room.withTransaction
import com.cashwind.app.database.CashwindDatabase
import com.cashwind.app.database.entity.*
import com.cashwind.app.model.*
import com.cashwind.app.network.RetrofitProvider
import com.cashwind.app.util.TokenManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SyncBillRepository(
    private val db: CashwindDatabase,
    private val tokenManager: TokenManager
) {
    fun getAllBillsLocal(userId: Int): Flow<List<Bill>> {
        return db.billDao().getAllBills(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun syncBillsFromBackend(userId: Int) {
        if (!tokenManager.isTokenValid()) return
        try {
            val bills = RetrofitProvider.billService.listBills()
            db.withTransaction {
                db.billDao().deleteAllBills(userId)
                db.billDao().insertBills(bills.map { it.toEntity() })
            }
        } catch (e: Exception) {
            // Handle network error gracefully
        }
    }

    suspend fun createBillLocally(request: CreateBillRequest): BillEntity {
        val bill = BillEntity(
            id = System.currentTimeMillis().toInt(),
            userId = 1, // Would come from current user
            name = request.name,
            amount = request.amount,
            dueDate = request.dueDate,
            category = request.category,
            recurring = request.recurring,
            frequency = request.frequency,
            notes = request.notes,
            webLink = request.webLink
        )
        db.billDao().insertBill(bill)
        
        // Sync to backend in background
        try {
            RetrofitProvider.billService.createBill(request)
        } catch (e: Exception) {
            // Will sync later when network available
        }
        
        return bill
    }
}

class SyncAccountRepository(
    private val db: CashwindDatabase,
    private val tokenManager: TokenManager
) {
    fun getAllAccountsLocal(userId: Int): Flow<List<Account>> {
        return db.accountDao().getAllAccounts(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun syncAccountsFromBackend(userId: Int) {
        if (!tokenManager.isTokenValid()) return
        try {
            val accounts = RetrofitProvider.accountService.listAccounts()
            db.withTransaction {
                db.accountDao().deleteAllAccounts(userId)
                db.accountDao().insertAccounts(accounts.map { it.toEntity() })
            }
        } catch (e: Exception) {
            // Handle network error gracefully
        }
    }
}

class SyncTransactionRepository(
    private val db: CashwindDatabase,
    private val tokenManager: TokenManager
) {
    fun getAllTransactionsLocal(userId: Int): Flow<List<Transaction>> {
        return db.transactionDao().getAllTransactions(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun syncTransactionsFromBackend(userId: Int) {
        if (!tokenManager.isTokenValid()) return
        try {
            val transactions = RetrofitProvider.transactionService.listTransactions()
            db.withTransaction {
                db.transactionDao().deleteAllTransactions(userId)
                db.transactionDao().insertTransactions(transactions.map { it.toEntity() })
            }
        } catch (e: Exception) {
            // Handle network error gracefully
        }
    }
}

class SyncBudgetRepository(
    private val db: CashwindDatabase,
    private val tokenManager: TokenManager
) {
    fun getAllBudgetsLocal(userId: Int): Flow<List<Budget>> {
        return db.budgetDao().getAllBudgets(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun syncBudgetsFromBackend(userId: Int) {
        if (!tokenManager.isTokenValid()) return
        try {
            val budgets = RetrofitProvider.budgetService.listBudgets()
            db.withTransaction {
                db.budgetDao().deleteAllBudgets(userId)
                db.budgetDao().insertBudgets(budgets.map { it.toEntity() })
            }
        } catch (e: Exception) {
            // Handle network error gracefully
        }
    }
}

class SyncGoalRepository(
    private val db: CashwindDatabase,
    private val tokenManager: TokenManager
) {
    fun getAllGoalsLocal(userId: Int): Flow<List<Goal>> {
        return db.goalDao().getAllGoals(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun syncGoalsFromBackend(userId: Int) {
        if (!tokenManager.isTokenValid()) return
        try {
            val goals = RetrofitProvider.goalService.listGoals()
            db.withTransaction {
                db.goalDao().deleteAllGoals(userId)
                db.goalDao().insertGoals(goals.map { it.toEntity() })
            }
        } catch (e: Exception) {
            // Handle network error gracefully
        }
    }
}

// Extension functions to convert between domain models and entities
fun Bill.toEntity() = BillEntity(
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
    createdAt = createdAt
)

fun BillEntity.toDomain() = Bill(
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
    createdAt = createdAt
)

fun Account.toEntity() = AccountEntity(
    id = id,
    userId = userId,
    type = type,
    name = name,
    balance = balance,
    accountType = accountType,
    creditLimit = creditLimit,
    interestRate = interestRate,
    minimumPayment = minimumPayment,
    dueDay = dueDay,
    createdAt = createdAt
)

fun AccountEntity.toDomain() = Account(
    id = id,
    userId = userId,
    type = type,
    name = name,
    balance = balance,
    accountType = accountType,
    creditLimit = creditLimit,
    interestRate = interestRate,
    minimumPayment = minimumPayment,
    dueDay = dueDay,
    createdAt = createdAt
)

fun Transaction.toEntity() = TransactionEntity(
    id = id,
    userId = userId,
    amount = amount,
    type = type,
    category = category,
    description = description,
    tags = tags.joinToString(","),
    isRecurring = isRecurring,
    frequency = frequency,
    date = date,
    accountId = accountId,
    createdAt = createdAt
)

fun TransactionEntity.toDomain() = Transaction(
    id = id,
    userId = userId,
    amount = amount,
    type = type,
    category = category,
    description = description,
    tags = tags.split(",").filter { it.isNotEmpty() },
    isRecurring = isRecurring,
    frequency = frequency,
    date = date,
    accountId = accountId,
    createdAt = createdAt
)

fun Budget.toEntity() = BudgetEntity(
    id = id,
    userId = userId,
    name = name,
    amount = amount,
    period = period,
    category = category,
    createdAt = createdAt
)

fun BudgetEntity.toDomain() = Budget(
    id = id,
    userId = userId,
    name = name,
    amount = amount,
    period = period,
    category = category,
    createdAt = createdAt
)

fun Goal.toEntity() = GoalEntity(
    id = id,
    userId = userId,
    name = name,
    type = type,
    targetAmount = targetAmount,
    currentAmount = currentAmount,
    targetDate = targetDate,
    monthlyContribution = monthlyContribution,
    priority = priority,
    status = status,
    accountId = accountId,
    category = category,
    notes = notes
)

fun GoalEntity.toDomain() = Goal(
    id = id,
    userId = userId,
    name = name,
    type = type,
    targetAmount = targetAmount,
    currentAmount = currentAmount,
    targetDate = targetDate,
    monthlyContribution = monthlyContribution,
    priority = priority,
    status = status,
    accountId = accountId,
    category = category,
    notes = notes
)
