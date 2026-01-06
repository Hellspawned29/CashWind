package com.cashwind.app

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.cashwind.app.BuildConfig
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.cashwind.app.worker.BillReminderWorker
import com.cashwind.app.worker.BillRecurrenceWorker
import com.cashwind.app.util.NotificationHelper
import com.cashwind.app.database.CashwindDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class DashboardActivity : Activity() {
    private var billsCard: LinearLayout? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            initializeApp()
        } catch (e: Exception) {
            e.printStackTrace()
            finish()
        }
    }
    
    private fun initializeApp() {
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setPadding(40, 60, 40, 40)
            gravity = Gravity.CENTER_HORIZONTAL
        }
        
        val titleRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(0, 20, 0, 40)
        }

        val title = TextView(this).apply {
            text = "Cashwind"
            textSize = 36f
            gravity = Gravity.CENTER
        }

        val version = TextView(this).apply {
            text = "v${BuildConfig.VERSION_NAME}"
            textSize = 14f
            setPadding(12, 16, 0, 0)
            gravity = Gravity.CENTER_VERTICAL
        }

        titleRow.addView(title)
        titleRow.addView(version)
        mainLayout.addView(titleRow)
        
        // Row 1: Bills and Accounts
        val row1 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            gravity = Gravity.CENTER
        }
        
        billsCard = createCard("📋", "Bills", null) {
            startActivity(Intent(this, MainActivity::class.java))
        }
        row1.addView(billsCard)
        
        row1.addView(createCard("💳", "Accounts", null) {
            startActivity(Intent(this, AccountsActivity::class.java))
        })
        
        mainLayout.addView(row1)
        
        // Row 2: Paycheck and Budgets
        val row2 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            gravity = Gravity.CENTER
            setPadding(0, 20, 0, 0)
        }
        
        row2.addView(createCard("💰", "Paycheck", null) {
            startActivity(Intent(this, PaycheckActivity::class.java))
        })

        row2.addView(createCard("📊", "Budgets", null) {
            startActivity(Intent(this, BudgetActivity::class.java))
        })
        
        mainLayout.addView(row2)

        // Row 3: Goals and Analytics
        val row3 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            gravity = Gravity.CENTER
            setPadding(0, 20, 0, 0)
        }

        row3.addView(createCard("🎯", "Goals", null) {
            startActivity(Intent(this, GoalsActivity::class.java))
        })

        row3.addView(createCard("📊", "Analytics", null) {
            startActivity(Intent(this, AnalyticsActivity::class.java))
        })

        mainLayout.addView(row3)
        
        // Row 4: Calendar
        val row4 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            gravity = Gravity.CENTER
            setPadding(0, 20, 0, 0)
        }

        row4.addView(createCard("📅", "Calendar", null) {
            startActivity(Intent(this, CalendarActivity::class.java))
        })

        // Placeholder for future feature
        row4.addView(createCard("🔍", "Search", null) {
            // TODO: Implement search
        })

        mainLayout.addView(row4)
        
        setContentView(mainLayout)
        
        // Setup notification channel and schedule reminders after UI is ready
        NotificationHelper.createNotificationChannel(this)
        scheduleReminderWorker()
        scheduleRecurrenceWorker()
        if (BuildConfig.DEBUG) {
            scheduleRecurrenceTestWork()
        }
        
        // Load bills data
        loadBillsData()
    }
    
    private fun loadBillsData() {
        GlobalScope.launch {
            val db = CashwindDatabase.getInstance(this@DashboardActivity)
            val bills = db.billDao().getAllBills(1).first() // userId = 1
            
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val calendar = Calendar.getInstance()
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentYear = calendar.get(Calendar.YEAR)
            
            val monthTotal = bills.filter { bill ->
                try {
                    val dueDate = sdf.parse(bill.dueDate)
                    if (dueDate != null) {
                        val dueCal = Calendar.getInstance().apply { time = dueDate }
                        dueCal.get(Calendar.MONTH) == currentMonth && 
                        dueCal.get(Calendar.YEAR) == currentYear
                    } else false
                } catch (e: Exception) {
                    false
                }
            }.sumOf { it.amount }
            
            runOnUiThread {
                updateBillsCard(monthTotal)
            }
        }
    }
    
    private fun updateBillsCard(monthTotal: Double) {
        billsCard?.let { card ->
            // Get theme colors
            val typedArray = obtainStyledAttributes(
                intArrayOf(
                    android.R.attr.colorBackground,
                    android.R.attr.textColorSecondary
                )
            )
            val textColor = typedArray.getColor(1, Color.GRAY)
            typedArray.recycle()
            
            val amountText = TextView(this).apply {
                text = "$${String.format("%.2f", monthTotal)}"
                textSize = 16f
                setTextColor(textColor)
                gravity = Gravity.CENTER
                setPadding(0, 4, 0, 0)
            }
            card.addView(amountText)
        }
    }
    
    private fun createCard(icon: String, label: String, amount: String?, onClick: () -> Unit): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(30, 40, 30, 40)
            layoutParams = LinearLayout.LayoutParams(
                0,
                dpToPx(180),
                1f
            ).apply {
                setMargins(10, 10, 10, 10)
            }
            
            // Get theme colors using TypedArray
            val typedArray = this@DashboardActivity.obtainStyledAttributes(
                intArrayOf(
                    android.R.attr.colorBackground,
                    android.R.attr.textColorPrimary
                )
            )
            val bgColor = typedArray.getColor(0, Color.WHITE)
            val textColor = typedArray.getColor(1, Color.BLACK)
            typedArray.recycle()
            
            val drawable = GradientDrawable().apply {
                setColor(bgColor)
                cornerRadius = dpToPx(16).toFloat()
                setStroke(1, if (textColor == Color.WHITE) Color.parseColor("#333333") else Color.parseColor("#E0E0E0"))
            }
            background = drawable
            elevation = dpToPx(4).toFloat()
            
            val iconText = TextView(context).apply {
                text = icon
                textSize = 48f
                setPadding(0, 0, 0, 16)
            }
            
            val labelText = TextView(context).apply {
                text = label
                textSize = 18f
                setTextColor(textColor)
                gravity = Gravity.CENTER
            }
            
            addView(iconText)
            addView(labelText)
            
            setOnClickListener { onClick() }
        }
    }
    
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun scheduleReminderWorker() {
        val reminderWork = PeriodicWorkRequestBuilder<BillReminderWorker>(
            24, TimeUnit.HOURS // Check once daily
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "bill_reminder_work",
            ExistingPeriodicWorkPolicy.KEEP,
            reminderWork
        )
    }

    private fun scheduleRecurrenceWorker() {
        val recurrenceWork = PeriodicWorkRequestBuilder<BillRecurrenceWorker>(
            24, TimeUnit.HOURS // Check once daily for bills that need recurrence reset
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "bill_recurrence_work",
            ExistingPeriodicWorkPolicy.KEEP,
            recurrenceWork
        )
    }

    private fun scheduleRecurrenceTestWork() {
        val testWork = OneTimeWorkRequestBuilder<BillRecurrenceWorker>().build()
        WorkManager.getInstance(this).enqueue(testWork)
    }
}
