package com.cashwind.app.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cashwind.app.databinding.ItemAccountBinding
import com.cashwind.app.model.Account

class AccountAdapter(
    private val onClick: (Account) -> Unit,
    private val onTransactions: (Account) -> Unit,
    private val onDelete: (Account) -> Unit
) : ListAdapter<Account, AccountAdapter.AccountViewHolder>(AccountDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val binding = ItemAccountBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AccountViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        holder.bind(getItem(position), onClick, onTransactions, onDelete)
    }

    class AccountViewHolder(private val binding: ItemAccountBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(account: Account, onClick: (Account) -> Unit, onTransactions: (Account) -> Unit, onDelete: (Account) -> Unit) {
            binding.accountName.text = account.name
            binding.accountType.text = account.accountType ?: account.type
            binding.accountBalance.text = formatCurrency(account.balance)

            val creditInfo = buildString {
                if (account.creditLimit != null) append("Limit: ${formatCurrency(account.creditLimit)}  ")
                if (account.interestRate != null) append("APR: ${account.interestRate}%  ")
                if (account.minimumPayment != null) append("Min: ${formatCurrency(account.minimumPayment)}  ")
                if (account.dueDay != null) append("Due: ${account.dueDay}")
            }.trim()
            binding.accountMeta.text = if (creditInfo.isEmpty()) "" else creditInfo

            // Click to view transactions
            binding.root.setOnClickListener { onTransactions(account) }
            // Long press to delete
            binding.root.setOnLongClickListener {
                AlertDialog.Builder(binding.root.context)
                    .setTitle("Delete Account")
                    .setMessage("Delete \"${account.name}\"? This cannot be undone.")
                    .setPositiveButton("Delete") { _, _ -> onDelete(account) }
                    .setNegativeButton("Cancel", null)
                    .show()
                true
            }
            // Double tap to edit (alternative: make a button)
            binding.accountName.setOnClickListener { onClick(account) }
        }

        private fun formatCurrency(value: Double): String = "$${String.format("%.2f", value)}"
    }

    class AccountDiffCallback : DiffUtil.ItemCallback<Account>() {
        override fun areItemsTheSame(oldItem: Account, newItem: Account): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Account, newItem: Account): Boolean = oldItem == newItem
    }
}
