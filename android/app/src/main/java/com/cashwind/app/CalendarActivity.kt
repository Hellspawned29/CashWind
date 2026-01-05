package com.cashwind.app

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
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
    private lateinit var calendarGridRecyclerView: RecyclerView
    private lateinit var eventsRecyclerView: RecyclerView
    private lateinit var prevButton: Button
    private lateinit var nextButton: Button
    private lateinit var backButton: Button

    private lateinit var calendarAdapter: CalendarAdapter
    private lateinit var eventAdapter: CalendarEventAdapter

    private var currentCalendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        database = CashwindDatabase.getInstance(this)

        monthYearTextView = findViewById(R.id.monthYearTextView)
        calendarGridRecyclerView = findViewById(R.id.calendarGridRecyclerView)
        eventsRecyclerView = findViewById(R.id.eventsRecyclerView)
        prevButton = findViewById(R.id.prevButton)
        nextButton = findViewById(R.id.nextButton)
        backButton = findViewById(R.id.backButton)

        calendarAdapter = CalendarAdapter { selectedDate ->
            loadEventsForDate(selectedDate)
        }
        calendarGridRecyclerView.layoutManager = GridLayoutManager(this, 7)
        calendarGridRecyclerView.adapter = calendarAdapter

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

        lifecycleScope.launch {
            try {
                val bills = database.billDao().getAllBillsLive().value ?: emptyList()
                android.util.Log.d("CalendarActivity", "Bills loaded: ${bills.size}")
                loadCalendarDays(bills)
            } catch (e: Exception) {
                android.util.Log.e("CalendarActivity", "Error loading bills", e)
                e.printStackTrace()
                loadCalendarDays(emptyList())
            }
        }
    }

    private fun loadCalendarDays(bills: List<BillEntity>) {
        val daysInMonth = currentCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfWeek = Calendar.getInstance().apply {
            set(currentCalendar.get(Calendar.YEAR), currentCalendar.get(Calendar.MONTH), 1)
        }.get(Calendar.DAY_OF_WEEK) - 1

        val days = mutableListOf<CalendarAdapter.CalendarDay>()
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())

        // Empty cells for days before the month starts
        repeat(firstDayOfWeek) {
            days.add(CalendarAdapter.CalendarDay(0, false))
        }

        // Days of the month
        for (day in 1..daysInMonth) {
            val cal = Calendar.getInstance().apply {
                set(currentCalendar.get(Calendar.YEAR), currentCalendar.get(Calendar.MONTH), day)
            }
            val hasBill = bills.any { bill ->
                try {
                    val billDate = dateFormat.parse(bill.dueDate)
                    val billCal = Calendar.getInstance().apply { time = billDate }
                    billCal.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
                    billCal.get(Calendar.MONTH) == cal.get(Calendar.MONTH) &&
                    billCal.get(Calendar.DAY_OF_MONTH) == cal.get(Calendar.DAY_OF_MONTH)
                } catch (e: Exception) {
                    false
                }
            }
            days.add(CalendarAdapter.CalendarDay(day, hasBill))
        }

        calendarAdapter.setDays(days)
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
                val bills = database.billDao().getAllBillsLive().value ?: emptyList()
                val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                val eventsForDay = bills.filter { bill ->
                    try {
                        val billDate = dateFormat.parse(bill.dueDate)
                        val billCal = Calendar.getInstance().apply { time = billDate }
                        billCal.get(Calendar.YEAR) == selectedCal.get(Calendar.YEAR) &&
                        billCal.get(Calendar.MONTH) == selectedCal.get(Calendar.MONTH) &&
                        billCal.get(Calendar.DAY_OF_MONTH) == selectedCal.get(Calendar.DAY_OF_MONTH)
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
}
