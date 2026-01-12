package com.cashwind.app

import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cashwind.app.database.CashwindDatabase
import com.cashwind.app.database.entity.BillEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CalendarActivity : AppCompatActivity() {

    private lateinit var database: CashwindDatabase
    private lateinit var monthYearTextView: TextView
    private lateinit var calendarTable: TableLayout
    private lateinit var eventsRecyclerView: RecyclerView
    private lateinit var prevButton: Button
    private lateinit var nextButton: Button
    private lateinit var backButton: Button

    private lateinit var eventAdapter: CalendarEventAdapter

    private var currentCalendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        database = CashwindDatabase.getInstance(this)

        monthYearTextView = findViewById(R.id.monthYearTextView)
        calendarTable = findViewById(R.id.calendarTable)
        eventsRecyclerView = findViewById(R.id.eventsRecyclerView)
        prevButton = findViewById(R.id.prevButton)
        nextButton = findViewById(R.id.nextButton)
        backButton = findViewById(R.id.backButton)

        eventAdapter = CalendarEventAdapter()
        eventsRecyclerView.layoutManager = LinearLayoutManager(this)
        eventsRecyclerView.adapter = eventAdapter

        backButton.setOnClickListener {
            finish()
        }

        prevButton.setOnClickListener {
            currentCalendar.add(Calendar.MONTH, -1)
            updateCalendar()
        }

        nextButton.setOnClickListener {
            currentCalendar.add(Calendar.MONTH, 1)
            updateCalendar()
        }

        updateCalendar()
    }

    private fun updateCalendar() {
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        monthYearTextView.text = monthFormat.format(currentCalendar.time)

        android.util.Log.d("CalendarActivity", "updateCalendar called")
        
        // Build the calendar synchronously on the main thread first
        buildCalendarGrid(emptyList())

        // Then load bills in background and update
        lifecycleScope.launch {
            try {
                val bills = database.billDao().getAllBillsDirect()
                android.util.Log.d("CalendarActivity", "Bills loaded: ${bills.size}")
                runOnUiThread {
                    buildCalendarGrid(bills)
                }
            } catch (e: Exception) {
                android.util.Log.e("CalendarActivity", "Error loading bills", e)
                e.printStackTrace()
            }
        }
    }

    private fun buildCalendarGrid(bills: List<BillEntity>) {
        android.util.Log.d("CalendarActivity", "buildCalendarGrid called with ${bills.size} bills")
        loadCalendarDays(bills)
    }

    private fun loadCalendarDays(bills: List<BillEntity>) {
        android.util.Log.d("CalendarActivity", "loadCalendarDays START")
        try {
            calendarTable.removeAllViews()
            
            val daysInMonth = currentCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            val firstDayOfWeek = Calendar.getInstance().apply {
                set(currentCalendar.get(Calendar.YEAR), currentCalendar.get(Calendar.MONTH), 1)
            }.get(Calendar.DAY_OF_WEEK) - 1

            android.util.Log.d("CalendarActivity", "Days in month: $daysInMonth, First day: $firstDayOfWeek")

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val totalCells = firstDayOfWeek + daysInMonth
            val rows = Math.ceil(totalCells / 7.0).toInt()
            
            android.util.Log.d("CalendarActivity", "Building $rows rows")

            var dayCounter = 1
            for (r in 0 until rows) {
                val row = TableRow(this)
                row.layoutParams = TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    dpToPx(100) // Explicit height
                )

                for (c in 0..6) {
                    val index = r * 7 + c
                    val tv = TextView(this)
                    tv.layoutParams = TableRow.LayoutParams(
                        0, 
                        dpToPx(100), // Explicit height instead of WRAP_CONTENT
                        1f
                    )
                    tv.gravity = Gravity.CENTER
                    tv.setPadding(8, 8, 8, 8)
                    tv.textSize = 18f
                    tv.setTypeface(null, android.graphics.Typeface.BOLD)

                    // Get theme colors
                    val backgroundColor = getColorBackground()
                    val textColor = getTextColorPrimary()
                    val surfaceColor = getSurfaceColor()
                    val borderColor = if (isDarkMode()) 0xFF3A3A3A.toInt() else 0xFFCCCCCC.toInt()
                    
                    // Set default background with border
                    val drawable = android.graphics.drawable.GradientDrawable()
                    drawable.setColor(backgroundColor)
                    drawable.setStroke(2, borderColor)
                    tv.background = drawable
                    tv.setTextColor(textColor)

                    if (index < firstDayOfWeek) {
                        tv.text = ""
                        val emptyDrawable = android.graphics.drawable.GradientDrawable()
                        val emptyColor = if (isDarkMode()) 0xFF2C2C2C.toInt() else 0xFFEEEEEE.toInt()
                        val borderColor = if (isDarkMode()) 0xFF3A3A3A.toInt() else 0xFFCCCCCC.toInt()
                        emptyDrawable.setColor(emptyColor)
                        emptyDrawable.setStroke(2, borderColor)
                        tv.background = emptyDrawable
                    } else if (dayCounter <= daysInMonth) {
                        tv.text = dayCounter.toString()
                        
                        val cal = Calendar.getInstance().apply {
                            set(currentCalendar.get(Calendar.YEAR), currentCalendar.get(Calendar.MONTH), dayCounter)
                        }
                        
                        // Check for bills on this day
                        val dayBills = bills.filter { bill ->
                            try {
                                val billDate = dateFormat.parse(bill.dueDate)
                                if (billDate != null) {
                                    val billCal = Calendar.getInstance().apply { time = billDate }
                                    billCal.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
                                    billCal.get(Calendar.MONTH) == cal.get(Calendar.MONTH) &&
                                    billCal.get(Calendar.DAY_OF_MONTH) == cal.get(Calendar.DAY_OF_MONTH)
                                } else {
                                    false
                                }
                            } catch (e: Exception) {
                                false
                            }
                        }
                        
                        // Check if any bills are overdue (unpaid and in the past)
                        val today = Calendar.getInstance()
                        val hasOverdueBill = dayBills.any { bill ->
                            !bill.isPaid && cal.before(today)
                        }

                        if (hasOverdueBill) {
                            val overdueDrawable = android.graphics.drawable.GradientDrawable()
                            val borderColor = if (isDarkMode()) 0xFF3A3A3A.toInt() else 0xFFCCCCCC.toInt()
                            overdueDrawable.setColor(0xFFD32F2F.toInt()) // Red for overdue
                            overdueDrawable.setStroke(2, borderColor)
                            tv.background = overdueDrawable
                            tv.setTextColor(0xFFFFFFFF.toInt()) // White text
                        } else if (dayBills.isNotEmpty()) {
                            val upcomingDrawable = android.graphics.drawable.GradientDrawable()
                            val borderColor = if (isDarkMode()) 0xFF3A3A3A.toInt() else 0xFFCCCCCC.toInt()
                            upcomingDrawable.setColor(0xFF1976D2.toInt()) // Blue for upcoming bills
                            upcomingDrawable.setStroke(2, borderColor)
                            tv.background = upcomingDrawable
                            tv.setTextColor(0xFFFFFFFF.toInt()) // White text
                        }
                        
                        val selectedDay = dayCounter
                        tv.setOnClickListener {
                            loadEventsForDate(selectedDay)
                        }
                        dayCounter += 1
                    } else {
                        tv.text = ""
                        val emptyDrawable = android.graphics.drawable.GradientDrawable()
                        val emptyColor = if (isDarkMode()) 0xFF2C2C2C.toInt() else 0xFFEEEEEE.toInt()
                        val borderColor = if (isDarkMode()) 0xFF3A3A3A.toInt() else 0xFFCCCCCC.toInt()
                        emptyDrawable.setColor(emptyColor)
                        emptyDrawable.setStroke(2, borderColor)
                        tv.background = emptyDrawable
                    }

                    row.addView(tv)
                }

                calendarTable.addView(row)
            }

            android.util.Log.d("CalendarActivity", "Table rows added: ${calendarTable.childCount}")
            
        } catch (e: Exception) {
            android.util.Log.e("CalendarActivity", "Error building calendar grid", e)
            e.printStackTrace()
            calendarTable.removeAllViews()
            val errorText = TextView(this).apply {
                text = "Error: ${e.message}"
                textSize = 18f
                setTextColor(0xFFFF0000.toInt())
                gravity = Gravity.CENTER
                setPadding(24, 24, 24, 24)
            }
            calendarTable.addView(errorText)
        }
    }

    private fun loadEventsForDate(day: Int) {
        if (day == 0) return

        val selectedCal = Calendar.getInstance().apply {
            set(currentCalendar.get(Calendar.YEAR), currentCalendar.get(Calendar.MONTH), day)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        lifecycleScope.launch {
            try {
                val bills = database.billDao().getAllBillsDirect()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val eventsForDay = bills.filter { bill ->
                    try {
                        val billDate = dateFormat.parse(bill.dueDate)
                        if (billDate != null) {
                            val billCal = Calendar.getInstance().apply { time = billDate }
                            billCal.get(Calendar.YEAR) == selectedCal.get(Calendar.YEAR) &&
                            billCal.get(Calendar.MONTH) == selectedCal.get(Calendar.MONTH) &&
                            billCal.get(Calendar.DAY_OF_MONTH) == selectedCal.get(Calendar.DAY_OF_MONTH)
                        } else {
                            false
                        }
                    } catch (e: Exception) {
                        false
                    }
                }
                eventAdapter.setEvents(eventsForDay)
            } catch (e: Exception) {
                e.printStackTrace()
                eventAdapter.setEvents(emptyList())
            }
        }
    }

    private fun isDarkMode(): Boolean {
        val nightModeFlags = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun getThemeColor(attrResId: Int): Int {
        val typedValue = android.util.TypedValue()
        theme.resolveAttribute(attrResId, typedValue, true)
        return typedValue.data
    }

    private fun getColorBackground(): Int = getThemeColor(android.R.attr.colorBackground)
    private fun getTextColorPrimary(): Int = getThemeColor(android.R.attr.textColorPrimary)
    private fun getSurfaceColor(): Int {
        val typedValue = android.util.TypedValue()
        return if (theme.resolveAttribute(com.google.android.material.R.attr.colorSurface, typedValue, true)) {
            typedValue.data
        } else {
            getColorBackground()
        }
    }
}
