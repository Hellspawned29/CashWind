package com.cashwind.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
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
        val emptyText = findViewById<TextView>(R.id.emptyText)

        viewModel.budgetsWithSpent.observe(this) { budgets ->
            emptyText.visibility = if (budgets.isEmpty()) View.VISIBLE else View.GONE
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

        addBudgetButton.setOnClickListener {
            val intent = Intent(this, AddBudgetActivity::class.java)
            startActivity(intent)
        }
        backButton.setOnClickListener { finish() }
    }
}


