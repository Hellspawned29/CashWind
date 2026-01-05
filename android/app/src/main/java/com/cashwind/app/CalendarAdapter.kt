package com.cashwind.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CalendarAdapter(private val onDaySelected: (Int) -> Unit) :
    RecyclerView.Adapter<CalendarAdapter.CalendarDayViewHolder>() {

    private var days = emptyList<CalendarDay>()

    data class CalendarDay(val day: Int, val hasEvent: Boolean)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarDayViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_day, parent, false)
        return CalendarDayViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarDayViewHolder, position: Int) {
        holder.bind(days[position])
    }

    override fun getItemCount(): Int = days.size

    fun setDays(newDays: List<CalendarDay>) {
        days = newDays
        notifyDataSetChanged()
    }

    inner class CalendarDayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dayTextView: TextView = itemView.findViewById(R.id.dayTextView)
        private val eventIndicator: View = itemView.findViewById(R.id.eventIndicator)

        fun bind(day: CalendarDay) {
            if (day.day == 0) {
                dayTextView.text = ""
                itemView.isEnabled = false
                itemView.alpha = 0.3f
            } else {
                dayTextView.text = day.day.toString()
                itemView.isEnabled = true
                itemView.alpha = 1.0f
                eventIndicator.visibility = if (day.hasEvent) View.VISIBLE else View.GONE

                itemView.setOnClickListener {
                    onDaySelected(day.day)
                }
            }
        }
    }
}
