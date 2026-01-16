package com.cashwind.app

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.cashwind.app.fragments.HomeFragment
import com.cashwind.app.fragments.MoreFragment
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
        
        val fragmentContainer = FrameLayout(this).apply {
            id = View.generateViewId()
            fragmentContainerId = id
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }
        
        val bottomNav = BottomNavigationView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            inflateMenu(R.menu.bottom_navigation)
            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home -> {
                        loadFragment(HomeFragment())
                        true
                    }
                    R.id.nav_bills -> {
                        val intent = Intent(this@DashboardActivity, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                        startActivity(intent)
                        false
                    }
                    R.id.nav_accounts -> {
                        val intent = Intent(this@DashboardActivity, AccountsActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                        startActivity(intent)
                        false
                    }
                    R.id.nav_analytics -> {
                        val intent = Intent(this@DashboardActivity, AnalyticsActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                        startActivity(intent)
                        false
                    }
                    R.id.nav_more -> {
                        loadFragment(MoreFragment())
                        true
                    }
                    else -> false
                }
            }
        }
        
        mainLayout.addView(fragmentContainer)
        mainLayout.addView(bottomNav)
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
}
