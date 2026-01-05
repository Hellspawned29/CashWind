package com.cashwind.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.cashwind.app.databinding.ActivityBillDetailBinding
import com.cashwind.app.ui.BillDetailViewModel
import com.cashwind.app.database.CashwindDatabase
import com.cashwind.app.database.entity.BillReminderEntity
import com.cashwind.app.model.Bill
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class BillDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBillDetailBinding
    private val viewModel: BillDetailViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                val database = CashwindDatabase.getInstance(this@BillDetailActivity)
                return BillDetailViewModel(database) as T
            }
        }
    }

    private lateinit var bill: Bill
    private val reminderDao by lazy { CashwindDatabase.getInstance(this).billReminderDao() }
    private var currentReminder: BillReminderEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBillDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bill = Bill(
            id = intent.getIntExtra("id", 0),
            userId = intent.getIntExtra("userId", 1),
            name = intent.getStringExtra("name") ?: "",
            amount = intent.getDoubleExtra("amount", 0.0),
            dueDate = intent.getStringExtra("dueDate") ?: "",
            isPaid = intent.getBooleanExtra("isPaid", false),
            category = intent.getStringExtra("category"),
            recurring = intent.getBooleanExtra("recurring", false),
            frequency = intent.getStringExtra("frequency"),
            notes = intent.getStringExtra("notes"),
            webLink = intent.getStringExtra("webLink"),
            createdAt = null
        )

        bindBill()
        loadReminder()

        binding.editButton.setOnClickListener {
            val intent = Intent(this, AddBillActivity::class.java).apply {
                putExtra("id", bill.id)
                putExtra("userId", bill.userId)
                putExtra("name", bill.name)
                putExtra("amount", bill.amount)
                putExtra("dueDate", bill.dueDate)
                putExtra("category", bill.category)
                putExtra("notes", bill.notes)
                putExtra("isPaid", bill.isPaid)
                putExtra("recurring", bill.recurring)
                putExtra("frequency", bill.frequency)
                putExtra("webLink", bill.webLink)
            }
            startActivity(intent)
        }

        binding.togglePaidButton.setOnClickListener {
            viewModel.togglePaid(bill) {
                bill = it
                bindBill()
                Toast.makeText(this, if (bill.isPaid) "Marked paid" else "Marked unpaid", Toast.LENGTH_SHORT).show()
            }
        }

        binding.deleteButton.setOnClickListener {
            viewModel.delete(bill) {
                Toast.makeText(this, "Bill deleted", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        binding.closeButton.setOnClickListener { finish() }

        // Web link
        binding.openLinkButton.setOnClickListener {
            bill.webLink?.let { url ->
                if (url.isNotBlank()) {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(this, "Invalid URL", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // 
        // Reminder settings
        binding.saveReminderButton.setOnClickListener { saveReminder() }
        binding.reminderEnabledCheckbox.setOnCheckedChangeListener { _, _ -> updateReminderUI() }
    }

    private fun loadReminder() {
        GlobalScope.launch {
            val reminder = reminderDao.getReminderForBill(bill.id)
            runOnUiThread {
                currentReminder = reminder
                if (reminder != null) {
                    binding.reminderEnabledCheckbox.isChecked = reminder.isEnabled
                    val daysOptions = resources.getStringArray(R.array.reminder_days)
                    // Match against the number part of strings like "0 (Day of)" or "1"
                    val selectedIndex = daysOptions.indexOfFirst { 
                        it.split(" ").firstOrNull()?.toIntOrNull() == reminder.daysBeforeDue 
                    }
                    if (selectedIndex >= 0) {
                        binding.reminderDaysSpinner.setSelection(selectedIndex)
                    }
                } else {
                    binding.reminderEnabledCheckbox.isChecked = false
                    binding.reminderDaysSpinner.setSelection(0)
                }
                updateReminderUI()
            }
        }
    }

    private fun updateReminderUI() {
        val isEnabled = binding.reminderEnabledCheckbox.isChecked
        binding.reminderDaysSpinner.isEnabled = isEnabled
    }

    private fun saveReminder() {
        val isEnabled = binding.reminderEnabledCheckbox.isChecked
        val daysStr = binding.reminderDaysSpinner.selectedItem.toString()
        // Extract just the number from strings like "0 (Day of)" or "1"
        val days = daysStr.split(" ").firstOrNull()?.toIntOrNull() ?: 1

        GlobalScope.launch {
            if (currentReminder == null) {
                val newReminder = BillReminderEntity(
                    id = (System.currentTimeMillis() / 1000).toInt(),
                    billId = bill.id,
                    daysBeforeDue = days,
                    isEnabled = isEnabled
                )
                reminderDao.insertReminder(newReminder)
                currentReminder = newReminder
            } else {
                val updated = currentReminder!!.copy(daysBeforeDue = days, isEnabled = isEnabled)
                reminderDao.updateReminder(updated)
                currentReminder = updated
            }
            runOnUiThread {
                Toast.makeText(this@BillDetailActivity, "Reminder saved!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun bindBill() {
        binding.billName.text = bill.name
        binding.billAmount.text = "$${String.format("%.2f", bill.amount)}"
        binding.billDue.text = bill.dueDate
        binding.billStatus.text = if (bill.isPaid) "Paid" else "Unpaid"
        binding.billCategory.text = bill.category ?: "General"
        binding.billNotes.text = bill.notes ?: "No notes"
        binding.togglePaidButton.text = if (bill.isPaid) "Mark Unpaid" else "Mark Paid"
        val recurrenceText = if (bill.recurring) {
            "Recurring: ${bill.frequency ?: "unspecified"}"
        } else {
            "One-time"
        }
        binding.billRecurrence.text = recurrenceText
        
        // Show web link section if link exists
        if (!bill.webLink.isNullOrBlank()) {
            binding.webLinkSection.visibility = View.VISIBLE
            binding.billWebLink.text = bill.webLink
        } else {
            binding.webLinkSection.visibility = View.GONE
        }
    }
}