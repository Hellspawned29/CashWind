package com.cashwind.app

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import com.cashwind.app.database.entity.GoalEntity

class GoalsAdapter(
    context: Context,
    private val goals: MutableList<GoalEntity>,
    private val onAddFunds: (GoalEntity) -> Unit,
    private val onDelete: (GoalEntity) -> Unit
) : ArrayAdapter<GoalEntity>(context, R.layout.item_goal, goals) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_goal, parent, false)

        val goal = goals[position]
        val nameView = view.findViewById<TextView>(R.id.goalName)
        val targetView = view.findViewById<TextView>(R.id.goalTarget)
        val currentView = view.findViewById<TextView>(R.id.goalCurrent)
        val progressBar = view.findViewById<ProgressBar>(R.id.goalProgressBar)
        val percentView = view.findViewById<TextView>(R.id.goalPercent)
        val dateView = view.findViewById<TextView>(R.id.goalTargetDate)
        val addFundsBtn = view.findViewById<Button>(R.id.addFundsButton)
        val deleteBtn = view.findViewById<ImageButton>(R.id.deleteGoalBtn)

        nameView.text = goal.name
        targetView.text = "Target: ${formatCurrency(goal.targetAmount)}"
        currentView.text = "Saved: ${formatCurrency(goal.currentAmount)}"
        dateView.text = "By: ${goal.targetDate}"

        val percent = if (goal.targetAmount > 0) {
            ((goal.currentAmount / goal.targetAmount) * 100).toInt()
        } else 0
        
        progressBar.progress = percent
        progressBar.max = 100
        percentView.text = "$percent%"

        val color = when {
            percent >= 100 -> context.getColor(android.R.color.holo_green_dark)
            percent >= 50 -> context.getColor(android.R.color.holo_blue_dark)
            else -> context.getColor(android.R.color.holo_orange_dark)
        }
        progressBar.progressDrawable.setTint(color)

        addFundsBtn.setOnClickListener { onAddFunds(goal) }
        deleteBtn.setOnClickListener {
            onDelete(goal)
            goals.removeAt(position)
            notifyDataSetChanged()
        }

        return view
    }

    private fun formatCurrency(value: Double): String = "$${String.format("%.2f", value)}"
}
