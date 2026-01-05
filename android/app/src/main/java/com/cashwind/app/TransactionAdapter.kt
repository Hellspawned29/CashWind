package com.cashwind.app

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.TextView
import com.cashwind.app.database.entity.TransactionEntity

class TransactionAdapter(
    context: Context,
    private val transactions: MutableList<TransactionEntity>,
    private val onDelete: (TransactionEntity) -> Unit,
    private val onEdit: (TransactionEntity) -> Unit
) : ArrayAdapter<TransactionEntity>(context, R.layout.item_transaction, transactions) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false)

        val transaction = transactions[position]
        val dateView = view.findViewById<TextView>(R.id.transactionDate)
        val descView = view.findViewById<TextView>(R.id.transactionDescription)
        val categoryView = view.findViewById<TextView>(R.id.transactionCategory)
        val amountView = view.findViewById<TextView>(R.id.transactionAmount)
        val deleteBtn = view.findViewById<ImageButton>(R.id.deleteTransactionBtn)
        val editBtn = view.findViewById<ImageButton>(R.id.editTransactionBtn)

        dateView.text = transaction.date
        descView.text = transaction.description ?: "No description"
        categoryView.text = transaction.category

        val isIncome = transaction.type == "income"
        val sign = if (isIncome) "+" else "-"
        amountView.text = "$sign$${"%.2f".format(transaction.amount)}"
        amountView.setTextColor(if (isIncome) context.getColor(android.R.color.holo_green_dark) else context.getColor(android.R.color.holo_red_dark))

        editBtn.setOnClickListener {
            Log.d("TransactionAdapter", "Edit button clicked for: ${transaction.description}")
            onEdit(transaction)
        }

        deleteBtn.setOnClickListener {
            Log.d("TransactionAdapter", "Delete button clicked for: ${transaction.description}")
            onDelete(transaction)
        }

        return view
    }
}
