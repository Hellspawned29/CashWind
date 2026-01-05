package com.cashwind.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.cashwind.app.database.CashwindDatabase
import com.cashwind.app.ui.PaycheckViewModel

class PaycheckActivity : AppCompatActivity() {
    private val viewModel: PaycheckViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                val db = CashwindDatabase.getInstance(this@PaycheckActivity)
                return PaycheckViewModel(db) as T
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
        val editIncomeButton = findViewById<android.widget.Button>(R.id.editIncomeButton)
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

        // Edit income button - opens Accounts activity
        editIncomeButton.setOnClickListener {
            startActivity(Intent(this, AccountsActivity::class.java))
        }

        // Back button
        backButton.setOnClickListener { finish() }
    }

    private fun formatCurrency(value: Double): String = "$${String.format("%.2f", value)}"
}
