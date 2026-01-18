package com.cashwind.app

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.cashwind.app.fragments.HomeFragment
import android.widget.FrameLayout
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.cashwind.app.worker.BillReminderWorker
import com.cashwind.app.worker.BillRecurrenceWorker
import com.cashwind.app.worker.UpdateCheckWorker
import com.cashwind.app.util.UpdateChecker
import com.cashwind.app.ui.UpdateDialog
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import android.content.Intent

class DashboardActivity : BaseActivity() {
    private var fragmentContainerId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        
        // TEST BUTTON - Launch MainActivity directly from Activity (not fragment)
        val testButton = android.widget.Button(this).apply {
            text = "TEST: Open Bills (Direct)"
            setOnClickListener {
                android.util.Log.d("DashboardActivity", "Test button clicked - launching MainActivity")
                try {
                    val intent = Intent(this@DashboardActivity, MainActivity::class.java)
                    startActivity(intent)
                    android.util.Log.d("DashboardActivity", "MainActivity launched successfully")
                } catch (e: Exception) {
                    android.util.Log.e("DashboardActivity", "Failed to launch MainActivity: ${e.message}", e)
                    android.widget.Toast.makeText(this@DashboardActivity, "Error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                }
            }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        mainLayout.addView(testButton)
        
        // TEST BUTTON 2 - Launch TestActivity (no XML layout)
        val testButton2 = android.widget.Button(this).apply {
            text = "TEST 2: Open TestActivity (No XML)"
            setOnClickListener {
                android.util.Log.d("DashboardActivity", "Test button 2 clicked - launching TestActivity")
                try {
                    val intent = Intent(this@DashboardActivity, TestActivity::class.java)
                    startActivity(intent)
                    android.util.Log.d("DashboardActivity", "TestActivity launched successfully")
                } catch (e: Exception) {
                    android.util.Log.e("DashboardActivity", "Failed to launch TestActivity: ${e.message}", e)
                    android.widget.Toast.makeText(this@DashboardActivity, "Error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                }
            }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        mainLayout.addView(testButton2)
        
        val fragmentContainer = FrameLayout(this).apply {
            id = View.generateViewId()
            fragmentContainerId = id
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }
        
        mainLayout.addView(fragmentContainer)
        setContentView(mainLayout)
        
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }
        
        scheduleReminderWorker()
        scheduleRecurrenceWorker()
        scheduleUpdateCheckWorker()
        
        checkForUpdates()
    }
    
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(fragmentContainerId, fragment)
            .commit()
    }

    private fun scheduleReminderWorker() {
        val reminderWork = PeriodicWorkRequestBuilder<BillReminderWorker>(
            24, TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "bill_reminder_work",
            ExistingPeriodicWorkPolicy.KEEP,
            reminderWork
        )
    }

    private fun scheduleRecurrenceWorker() {
        val recurrenceWork = PeriodicWorkRequestBuilder<BillRecurrenceWorker>(
            24, TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "bill_recurrence_work",
            ExistingPeriodicWorkPolicy.KEEP,
            recurrenceWork
        )
    }

    private fun scheduleUpdateCheckWorker() {
        val updateWork = PeriodicWorkRequestBuilder<UpdateCheckWorker>(
            24, TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "update_check_work",
            ExistingPeriodicWorkPolicy.KEEP,
            updateWork
        )
    }

    private fun checkForUpdates() {
        lifecycleScope.launch {
            try {
                val currentVersion = BuildConfig.VERSION_NAME
                val updateInfo = withContext(Dispatchers.IO) {
                    UpdateChecker.checkForUpdates(currentVersion)
                }

                if (updateInfo != null && updateInfo.isNewer) {
                    UpdateDialog(this@DashboardActivity, updateInfo) {
                        UpdateChecker.downloadAndInstallUpdate(
                            this@DashboardActivity,
                            updateInfo.downloadUrl,
                            updateInfo.version
                        )
                    }.show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // When back is pressed on dashboard, minimize app instead of finishing
        moveTaskToBack(true)
    }
}
