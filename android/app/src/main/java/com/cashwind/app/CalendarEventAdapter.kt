package com.cashwind.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cashwind.app.database.entity.BillEntity
import java.text.SimpleDateFormat
import java.util.*

class CalendarEventAdapter : RecyclerView.Adapter<CalendarEventAdapter.EventViewHolder>() {

    private var events = emptyList<BillEntity>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(events[position])
    }

    override fun getItemCount(): Int = events.size

    fun setEvents(newEvents: List<BillEntity>) {
        events = newEvents
        notifyDataSetChanged()
    }

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val eventNameTextView: TextView = itemView.findViewById(R.id.eventNameTextView)
        private val eventAmountTextView: TextView = itemView.findViewById(R.id.eventAmountTextView)
        private val eventStatusTextView: TextView = itemView.findViewById(R.id.eventStatusTextView)

        fun bind(bill: BillEntity) {
            eventNameTextView.text = bill.name
            eventAmountTextView.text = "$${String.format("%.2f", bill.amount)}"
            eventStatusTextView.text = if (bill.isPaid) "Paid" else "Unpaid"
            eventStatusTextView.setTextColor(
                itemView.context.getColor(
                    if (bill.isPaid) android.R.color.holo_green_dark else android.R.color.holo_red_dark
                )
            )
        }
    }
}
