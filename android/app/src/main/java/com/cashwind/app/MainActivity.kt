package com.cashwind.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.cashwind.app.databinding.ActivityMainBinding
import com.cashwind.app.ui.MainViewModel
import com.cashwind.app.ui.BillAdapter

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
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
        android.util.Log.d("MainActivity", "onCreate started - BYPASS VIEWBINDING TEST")
        super.onCreate(savedInstanceState)
        android.util.Log.d("MainActivity", "super.onCreate completed")
        
        // TEMPORARILY BYPASS VIEWBINDING - Use setContentView directly
        try {
            android.util.Log.d("MainActivity", "Setting content view from XML...")
            setContentView(R.layout.activity_main)
            android.util.Log.d("MainActivity", "SUCCESS! XML layout inflated without ViewBinding")
            
            // Show success message
            android.widget.Toast.makeText(this, "MainActivity loaded WITHOUT ViewBinding!", android.widget.Toast.LENGTH_LONG).show()
            
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "CRASH during setContentView: ${e.javaClass.simpleName}: ${e.message}", e)
            
            android.app.AlertDialog.Builder(this)
                .setTitle("XML Inflation Error")
                .setMessage("${e.javaClass.simpleName}: ${e.message}\n\n${e.stackTraceToString().take(800)}")
                .setPositiveButton("OK") { _, _ -> finish() }
                .show()
            return
        }
        
        // Old code commented out for testing
        /*
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup RecyclerView
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
        binding.billsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = billAdapter
        }

        // Load bills on startup
        viewModel.loadBills()

        // Observe sorted bills
        viewModel.sortedBills.observe(this) { bills ->
            billAdapter.submitList(bills)
            binding.emptyText.visibility = if (bills.isEmpty()) View.VISIBLE else View.GONE
        }

        // Observe totals
        viewModel.totals.observe(this) { totals ->
            binding.totalCount.text = totals.total.toString()
            binding.paidCount.text = totals.paid.toString()
            binding.unpaidCount.text = totals.unpaid.toString()
            binding.dueWeekCount.text = totals.dueThisWeek.toString()
            binding.dueMonthCount.text = totals.dueThisMonth.toString()

            binding.totalAmount.text = formatCurrency(totals.totalAmount)
            binding.paidAmount.text = formatCurrency(totals.paidAmount)
            binding.unpaidAmount.text = formatCurrency(totals.unpaidAmount)
            binding.dueWeekAmount.text = formatCurrency(totals.dueThisWeekAmount)
            binding.dueMonthAmount.text = formatCurrency(totals.dueThisMonthAmount)
        }

        // Sort spinner
        ArrayAdapter.createFromResource(
            this,
            R.array.sort_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.sortSpinner.adapter = adapter
        }

        binding.sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
        ArrayAdapter.createFromResource(
            this,
            R.array.status_filters,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.statusSpinner.adapter = adapter
        }

        binding.statusSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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

        // Add bill button - navigate to AddBillActivity
        binding.addBillButton.setOnClickListener {
            startActivity(Intent(this, AddBillActivity::class.java))
        }

        // Back button - return to previous activity
        binding.backButton.setOnClickListener {
            finish()
        }
            
            android.util.Log.d("MainActivity", "onCreate completed successfully")
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "CRASH in onCreate: ${e.javaClass.simpleName}: ${e.message}", e)
            e.printStackTrace()
            
            // Show error dialog to user
            android.app.AlertDialog.Builder(this)
                .setTitle("Error Loading Bills")
                .setMessage("${e.javaClass.simpleName}: ${e.message}\n\nStack: ${e.stackTraceToString().take(500)}")
                .setPositiveButton("OK") { _, _ -> finish() }
                .setCancelable(false)
                .show()
        }
        */
    }

    private fun formatCurrency(value: Double): String = "$${String.format("%.2f", value)}"
}