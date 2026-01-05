package com.cashwind.app

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import com.cashwind.app.ui.BudgetWithSpent

class BudgetAdapter(
    context: Context,
    private val budgets: MutableList<BudgetWithSpent>,
    private val onDelete: (BudgetWithSpent) -> Unit
) : ArrayAdapter<BudgetWithSpent>(context, R.layout.item_budget, budgets) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_budget, parent, false)

        val item = budgets[position]
        val nameView = view.findViewById<TextView>(R.id.budgetName)
        val periodView = view.findViewById<TextView>(R.id.budgetPeriod)
        val categoryView = view.findViewById<TextView>(R.id.budgetCategory)
        val spentView = view.findViewById<TextView>(R.id.budgetSpent)
        val remainingView = view.findViewById<TextView>(R.id.budgetRemaining)
        val progressBar = view.findViewById<ProgressBar>(R.id.budgetProgressBar)
        val deleteBtn = view.findViewById<ImageButton>(R.id.deleteBudgetBtn)

        nameView.text = item.budget.name
        periodView.text = item.budget.period.replaceFirstChar { it.uppercase() }
        categoryView.text = item.budget.category
        spentView.text = formatCurrency(item.spent)
        remainingView.text = formatCurrency(item.remaining)

        progressBar.progress = item.percentUsed
        progressBar.max = 100

        val color = when {
            item.percentUsed >= 100 -> context.getColor(android.R.color.holo_red_dark)
            item.percentUsed >= 80 -> context.getColor(android.R.color.holo_orange_dark)
            else -> context.getColor(android.R.color.holo_green_dark)
        }
        progressBar.progressDrawable.setTint(color)

        deleteBtn.setOnClickListener {
            onDelete(item)
            budgets.removeAt(position)
            notifyDataSetChanged()
        }

        return view
    }

    private fun formatCurrency(value: Double): String = "$${String.format("%.2f", value)}"
}
