package com.cashwind.app.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.cashwind.app.database.CashwindDatabase
import com.cashwind.app.database.entity.BillEntity
import com.cashwind.app.model.Account
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddBillViewModel(private val database: CashwindDatabase) : ViewModel() {
    private val billDao = database.billDao()
    private val accountDao = database.accountDao()

    // LiveData for all accounts - populated on demand
    private val _allAccounts = MutableLiveData<List<Account>>()
    val allAccounts: LiveData<List<Account>> = _allAccounts

    init {
        loadAccounts()
    }

    private fun loadAccounts() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // This will be implemented to load accounts from database
                _allAccounts.postValue(emptyList())
            } catch (e: Exception) {
                _allAccounts.postValue(emptyList())
            }
        }
    }

    fun saveOrUpdate(bill: BillEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            if (bill.id == 0) {
                billDao.insertBill(bill)
            } else {
                billDao.updateBill(bill)
            }
        }
    }
}
