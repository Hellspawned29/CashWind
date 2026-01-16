package com.cashwind.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import com.cashwind.app.database.entity.GoalEntity
import com.cashwind.app.ui.GoalsViewModel

class GoalsActivity : BaseActivity() {
    private val viewModel: GoalsViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return GoalsViewModel(database) as T
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

        addGoalButton.setOnClickListener {
            val intent = Intent(this, AddGoalActivity::class.java)
            startActivity(intent)
        }
        backButton.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
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


