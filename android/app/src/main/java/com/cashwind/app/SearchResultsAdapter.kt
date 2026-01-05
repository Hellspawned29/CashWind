package com.cashwind.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cashwind.app.database.entity.AccountEntity
import com.cashwind.app.database.entity.BillEntity
import com.cashwind.app.database.entity.TransactionEntity
import java.text.SimpleDateFormat
import java.util.*

class SearchResultsAdapter :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<SearchResultItem>()

    sealed class SearchResultItem {
        data class BillItem(val bill: BillEntity) : SearchResultItem()
        data class AccountItem(val account: AccountEntity) : SearchResultItem()
        data class TransactionItem(val transaction: TransactionEntity) : SearchResultItem()
        data class HeaderItem(val title: String) : SearchResultItem()
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is SearchResultItem.BillItem -> VIEW_TYPE_BILL
            is SearchResultItem.AccountItem -> VIEW_TYPE_ACCOUNT
            is SearchResultItem.TransactionItem -> VIEW_TYPE_TRANSACTION
            is SearchResultItem.HeaderItem -> VIEW_TYPE_HEADER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_BILL -> BillViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_bill, parent, false)
            )
            VIEW_TYPE_ACCOUNT -> AccountViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_account, parent, false)
            )
            VIEW_TYPE_TRANSACTION -> TransactionViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_transaction, parent, false)
            )
            VIEW_TYPE_HEADER -> HeaderViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.search_header, parent, false)
            )
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is SearchResultItem.BillItem -> (holder as BillViewHolder).bind(item.bill)
            is SearchResultItem.AccountItem -> (holder as AccountViewHolder).bind(item.account)
            is SearchResultItem.TransactionItem -> (holder as TransactionViewHolder).bind(item.transaction)
            is SearchResultItem.HeaderItem -> (holder as HeaderViewHolder).bind(item.title)
        }
    }

    override fun getItemCount(): Int = items.size

    fun setResults(
        bills: List<BillEntity>,
        accounts: List<AccountEntity>,
        transactions: List<TransactionEntity>
    ) {
        items.clear()

        if (bills.isNotEmpty()) {
            items.add(SearchResultItem.HeaderItem("Bills"))
            bills.forEach { items.add(SearchResultItem.BillItem(it)) }
        }

        if (accounts.isNotEmpty()) {
            items.add(SearchResultItem.HeaderItem("Accounts"))
            accounts.forEach { items.add(SearchResultItem.AccountItem(it)) }
        }

        if (transactions.isNotEmpty()) {
            items.add(SearchResultItem.HeaderItem("Transactions"))
            transactions.forEach { items.add(SearchResultItem.TransactionItem(it)) }
        }

        notifyDataSetChanged()
    }

    inner class BillViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val billName: TextView? = itemView.findViewById(R.id.billName)
        private val billAmount: TextView? = itemView.findViewById(R.id.billAmount)
        private val billStatus: TextView? = itemView.findViewById(R.id.billStatus)
        private val billDueDate: TextView? = itemView.findViewById(R.id.billDueDate)

        fun bind(bill: BillEntity) {
            billName?.text = bill.name
            billAmount?.text = "$${String.format("%.2f", bill.amount)}"
            billStatus?.text = if (bill.isPaid) "Paid" else "Unpaid"
            billDueDate?.text = "Due: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(bill.dueDate))}"
        }
    }

    inner class AccountViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val accountName: TextView? = itemView.findViewById(R.id.accountName)
        private val accountBalance: TextView? = itemView.findViewById(R.id.accountBalance)
        private val accountType: TextView? = itemView.findViewById(R.id.accountType)

        fun bind(account: AccountEntity) {
            accountName?.text = account.name
            accountBalance?.text = "$${String.format("%.2f", account.balance)}"
            accountType?.text = account.type
        }
    }

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val transDescription: TextView? = itemView.findViewById(R.id.transactionDescription)
        private val transAmount: TextView? = itemView.findViewById(R.id.transactionAmount)
        private val transDate: TextView? = itemView.findViewById(R.id.transactionDate)

        fun bind(transaction: TransactionEntity) {
            transDescription?.text = transaction.description
            transAmount?.text = "$${String.format("%.2f", transaction.amount)}"
            transDate?.text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(transaction.date))
        }
    }

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val headerText: TextView? = itemView.findViewById(R.id.headerText)

        fun bind(title: String) {
            headerText?.text = title
        }
    }

    companion object {
        private const val VIEW_TYPE_BILL = 0
        private const val VIEW_TYPE_ACCOUNT = 1
        private const val VIEW_TYPE_TRANSACTION = 2
        private const val VIEW_TYPE_HEADER = 3
    }
}
