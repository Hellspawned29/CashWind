package com.cashwind.app.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.cashwind.app.MainActivity
import com.cashwind.app.R

object NotificationHelper {
    private const val CHANNEL_ID = "bill_reminders"
    private const val CHANNEL_NAME = "Bill Reminders"
    private const val CHANNEL_DESCRIPTION = "Notifications for upcoming bills"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendBillReminderNotification(
        context: Context,
        billId: Int,
        billName: String,
        amount: Double,
        daysUntilDue: Int
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            billId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = when {
            daysUntilDue == 0 -> "Bill Due Today!"
            daysUntilDue == 1 -> "Bill Due Tomorrow"
            else -> "Upcoming Bill"
        }

        val message = when {
            daysUntilDue == 0 -> "$billName (\$${String.format("%.2f", amount)}) is due today"
            daysUntilDue == 1 -> "$billName (\$${String.format("%.2f", amount)}) is due tomorrow"
            else -> "$billName (\$${String.format("%.2f", amount)}) is due in $daysUntilDue days"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(billId, notification)
    }

    fun sendGoalDeadlineNotification(
        context: Context,
        goalId: Int,
        goalName: String,
        remainingAmount: Double,
        daysUntilDeadline: Int
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            10000 + goalId, // Offset to avoid collision with bill IDs
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = when {
            daysUntilDeadline <= 7 -> "Goal Deadline Approaching"
            else -> "Goal Progress Update"
        }

        val message = "$goalName has \$${String.format("%.2f", remainingAmount)} remaining with $daysUntilDeadline days until deadline"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(10000 + goalId, notification)
    }

    fun sendUpdateNotification(
        context: Context,
        version: String,
        downloadUrl: String
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Intent to trigger download when notification is tapped
        val intent = Intent(context, com.cashwind.app.DashboardActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("check_update", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            20000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Cashwind Update Available")
            .setContentText("Version $version is now available!")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Tap to download and install version $version"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(20000, notification)
    }
}
