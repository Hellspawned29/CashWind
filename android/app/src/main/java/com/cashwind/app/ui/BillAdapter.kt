package com.cashwind.app.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cashwind.app.databinding.ItemBillBinding
import com.cashwind.app.model.Bill
import com.cashwind.app.util.DateUtils

class BillAdapter(
    private val onTogglePaid: (Bill) -> Unit,
    private val onDelete: (Bill) -> Unit,
    private val onDetail: (Bill) -> Unit
) : ListAdapter<Bill, BillAdapter.BillViewHolder>(BillDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillViewHolder {
        val binding = ItemBillBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BillViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BillViewHolder, position: Int) {
        holder.bind(getItem(position), onTogglePaid, onDelete, onDetail)
    }

    class BillViewHolder(private val binding: ItemBillBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            bill: Bill,
            onTogglePaid: (Bill) -> Unit,
            onDelete: (Bill) -> Unit,
            onDetail: (Bill) -> Unit
        ) {
            binding.billName.text = bill.name
            
            // Display amount with past due indicator if applicable
            val amountText = if (bill.hasPastDue && bill.pastDueAmount > 0.0) {
                "$${String.format("%.2f", bill.amount)} + PD: $${String.format("%.2f", bill.pastDueAmount)}"
            } else {
                "$${String.format("%.2f", bill.amount)}"
            }
            binding.billAmount.text = amountText
            
            binding.billDueDate.text = "Due: ${formatDate(bill.dueDate)}"
            binding.billCategory.text = bill.category ?: "General"
            binding.billStatus.text = if (bill.isPaid) "Paid" else "Unpaid"
            binding.billStatus.setTextColor(
                binding.root.context.resources.getColor(
                    if (bill.isPaid) android.R.color.holo_green_dark else android.R.color.holo_red_dark,
                    null
                )
            )

            // Toggle paid/unpaid on status click
            binding.billStatus.setOnClickListener {
                onTogglePaid(bill)
            }

            // Long press to delete
            binding.root.setOnLongClickListener {
                AlertDialog.Builder(binding.root.context)
                    .setTitle("Delete Bill")
                    .setMessage("Delete \"${bill.name}\"? This cannot be undone.")
                    .setPositiveButton("Delete") { _, _ -> onDelete(bill) }
                    .setNegativeButton("Cancel", null)
                    .show()
                true
            }

            // Tap row to open detail
            binding.root.setOnClickListener {
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
