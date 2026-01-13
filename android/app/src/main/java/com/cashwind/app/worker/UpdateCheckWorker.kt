package com.cashwind.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cashwind.app.BuildConfig
import com.cashwind.app.util.UpdateChecker
import com.cashwind.app.util.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UpdateCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val currentVersion = BuildConfig.VERSION_NAME
            val updateInfo = UpdateChecker.checkForUpdates(currentVersion)

            if (updateInfo != null && updateInfo.isNewer) {
                NotificationHelper.sendUpdateNotification(
                    applicationContext,
                    updateInfo.version,
                    updateInfo.downloadUrl
                )
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}
