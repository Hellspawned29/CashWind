package com.cashwind.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cashwind.app.database.CashwindDatabase
import com.cashwind.app.util.DateUtils
import com.cashwind.app.util.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import java.util.Calendar

class BillReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val db = CashwindDatabase.getInstance(applicationContext)
            val billDao = db.billDao()
            val billReminderDao = db.billReminderDao()
            val goalDao = db.goalDao()
            val userId = 1 // Default user

            // Create notification channel
            NotificationHelper.createNotificationChannel(applicationContext)

            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            // Check bill reminders
            val allBills = billDao.getAllBills(userId).first()
            val reminders = billReminderDao.getAllReminders().first()

            allBills.forEach { bill ->
                if (bill.isPaid) return@forEach

                val reminder = reminders.find { it.billId == bill.id }
                if (reminder == null || !reminder.isEnabled) return@forEach

                try {
                    val dueDate = DateUtils.parseIsoDate(bill.dueDate)
                    if (dueDate != null) {
                        val dueCal = Calendar.getInstance().apply {
                            time = dueDate
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }

                        val daysUntilDue = ((dueCal.timeInMillis - today.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()

                        // Send notification if within reminder window
                        if (daysUntilDue in 0..reminder.daysBeforeDue) {
                            NotificationHelper.sendBillReminderNotification(
                                applicationContext,
                                bill.id,
                                bill.name,
                                bill.amount,
                                daysUntilDue
                            )
                        }
                    }
                } catch (e: Exception) {
                    // Ignore parse errors
                }
            }

            // Check goal deadlines (warn 7 days before)
            val allGoals = goalDao.getAllGoals(userId).first()
            allGoals.forEach { goal ->
                if (goal.status == "completed") return@forEach

                val remaining = goal.targetAmount - goal.currentAmount
                if (remaining <= 0) return@forEach

                try {
                    val targetDate = DateUtils.parseIsoDate(goal.targetDate)
                    if (targetDate != null) {
                        val targetCal = Calendar.getInstance().apply {
                            time = targetDate
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }

                        val daysUntilDeadline = ((targetCal.timeInMillis - today.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()

                        // Notify if deadline is within 7 days and goal is not on track
                        if (daysUntilDeadline in 0..7 && remaining > 0) {
                            NotificationHelper.sendGoalDeadlineNotification(
                                applicationContext,
                                goal.id,
                                goal.name,
                                remaining,
                                daysUntilDeadline
                            )
                        }
                    }
                } catch (e: Exception) {
                    // Ignore parse errors
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
