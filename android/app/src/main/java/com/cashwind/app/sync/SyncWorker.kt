package com.cashwind.app.sync

import android.content.Context
import androidx.work.*
import com.cashwind.app.database.CashwindDatabase
import com.cashwind.app.repository.SyncBillRepository
import com.cashwind.app.util.TokenManager
import java.util.concurrent.TimeUnit

class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    private val db = CashwindDatabase.getInstance(context)
    private val tokenManager = TokenManager(context)
    private val billRepo = SyncBillRepository(db, tokenManager)

    override suspend fun doWork(): Result {
        return try {
            // Sync all data from backend when network available
            val userId = 1 // Would come from auth state
            billRepo.syncBillsFromBackend(userId)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

object SyncManager {
    fun setupPeriodicSync(context: Context) {
        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            15, // Repeat every 15 minutes
            TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "cashwind_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }

    fun syncNow(context: Context) {
        val oneTimeSync = OneTimeWorkRequestBuilder<SyncWorker>().build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            "cashwind_sync_now",
            ExistingWorkPolicy.REPLACE,
            oneTimeSync
        )
    }
}
