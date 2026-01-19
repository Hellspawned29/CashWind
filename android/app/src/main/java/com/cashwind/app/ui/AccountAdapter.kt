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
import com.cashwind.app.model.Account

class AccountAdapter(
    private val onClick: (Account) -> Unit,
    private val onTransactions: (Account) -> Unit,
    private val onDelete: (Account) -> Unit
) : ListAdapter<Account, AccountAdapter.AccountViewHolder>(AccountDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_account, parent, false)
        return AccountViewHolder(view)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        holder.bind(getItem(position), onClick, onTransactions, onDelete)
    }

    class AccountViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val accountName: TextView = itemView.findViewById(R.id.accountName)
        private val accountType: TextView = itemView.findViewById(R.id.accountType)
        private val accountBalance: TextView = itemView.findViewById(R.id.accountBalance)
        private val accountMeta: TextView = itemView.findViewById(R.id.accountMeta)
        
        fun bind(account: Account, onClick: (Account) -> Unit, onTransactions: (Account) -> Unit, onDelete: (Account) -> Unit) {
            accountName.text = account.name
            accountType.text = account.accountType ?: account.type
            accountBalance.text = formatCurrency(account.balance)

            val creditInfo = buildString {
                if (account.creditLimit != null) append("Limit: ${formatCurrency(account.creditLimit)}  ")
                if (account.interestRate != null) append("APR: ${account.interestRate}%  ")
                if (account.minimumPayment != null) append("Min: ${formatCurrency(account.minimumPayment)}  ")
                if (account.dueDay != null) append("Due: ${account.dueDay}")
            }.trim()
            accountMeta.text = if (creditInfo.isEmpty()) "" else creditInfo

            // Click to view transactions
            itemView.setOnClickListener { onTransactions(account) }
            // Long press to delete
            itemView.setOnLongClickListener {
                AlertDialog.Builder(itemView.context)
                    .setTitle("Delete Account")
                    .setMessage("Delete \"${account.name}\"? This cannot be undone.")
                    .setPositiveButton("Delete") { _, _ -> onDelete(account) }
                    .setNegativeButton("Cancel", null)
                    .show()
                true
            }
            // Double tap to edit (alternative: make a button)
            accountName.setOnClickListener { onClick(account) }
        }

        private fun formatCurrency(value: Double): String = "$${String.format("%.2f", value)}"
    }

    class AccountDiffCallback : DiffUtil.ItemCallback<Account>() {
        override fun areItemsTheSame(oldItem: Account, newItem: Account): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Account, newItem: Account): Boolean = oldItem == newItem
    }
}
