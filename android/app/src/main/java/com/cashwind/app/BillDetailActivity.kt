package com.cashwind.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.Spinner
import androidx.activity.viewModels
import com.google.android.material.snackbar.Snackbar
import com.cashwind.app.databinding.ActivityBillDetailBinding
import com.cashwind.app.ui.BillDetailViewModel
import com.cashwind.app.database.entity.BillReminderEntity
import com.cashwind.app.model.Bill
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class BillDetailActivity : BaseActivity() {
    private lateinit var binding: ActivityBillDetailBinding
    private val viewModel: BillDetailViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return BillDetailViewModel(database) as T
            }
        }
    }

    private lateinit var bill: Bill
    private val reminderDao by lazy { database.billReminderDao() }
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
            lastPaidAt = intent.getStringExtra("lastPaidAt"),
            category = intent.getStringExtra("category"),
            recurring = intent.getBooleanExtra("recurring", false),
            frequency = intent.getStringExtra("frequency"),
            notes = intent.getStringExtra("notes"),
            webLink = intent.getStringExtra("webLink"),
            hasPastDue = intent.getBooleanExtra("hasPastDue", false),
            pastDueAmount = intent.getDoubleExtra("pastDueAmount", 0.0),
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
                putExtra("hasPastDue", bill.hasPastDue)
                putExtra("pastDueAmount", bill.pastDueAmount)
            }
            startActivity(intent)
        }

        binding.togglePaidButton.setOnClickListener {
            viewModel.togglePaid(bill) {
                bill = it
                bindBill()
                Snackbar.make(binding.root, if (bill.isPaid) "Marked paid" else "Marked unpaid", Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.deleteButton.setOnClickListener {
            viewModel.delete(bill) {
                Snackbar.make(binding.root, "Bill deleted", Snackbar.LENGTH_SHORT).show()
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
                        Snackbar.make(binding.root, "Invalid URL", Snackbar.LENGTH_SHORT).show()
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
                Snackbar.make(binding.root, "Reminder saved!", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun bindBill() {
        binding.billName.text = bill.name
        val totalAmount = bill.amount + bill.pastDueAmount
        binding.billAmount.text = "$${String.format("%.2f", totalAmount)}"
        
        // Show breakdown if there's a past due amount
        if (bill.hasPastDue && bill.pastDueAmount > 0.0) {
            binding.billAmount.text = "$${String.format("%.2f", bill.amount)} + Past Due: $${String.format("%.2f", bill.pastDueAmount)}"
        } else {
            binding.billAmount.text = "$${String.format("%.2f", bill.amount)}"
        }
        
        binding.billDue.text = bill.dueDate
        binding.billStatus.text = if (bill.isPaid) "Paid" else "Unpaid"
        binding.billLastPaid.text = bill.lastPaidAt?.let { "Last paid: $it" } ?: ""
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