package com.cashwind.app.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.cashwind.app.database.CashwindDatabase
import com.cashwind.app.database.entity.AccountEntity
import com.cashwind.app.model.Account
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AccountsViewModel(private val db: CashwindDatabase) : ViewModel() {
    private val accountDao = db.accountDao()

    val accounts: LiveData<List<Account>> = accountDao.getAllAccountsLive(userId = 1).map { list ->
        list.map { it.toModel() }
    }

    val totalBalance: LiveData<Double> = accounts.map { list -> list.sumOf { it.balance } }

    fun deleteAccount(account: Account) {
        viewModelScope.launch(Dispatchers.IO) {
            accountDao.deleteAccount(account.toEntity())
        }
    }

    fun saveOrUpdate(account: Account) {
        viewModelScope.launch(Dispatchers.IO) {
            if (account.id == 0) {
                accountDao.insertAccount(account.toEntity())
            } else {
                accountDao.updateAccount(account.toEntity())
            }
        }
    }

    private fun AccountEntity.toModel(): Account = Account(
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

    private fun Account.toEntity(): AccountEntity = AccountEntity(
        id = id,
        userId = if (userId == 0) 1 else userId,
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
}
