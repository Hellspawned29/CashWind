package com.cashwind.app.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.cashwind.app.database.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: BillEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBills(bills: List<BillEntity>)

    @Update
    suspend fun updateBill(bill: BillEntity)

    @Delete
    suspend fun deleteBill(bill: BillEntity)

    @Query("SELECT * FROM bills WHERE userId = :userId ORDER BY dueDate ASC")
    fun getAllBills(userId: Int): Flow<List<BillEntity>>

    @Query("SELECT * FROM bills ORDER BY dueDate ASC")
    fun getAllBillsLive(): LiveData<List<BillEntity>>

    @Query("SELECT * FROM bills WHERE id = :billId")
    suspend fun getBillById(billId: Int): BillEntity?

    @Query("DELETE FROM bills WHERE userId = :userId")
    suspend fun deleteAllBills(userId: Int)

    @Query("SELECT * FROM bills WHERE name LIKE '%' || :query || '%' ORDER BY dueDate ASC")
    fun searchBills(query: String): Flow<List<BillEntity>>
    
    @Query("SELECT * FROM bills WHERE name LIKE '%' || :query || '%' ORDER BY dueDate ASC")
    suspend fun searchBillsDirect(query: String): List<BillEntity>
    
    @Query("SELECT * FROM bills ORDER BY dueDate ASC")
    suspend fun getAllBillsDirect(): List<BillEntity>
    
    @Query("SELECT * FROM bills WHERE isPaid = 0 AND dueDate < :today ORDER BY dueDate ASC")
    suspend fun getOverdueBills(today: String): List<BillEntity>
}

@Dao
interface AccountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<AccountEntity>)

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Delete
    suspend fun deleteAccount(account: AccountEntity)

    @Query("SELECT * FROM accounts WHERE userId = :userId ORDER BY name ASC")
    fun getAllAccounts(userId: Int): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE userId = :userId ORDER BY name ASC")
    fun getAllAccountsLive(userId: Int): androidx.lifecycle.LiveData<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :accountId")
    suspend fun getAccountById(accountId: Int): AccountEntity?

    @Query("SELECT * FROM accounts WHERE id = :accountId")
    fun getAccountLive(accountId: Int): androidx.lifecycle.LiveData<AccountEntity?>

    @Query("DELETE FROM accounts WHERE userId = :userId")
    suspend fun deleteAllAccounts(userId: Int)

    @Query("SELECT * FROM accounts WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchAccounts(query: String): Flow<List<AccountEntity>>
    
    @Query("SELECT * FROM accounts WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    suspend fun searchAccountsDirect(query: String): List<AccountEntity>
    
    @Query("SELECT * FROM accounts WHERE userId = :userId ORDER BY name ASC")
    suspend fun getAllAccountsDirect(userId: Int): List<AccountEntity>
}

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC")
    fun getAllTransactions(userId: Int): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE accountId = :accountId ORDER BY date DESC")
    fun getTransactionsByAccountFlow(accountId: Int): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE accountId = :accountId ORDER BY date DESC")
    fun getAllTransactionsByAccountLive(accountId: Int): androidx.lifecycle.LiveData<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE accountId = :accountId ORDER BY date DESC")
    suspend fun getTransactionsByAccount(accountId: Int): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE id = :transactionId")
    suspend fun getTransactionById(transactionId: Int): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE description LIKE '%' || :query || '%' ORDER BY date DESC")
    fun searchTransactions(query: String): Flow<List<TransactionEntity>>
    
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC")
    suspend fun getAllTransactionsDirect(userId: Int): List<TransactionEntity>
    
    @Query("SELECT * FROM transactions WHERE description LIKE '%' || :query || '%' ORDER BY date DESC")
    suspend fun searchTransactionsDirect(query: String): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND type = 'income' AND isRecurring = 1 ORDER BY amount DESC")
    fun getRecurringIncomeLive(userId: Int): androidx.lifecycle.LiveData<List<TransactionEntity>>

    @Query("DELETE FROM transactions WHERE userId = :userId")
    suspend fun deleteAllTransactions(userId: Int)
}

@Dao
interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgets(budgets: List<BudgetEntity>)

    @Update
    suspend fun updateBudget(budget: BudgetEntity)

    @Delete
    suspend fun deleteBudget(budget: BudgetEntity)

    @Query("SELECT * FROM budgets WHERE userId = :userId ORDER BY period DESC")
    fun getAllBudgets(userId: Int): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE userId = :userId ORDER BY period DESC")
    fun getAllBudgetsLive(userId: Int): androidx.lifecycle.LiveData<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE id = :budgetId")
    suspend fun getBudgetById(budgetId: Int): BudgetEntity?

    @Query("DELETE FROM budgets WHERE userId = :userId")
    suspend fun deleteAllBudgets(userId: Int)
}

@Dao
interface GoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoals(goals: List<GoalEntity>)

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Delete
    suspend fun deleteGoal(goal: GoalEntity)

    @Query("SELECT * FROM goals WHERE userId = :userId ORDER BY targetDate ASC")
    fun getAllGoals(userId: Int): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE userId = :userId ORDER BY targetDate ASC")
    fun getAllGoalsLive(userId: Int): androidx.lifecycle.LiveData<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE id = :goalId")
    suspend fun getGoalById(goalId: Int): GoalEntity?

    @Query("DELETE FROM goals WHERE userId = :userId")
    suspend fun deleteAllGoals(userId: Int)
}

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Int): UserEntity?

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: Int)
}
@Dao
interface BillReminderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: BillReminderEntity)

    @Update
    suspend fun updateReminder(reminder: BillReminderEntity)

    @Delete
    suspend fun deleteReminder(reminder: BillReminderEntity)

    @Query("SELECT * FROM bill_reminders WHERE billId = :billId")
    suspend fun getReminderForBill(billId: Int): BillReminderEntity?

    @Query("SELECT * FROM bill_reminders WHERE billId = :billId")
    fun getReminderForBillLive(billId: Int): androidx.lifecycle.LiveData<BillReminderEntity?>

    @Query("SELECT * FROM bill_reminders")
    fun getAllReminders(): kotlinx.coroutines.flow.Flow<List<BillReminderEntity>>
}

@Dao
interface BillPaymentAllocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllocation(allocation: BillPaymentAllocationEntity)

    @Update
    suspend fun updateAllocation(allocation: BillPaymentAllocationEntity)

    @Delete
    suspend fun deleteAllocation(allocation: BillPaymentAllocationEntity)

    @Query("SELECT * FROM bill_payment_allocations WHERE billId = :billId AND userId = :userId ORDER BY createdAt DESC")
    suspend fun getAllocationsForBill(billId: Int, userId: Int): List<BillPaymentAllocationEntity>

    @Query("SELECT * FROM bill_payment_allocations WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllAllocationsLive(userId: Int): LiveData<List<BillPaymentAllocationEntity>>

    @Query("SELECT SUM(allocatedAmount) FROM bill_payment_allocations WHERE billId = :billId AND userId = :userId")
    suspend fun getTotalAllocatedForBill(billId: Int, userId: Int): Double?

    @Query("SELECT SUM(paidAmount) FROM bill_payment_allocations WHERE billId = :billId AND userId = :userId")
    suspend fun getTotalPaidForBill(billId: Int, userId: Int): Double?

    @Query("DELETE FROM bill_payment_allocations WHERE billId = :billId")
    suspend fun deleteAllocationsForBill(billId: Int)
}