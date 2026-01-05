package com.cashwind.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cashwind.app.database.CashwindDatabase
import com.cashwind.app.database.entity.BillEntity
import com.cashwind.app.model.Bill
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BillDetailViewModel(private val database: CashwindDatabase) : ViewModel() {
    private val billDao = database.billDao()

    fun togglePaid(bill: Bill, onDone: (Bill) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = bill.copy(isPaid = !bill.isPaid)
            billDao.updateBill(updated.toEntity())
            withContext(Dispatchers.Main) { onDone(updated) }
        }
    }

    fun delete(bill: Bill, onDone: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            billDao.deleteBill(bill.toEntity())
            withContext(Dispatchers.Main) { onDone() }
        }
    }

    private fun Bill.toEntity(): BillEntity {
        return BillEntity(
            id = id,
            userId = if (userId == 0) 1 else userId,
            name = name,
            amount = amount,
            dueDate = dueDate,
            isPaid = isPaid,
            category = category,
            recurring = recurring,
            frequency = frequency,
            notes = notes,
            webLink = webLink,
            createdAt = createdAt
        )
    }
}
