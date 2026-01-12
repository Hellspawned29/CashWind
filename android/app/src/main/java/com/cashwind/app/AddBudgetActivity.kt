package com.cashwind.app

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import com.google.android.material.snackbar.Snackbar
import com.cashwind.app.databinding.ActivityAddBudgetBinding
import com.cashwind.app.ui.BudgetViewModel

class AddBudgetActivity : BaseActivity() {
    private lateinit var binding: ActivityAddBudgetBinding
    private val viewModel: BudgetViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return BudgetViewModel(database) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBudgetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Period spinner setup
        ArrayAdapter.createFromResource(
            this,
            R.array.budget_periods,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.budgetPeriodSpinner.adapter = adapter
        }

        binding.saveBudgetButton.setOnClickListener { saveBudget() }
        binding.cancelButton.setOnClickListener { finish() }
    }

    private fun saveBudget() {
        val name = binding.budgetNameInput.text.toString().trim()
        val amountStr = binding.budgetAmountInput.text.toString().trim()
        val category = binding.budgetCategoryInput.text.toString().trim()
        val period = when (binding.budgetPeriodSpinner.selectedItemPosition) {
            0 -> "weekly"
            1 -> "monthly"
            else -> "yearly"
        }

        if (name.isEmpty()) {
            Snackbar.make(binding.root, "Budget name required", Snackbar.LENGTH_SHORT).show()
            return
        }

        if (amountStr.isEmpty()) {
            Snackbar.make(binding.root, "Amount required", Snackbar.LENGTH_SHORT).show()
            return
        }

        val amount = try {
            amountStr.toDouble()
        } catch (e: NumberFormatException) {
            Snackbar.make(binding.root, "Invalid amount", Snackbar.LENGTH_SHORT).show()
            return
        }

        if (amount <= 0) {
            Snackbar.make(binding.root, "Amount must be greater than 0", Snackbar.LENGTH_SHORT).show()
            return
        }

        val finalCategory = category.ifBlank { "General" }
        viewModel.addBudget(name, amount, period, finalCategory)
        Snackbar.make(binding.root, "Budget created!", Snackbar.LENGTH_SHORT).show()
        finish()
    }
}
