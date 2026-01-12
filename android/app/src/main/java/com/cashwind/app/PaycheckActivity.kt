package com.cashwind.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import com.cashwind.app.ui.PaycheckViewModel

class PaycheckActivity : BaseActivity() {
    private val viewModel: PaycheckViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return PaycheckViewModel(database) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paycheck)

        val upcomingBillsTotal = findViewById<android.widget.TextView>(R.id.upcomingBillsTotal)
        val goalsAllocation = findViewById<android.widget.TextView>(R.id.goalsAllocation)
        val allocationPerPaycheck = findViewById<android.widget.TextView>(R.id.allocationPerPaycheck)
        val allocationPercentage = findViewById<android.widget.TextView>(R.id.allocationPercentage)
        val remainingAfterBills = findViewById<android.widget.TextView>(R.id.remainingAfterBills)
        val monthlyBillsPerPaycheck = findViewById<android.widget.TextView>(R.id.monthlyBillsPerPaycheck)
        val pastDueBillsTotal = findViewById<android.widget.TextView>(R.id.pastDueBillsTotal)
        val pastDueBillsCount = findViewById<android.widget.TextView>(R.id.pastDueBillsCount)
        val pastDueSuggestion = findViewById<android.widget.TextView>(R.id.pastDueSuggestion)
        val viewPastDueButton = findViewById<android.widget.Button>(R.id.viewPastDueButton)
        val pastDueBalanceTotal = findViewById<android.widget.TextView>(R.id.pastDueBalanceTotal)
        val pastDueBalanceCount = findViewById<android.widget.TextView>(R.id.pastDueBalanceCount)
        val managePastDueBalanceButton = findViewById<android.widget.Button>(R.id.managePastDueBalanceButton)
        val pastDueBalanceSection = findViewById<android.widget.LinearLayout>(R.id.pastDueBalanceSection)
        val editIncomeButton = findViewById<com.google.android.material.button.MaterialButton>(R.id.editIncomeButton)
        val backButton = findViewById<android.widget.Button>(R.id.backButton)

        // Observe monthly bills per paycheck
        viewModel.monthlyBillsPerPaycheck.observe(this) { amount ->
            monthlyBillsPerPaycheck.text = formatCurrency(amount)
        }

        // Observe upcoming bills total
        viewModel.upcomingBillsTotal.observe(this) { total ->
            upcomingBillsTotal.text = formatCurrency(total)
        }

        // Observe goals allocation
        viewModel.goalsAllocation.observe(this) { total ->
            goalsAllocation.text = formatCurrency(total)
        }

        // Observe allocation per paycheck
        viewModel.allocationPerPaycheck.observe(this) { allocation ->
            allocationPerPaycheck.text = formatCurrency(allocation)
        }

        // Observe remaining after bills
        viewModel.remainingAfterBills.observe(this) { remaining ->
            val color = if (remaining >= 0) android.R.color.holo_green_dark else android.R.color.holo_red_dark
            remainingAfterBills.setTextColor(getColor(color))
            remainingAfterBills.text = formatCurrency(remaining)
        }

        // Observe past due bills
        viewModel.pastDueBillsTotal.observe(this) { total ->
            pastDueBillsTotal.text = formatCurrency(total)
        }

        viewModel.pastDueBillsCount.observe(this) { count ->
            pastDueBillsCount.text = "$count bill${if (count == 1) "" else "s"} overdue"
            // Show suggestion if there are overdue bills and paycheck can cover some
            val hasPastDue = count > 0
            pastDueSuggestion.visibility = if (hasPastDue) android.view.View.VISIBLE else android.view.View.GONE
        }

        // Observe past due balances
        viewModel.pastDueBalanceTotal.observe(this) { total ->
            pastDueBalanceTotal.text = formatCurrency(total)
            // Hide section if no past due balances
            pastDueBalanceSection.visibility = if (total > 0) android.view.View.VISIBLE else android.view.View.GONE
        }

        viewModel.pastDueBalanceCount.observe(this) { count ->
            pastDueBalanceCount.text = "$count bill${if (count == 1) "" else "s"} have past due balance${if (count == 1) "" else "s"}"
            pastDueBalanceCount.visibility = if (count > 0) android.view.View.VISIBLE else android.view.View.GONE
        }

        // View past due button - opens PastDueBillsActivity
        viewPastDueButton.setOnClickListener {
            startActivity(Intent(this, PastDueBillsActivity::class.java))
        }

        // Manage past due balances - opens dialog to allocate to past due balance bills
        managePastDueBalanceButton.setOnClickListener {
            // Show dialog to select which past due balance bill(s) to allocate to
            showPastDueBalanceAllocationDialog()
        }

        // Edit income button - opens Accounts activity
        editIncomeButton.setOnClickListener {
            startActivity(Intent(this, AccountsActivity::class.java))
        }

        // Back button
        backButton.setOnClickListener { finish() }
    }

    private fun showPastDueBalanceAllocationDialog() {
        viewModel.billsWithPastDueBalance.observe(this) { bills ->
            if (bills.isEmpty()) {
                Toast.makeText(this, "No bills with past due balances", Toast.LENGTH_SHORT).show()
                return@observe
            }
            
            val billNames = bills.map { it.name }.toTypedArray()
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Select Bill to Allocate To")
                .setItems(billNames) { _, which ->
                    val selectedBill = bills[which]
                    showAllocationAmountDialog(selectedBill)
                }
                .show()
        }
    }

    private fun showAllocationAmountDialog(bill: com.cashwind.app.model.Bill) {
        val input = android.widget.EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            hint = "Amount (max: $${String.format("%.2f", bill.pastDueAmount)})"
        }
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Allocate to ${bill.name} Past Due")
            .setView(input)
            .setPositiveButton("Allocate") { _, _ ->
                val amountStr = input.text.toString()
                if (amountStr.isNotEmpty()) {
                    val amount = amountStr.toDoubleOrNull() ?: 0.0
                    if (amount > 0 && amount <= bill.pastDueAmount) {
                        // Create allocation record
                        viewModel.allocateToPastDueBalance(bill.id, amount)
                        Toast.makeText(this, "Allocated $${String.format("%.2f", amount)} to ${bill.name}", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun formatCurrency(value: Double): String = "$${String.format("%.2f", value)}"
}
