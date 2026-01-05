package com.cashwind.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cashwind.app.database.CashwindDatabase
import com.cashwind.app.database.entity.AccountEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddAccountViewModel(private val database: CashwindDatabase) : ViewModel() {
    private val accountDao = database.accountDao()

    fun saveOrUpdate(account: AccountEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            if (account.id == 0) {
                accountDao.insertAccount(account)
            } else {
                accountDao.updateAccount(account)
            }
        }
    }
}
