package com.cashwind.app.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cashwind.app.R
import com.cashwind.app.model.Bill
import com.cashwind.app.util.DateUtils

class BillAdapter(
    private val onTogglePaid: (Bill) -> Unit,
    private val onDelete: (Bill) -> Unit,
    private val onDetail: (Bill) -> Unit
) : ListAdapter<Bill, BillAdapter.BillViewHolder>(BillDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bill, parent, false)
        return BillViewHolder(view)
    }

    override fun onBindViewHolder(holder: BillViewHolder, position: Int) {
        holder.bind(getItem(position), onTogglePaid, onDelete, onDetail)
    }

    class BillViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val billName: TextView = itemView.findViewById(R.id.billName)
        private val billAmount: TextView = itemView.findViewById(R.id.billAmount)
        private val billDueDate: TextView = itemView.findViewById(R.id.billDueDate)
        private val billCategory: TextView = itemView.findViewById(R.id.billCategory)
        private val billStatus: TextView = itemView.findViewById(R.id.billStatus)
        
        fun bind(
            bill: Bill,
            onTogglePaid: (Bill) -> Unit,
            onDelete: (Bill) -> Unit,
            onDetail: (Bill) -> Unit
        ) {
            billName.text = bill.name
            
            // Display amount with past due indicator if applicable
            val amountText = if (bill.hasPastDue && bill.pastDueAmount > 0.0) {
                "$${String.format("%.2f", bill.amount)} + PD: $${String.format("%.2f", bill.pastDueAmount)}"
            } else {
                "$${String.format("%.2f", bill.amount)}"
            }
            billAmount.text = amountText
            
            billDueDate.text = "Due: ${formatDate(bill.dueDate)}"
            billCategory.text = bill.category ?: "General"
            billStatus.text = if (bill.isPaid) "Paid" else "Unpaid"
            billStatus.setTextColor(
                itemView.context.resources.getColor(
                    if (bill.isPaid) android.R.color.holo_green_dark else android.R.color.holo_red_dark,
                    null
                )
            )

            // Toggle paid/unpaid on status click
            billStatus.setOnClickListener {
                onTogglePaid(bill)
            }

            // Long press to delete
            itemView.setOnLongClickListener {
                AlertDialog.Builder(itemView.context)
                    .setTitle("Delete Bill")
                    .setMessage("Delete \"${bill.name}\"? This cannot be undone.")
                    .setPositiveButton("Delete") { _, _ -> onDelete(bill) }
                    .setNegativeButton("Cancel", null)
                    .show()
                true
            }

            // Tap row to open detail
            itemView.setOnClickListener {
                onDetail(bill)
            }
        }

        private fun formatDate(date: String): String {
            return try {
                val parsedDate = DateUtils.parseIsoDate(date)
                parsedDate?.let { DateUtils.formatDisplayDate(it) } ?: date
            } catch (e: Exception) {
                date
            }
        }
    }

    class BillDiffCallback : DiffUtil.ItemCallback<Bill>() {
        override fun areItemsTheSame(oldItem: Bill, newItem: Bill): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Bill, newItem: Bill): Boolean {
            return oldItem == newItem
        }
    }
}
