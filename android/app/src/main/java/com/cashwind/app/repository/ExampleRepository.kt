package com.cashwind.app.repository

import com.cashwind.app.network.RetrofitProvider

class BillRepository {
    suspend fun listBills() = RetrofitProvider.billService.listBills()
}

class AccountRepository {
    suspend fun listAccounts() = RetrofitProvider.accountService.listAccounts()
}

class TransactionRepository {
    suspend fun listTransactions() = RetrofitProvider.transactionService.listTransactions()
    suspend fun getAnalytics() = RetrofitProvider.transactionService.getAnalytics()
}
