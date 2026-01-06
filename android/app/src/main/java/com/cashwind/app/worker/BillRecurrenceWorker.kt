package com.cashwind.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cashwind.app.database.CashwindDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BillRecurrenceWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            val database = CashwindDatabase.getInstance(applicationContext)
            val billDao = database.billDao()
            val userId = 1 // Default user

            val bills = billDao.getAllBills(userId).first()
            val now = Calendar.getInstance()
            val today = sdf.format(now.time)

            bills.forEach { billEntity ->
                // Only process recurring bills that are currently marked as paid
                if (billEntity.recurring && billEntity.isPaid) {
                    try {
                        val dueDate = sdf.parse(billEntity.dueDate)
                        if (dueDate != null) {
                            val dueCal = Calendar.getInstance().apply { time = dueDate }
                            
                            // Check if the bill is overdue for a new cycle
                            if (now >= dueCal) {
                                // Calculate the next due date
                                val nextDueDate = calculateNextDueDate(dueCal, billEntity.frequency)
                                
                                // Reset the bill: mark unpaid and update due date
                                val updatedBill = billEntity.copy(
                                    isPaid = false,
                                    dueDate = sdf.format(nextDueDate.time)
                                )
                                billDao.updateBill(updatedBill)
                            }
                        }
                    } catch (e: Exception) {
                        // Log error but continue processing other bills
                        e.printStackTrace()
                    }
                }
            }
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun calculateNextDueDate(currentDue: Calendar, frequency: String?): Calendar {
        return when (frequency?.lowercase(Locale.US)) {
            "weekly" -> (currentDue.clone() as Calendar).apply { add(Calendar.WEEK_OF_YEAR, 1) }
            "monthly" -> (currentDue.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
            "quarterly" -> (currentDue.clone() as Calendar).apply { add(Calendar.MONTH, 3) }
            "semi-annual" -> (currentDue.clone() as Calendar).apply { add(Calendar.MONTH, 6) }
            "annual" -> (currentDue.clone() as Calendar).apply { add(Calendar.YEAR, 1) }
            else -> (currentDue.clone() as Calendar).apply { add(Calendar.MONTH, 1) } // Default to monthly
        }
    }
}
