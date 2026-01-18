package com.cashwind.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cashwind.app.ui.MainViewModel
import com.cashwind.app.ui.BillAdapter

class MainActivity : BaseActivity() {
    private val viewModel: MainViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(database) as T
            }
        }
    }
    private lateinit var billAdapter: BillAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Setup RecyclerView
        val billsRecyclerView = findViewById<RecyclerView>(R.id.billsRecyclerView)
        val emptyText = findViewById<TextView>(R.id.emptyText)
        
        billAdapter = BillAdapter(
            onTogglePaid = { bill -> viewModel.togglePaid(bill) },
            onDelete = { bill -> viewModel.deleteBill(bill) },
            onDetail = { bill ->
                val intent = Intent(this, BillDetailActivity::class.java).apply {
                    putExtra("id", bill.id)
                    putExtra("userId", bill.userId)
                    putExtra("name", bill.name)
                    putExtra("amount", bill.amount)
                    putExtra("dueDate", bill.dueDate)
                    putExtra("category", bill.category)
                    putExtra("notes", bill.notes)
                    putExtra("webLink", bill.webLink)
                    putExtra("isPaid", bill.isPaid)
                    putExtra("lastPaidAt", bill.lastPaidAt)
                    putExtra("recurring", bill.recurring)
                    putExtra("frequency", bill.frequency)
                    putExtra("hasPastDue", bill.hasPastDue)
                    putExtra("pastDueAmount", bill.pastDueAmount)
                }
                startActivity(intent)
            }
        )
        billsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = billAdapter
        }

        // Load bills on startup
        viewModel.loadBills()

        // Observe sorted bills
        viewModel.sortedBills.observe(this) { bills ->
            billAdapter.submitList(bills)
            emptyText.visibility = if (bills.isEmpty()) View.VISIBLE else View.GONE
        }

        // Observe totals
        val totalCount = findViewById<TextView>(R.id.totalCount)
        val paidCount = findViewById<TextView>(R.id.paidCount)
        val unpaidCount = findViewById<TextView>(R.id.unpaidCount)
        val dueWeekCount = findViewById<TextView>(R.id.dueWeekCount)
        val dueMonthCount = findViewById<TextView>(R.id.dueMonthCount)
        val totalAmount = findViewById<TextView>(R.id.totalAmount)
        val paidAmount = findViewById<TextView>(R.id.paidAmount)
        val unpaidAmount = findViewById<TextView>(R.id.unpaidAmount)
        val dueWeekAmount = findViewById<TextView>(R.id.dueWeekAmount)
        val dueMonthAmount = findViewById<TextView>(R.id.dueMonthAmount)
        
        viewModel.totals.observe(this) { totals ->
            totalCount.text = totals.total.toString()
            paidCount.text = totals.paid.toString()
            unpaidCount.text = totals.unpaid.toString()
            dueWeekCount.text = totals.dueThisWeek.toString()
            dueMonthCount.text = totals.dueThisMonth.toString()

            totalAmount.text = formatCurrency(totals.totalAmount)
            paidAmount.text = formatCurrency(totals.paidAmount)
            unpaidAmount.text = formatCurrency(totals.unpaidAmount)
            dueWeekAmount.text = formatCurrency(totals.dueThisWeekAmount)
            dueMonthAmount.text = formatCurrency(totals.dueThisMonthAmount)
        }

        // Sort spinner
        val sortSpinner = findViewById<Spinner>(R.id.sortSpinner)
        ArrayAdapter.createFromResource(
            this,
            R.array.sort_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            sortSpinner.adapter = adapter
        }

        sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val option = when (position) {
                    1 -> MainViewModel.SortOption.AMOUNT_DESC
                    2 -> MainViewModel.SortOption.NAME
                    else -> MainViewModel.SortOption.DUE_DATE
                }
                viewModel.updateSort(option)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Status filter spinner
        val statusSpinner = findViewById<Spinner>(R.id.statusSpinner)
        ArrayAdapter.createFromResource(
            this,
            R.array.status_filters,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            statusSpinner.adapter = adapter
        }

        statusSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val filter = when (position) {
                    1 -> MainViewModel.StatusFilter.PAID
                    2 -> MainViewModel.StatusFilter.UNPAID
                    else -> MainViewModel.StatusFilter.ALL
                }
                viewModel.updateStatusFilter(filter)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Add bill button
        findViewById<Button>(R.id.addBillButton).setOnClickListener {
            startActivity(Intent(this, AddBillActivity::class.java))
        }

        // Back button
        findViewById<Button>(R.id.backButton).setOnClickListener {
            finish()
        }
    }

    private fun formatCurrency(value: Double): String = "$${String.format("%.2f", value)}"
}
