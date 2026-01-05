package com.cashwind.app.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.cashwind.app.database.CashwindDatabase
import com.cashwind.app.database.entity.AccountEntity
import com.cashwind.app.database.entity.TransactionEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AccountTransactionViewModel(private val db: CashwindDatabase, val accountId: Int) : ViewModel() {
    private val transactionDao = db.transactionDao()
    private val accountDao = db.accountDao()
    private val userId = 1 // Default user (no auth)

    val account: LiveData<AccountEntity?> = accountDao.getAccountLive(accountId)

    val transactions: LiveData<List<TransactionEntity>> = transactionDao.getAllTransactionsByAccountLive(accountId)

    fun addTransaction(
        amount: Double,
        type: String, // "income", "expense"
        category: String,
        description: String,
        date: String,
        isRecurring: Boolean = false,
        frequency: String? = null
    ) {
        viewModelScope.launch {
            val newId = (System.currentTimeMillis() / 1000).toInt()
            val transaction = TransactionEntity(
                id = newId,
                userId = userId,
                amount = amount,
                type = type,
                category = category,
                description = description,
                date = date,
                accountId = accountId,
                isRecurring = isRecurring,
                frequency = frequency,
                createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Calendar.getInstance().time)
            )
            transactionDao.insertTransaction(transaction)
            updateAccountBalance()
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            transactionDao.deleteTransaction(transaction)
            updateAccountBalance()
        }
    }

    fun updateTransaction(
        id: Int,
        amount: Double,
        type: String,
        category: String,
        description: String,
        date: String,
        isRecurring: Boolean = false,
        frequency: String? = null
    ) {
        viewModelScope.launch {
            val existingTransaction = transactionDao.getTransactionById(id) ?: return@launch
            val updatedTransaction = existingTransaction.copy(
                amount = amount,
                type = type,
                category = category,
                description = description,
                date = date,
                isRecurring = isRecurring,
                frequency = frequency
            )
            transactionDao.updateTransaction(updatedTransaction)
            updateAccountBalance()
        }
    }

    private suspend fun updateAccountBalance() {
        val account = accountDao.getAccountById(accountId) ?: return
        val transactions = transactionDao.getTransactionsByAccount(accountId)
        
        var newBalance = account.balance
        transactions.forEach { tx ->
            val sign = if (tx.type == "income") 1.0 else -1.0
            newBalance += (tx.amount * sign)
        }
        
        val updated = account.copy(balance = newBalance)
        accountDao.updateAccount(updated)
    }
}
