package com.cashwind.app

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.cashwind.app.database.CashwindDatabase
import com.cashwind.app.ui.BudgetViewModel

class BudgetActivity : AppCompatActivity() {
    private val viewModel: BudgetViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                val db = CashwindDatabase.getInstance(this@BudgetActivity)
                return BudgetViewModel(db) as T
            }
        }
    }

    private lateinit var budgetListView: ListView
    private lateinit var addBudgetButton: Button
    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget)

        budgetListView = findViewById(R.id.budgetListView)
        addBudgetButton = findViewById(R.id.addBudgetButton)
        backButton = findViewById(R.id.backButton)

        viewModel.budgetsWithSpent.observe(this) { budgets ->
            val adapter = BudgetAdapter(this, budgets.toMutableList()) { budget ->
                AlertDialog.Builder(this)
                    .setMessage("Delete budget: ${budget.budget.name}?")
                    .setPositiveButton("Delete") { _, _ ->
                        viewModel.deleteBudget(budget.budget)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
            budgetListView.adapter = adapter
        }

        addBudgetButton.setOnClickListener { showAddBudgetDialog() }
        backButton.setOnClickListener { finish() }
    }

    private fun showAddBudgetDialog() {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_add_budget, null)

        val nameInput = view.findViewById<EditText>(R.id.budgetNameInput)
        val amountInput = view.findViewById<EditText>(R.id.budgetAmountInput)
        val categoryInput = view.findViewById<EditText>(R.id.budgetCategoryInput)
        val periodSpinner = view.findViewById<android.widget.Spinner>(R.id.budgetPeriodSpinner)

        builder.setView(view)
            .setTitle("Add Budget")
            .setPositiveButton("Add") { _, _ ->
                val name = nameInput.text.toString().ifBlank { "Budget" }
                val amount = amountInput.text.toString().toDoubleOrNull() ?: 0.0
                val category = categoryInput.text.toString().ifBlank { "General" }
                val period = when (periodSpinner.selectedItemPosition) {
                    0 -> "weekly"
                    1 -> "monthly"
                    else -> "yearly"
                }

                if (amount > 0) {
                    viewModel.addBudget(name, amount, period, category)
                    Toast.makeText(this, "Budget created!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
