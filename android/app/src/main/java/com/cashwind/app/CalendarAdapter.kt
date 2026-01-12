package com.cashwind.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CalendarAdapter(private val onDaySelected: (Int) -> Unit) :
    RecyclerView.Adapter<CalendarAdapter.CalendarDayViewHolder>() {

    private var days = emptyList<CalendarDay>()
    private var itemSize = 0

    data class CalendarDay(val day: Int, val hasEvent: Boolean)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarDayViewHolder {
        android.util.Log.d("CalendarAdapter", "onCreateViewHolder called")
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_day, parent, false)
        
        // Calculate item size if not already done
        if (itemSize == 0) {
            itemSize = parent.width / 7
        }
        
        // Set explicit layout params
        view.layoutParams = ViewGroup.LayoutParams(itemSize, itemSize)
        
        android.util.Log.d("CalendarAdapter", "View inflated: ${view.width}x${view.height}, layoutParams: ${itemSize}x${itemSize}")
        return CalendarDayViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarDayViewHolder, position: Int) {
        android.util.Log.d("CalendarAdapter", "onBindViewHolder position=$position, day=${days[position].day}")
        holder.bind(days[position])
    }

    override fun getItemCount(): Int {
        android.util.Log.d("CalendarAdapter", "getItemCount: ${days.size}")
        return days.size
    }

    fun setDays(newDays: List<CalendarDay>) {
        android.util.Log.d("CalendarAdapter", "setDays called with ${newDays.size} days")
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
