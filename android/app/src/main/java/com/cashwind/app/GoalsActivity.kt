package com.cashwind.app

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import com.cashwind.app.database.CashwindDatabase
import com.cashwind.app.database.entity.GoalEntity
import com.cashwind.app.ui.GoalsViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class GoalsActivity : AppCompatActivity() {
    private val viewModel: GoalsViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                val db = CashwindDatabase.getInstance(this@GoalsActivity)
                return GoalsViewModel(db) as T
            }
        }
    }

    private lateinit var goalsListView: ListView
    private lateinit var addGoalButton: Button
    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goals)

        goalsListView = findViewById(R.id.goalsListView)
        addGoalButton = findViewById(R.id.addGoalButton)
        backButton = findViewById(R.id.backButton)
        val emptyText = findViewById<TextView>(R.id.emptyText)

        viewModel.goals.observe(this) { goals ->
            emptyText.visibility = if (goals.isEmpty()) View.VISIBLE else View.GONE
            val adapter = GoalsAdapter(
                this,
                goals.toMutableList(),
                onAddFunds = { goal -> showAddFundsDialog(goal) },
                onDelete = { goal ->
                    AlertDialog.Builder(this)
                        .setMessage("Delete goal: ${goal.name}?")
                        .setPositiveButton("Delete") { _, _ ->
                            viewModel.deleteGoal(goal)
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            )
            goalsListView.adapter = adapter
        }

        addGoalButton.setOnClickListener { showAddGoalDialog() }
        backButton.setOnClickListener { finish() }
    }

    private fun showAddGoalDialog() {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_add_goal, null)

        val nameInput = view.findViewById<EditText>(R.id.goalNameInput)
        val targetAmountInput = view.findViewById<EditText>(R.id.goalTargetAmountInput)
        val targetDateInput = view.findViewById<EditText>(R.id.goalTargetDateInput)
        val monthlyInput = view.findViewById<EditText>(R.id.goalMonthlyInput)
        val categoryInput = view.findViewById<EditText>(R.id.goalCategoryInput)
        val notesInput = view.findViewById<EditText>(R.id.goalNotesInput)

        val defaultDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(
            Calendar.getInstance().apply { add(Calendar.YEAR, 1) }.time
        )
        targetDateInput.setText(defaultDate)

        builder.setView(view)
            .setTitle("Add Goal")
            .setPositiveButton("Add") { _, _ ->
                val name = nameInput.text.toString().ifBlank { "New Goal" }
                val targetAmount = targetAmountInput.text.toString().toDoubleOrNull() ?: 0.0
                val targetDate = targetDateInput.text.toString()
                val monthly = monthlyInput.text.toString().toDoubleOrNull()
                val category = categoryInput.text.toString().takeIf { it.isNotBlank() }
                val notes = notesInput.text.toString().takeIf { it.isNotBlank() }

                if (targetAmount > 0) {
                    viewModel.addGoal(name, "savings", targetAmount, targetDate, monthly, category, notes)
                    Snackbar.make(findViewById(android.R.id.content), "Goal created!", Snackbar.LENGTH_SHORT).show()
                } else {
                    Snackbar.make(findViewById(android.R.id.content), "Please enter a target amount", Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddFundsDialog(goal: GoalEntity) {
        val builder = AlertDialog.Builder(this)
        val input = EditText(this).apply {
            hint = "Amount to add"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        builder.setView(input)
            .setTitle("Add to ${goal.name}")
            .setPositiveButton("Add") { _, _ ->
                val amount = input.text.toString().toDoubleOrNull() ?: 0.0
                if (amount > 0) {
                    viewModel.updateProgress(goal, amount)
                    Snackbar.make(findViewById(android.R.id.content), "Added $${String.format("%.2f", amount)}", Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}


