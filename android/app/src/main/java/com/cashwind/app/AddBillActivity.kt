package com.cashwind.app

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import com.google.android.material.snackbar.Snackbar
import com.cashwind.app.databinding.ActivityAddBillBinding
import com.cashwind.app.ui.AddBillViewModel
import com.cashwind.app.database.entity.BillEntity
import com.cashwind.app.util.DateUtils
import java.util.Calendar

class AddBillActivity : BaseActivity() {
    private lateinit var binding: ActivityAddBillBinding
    private val viewModel: AddBillViewModel by lazy {
        androidx.lifecycle.ViewModelProvider(
            this,
            object : androidx.lifecycle.ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return AddBillViewModel(database) as T
                }
            }
        )[AddBillViewModel::class.java]
    }
    private var editingBillId: Int = 0
    private var existingIsPaid: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBillBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Category spinner setup
        ArrayAdapter.createFromResource(
            this,
            R.array.bill_categories,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.categorySpinner.adapter = adapter
        }

        // Recurrence spinner setup
        ArrayAdapter.createFromResource(
            this,
            R.array.recurrence_frequency,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.frequencySpinner.adapter = adapter
        }

        // Load accounts for payment account spinner
        viewModel.allAccounts.observe(this) { accounts ->
            val accountNames = accounts.map { it.name }.toTypedArray()
            ArrayAdapter(this, android.R.layout.simple_spinner_item, accountNames).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.accountSpinner.adapter = adapter
            }
        }

        binding.recurringSwitch.setOnCheckedChangeListener { _, isChecked ->
            binding.frequencySpinner.isEnabled = isChecked
        }

        binding.hasPastDueCheckbox.setOnCheckedChangeListener { _, isChecked ->
            binding.pastDueSection.visibility = if (isChecked) android.view.View.VISIBLE else android.view.View.GONE
        }

        // Date picker button
        binding.datePickerButton.setOnClickListener {
            showDatePicker()
        }

        // Determine if editing
        editingBillId = intent.getIntExtra("id", 0)
        val existingName = intent.getStringExtra("name")
        val existingAmount = intent.getDoubleExtra("amount", 0.0)
        val existingDueDate = intent.getStringExtra("dueDate")
        val existingCategory = intent.getStringExtra("category")
        val existingNotes = intent.getStringExtra("notes")
        val existingWebLink = intent.getStringExtra("webLink")
        val existingRecurring = intent.getBooleanExtra("recurring", false)
        val existingFrequency = intent.getStringExtra("frequency")
        val existingHasPastDue = intent.getBooleanExtra("hasPastDue", false)
        val existingPastDueAmount = intent.getDoubleExtra("pastDueAmount", 0.0)
        existingIsPaid = intent.getBooleanExtra("isPaid", false)

        if (editingBillId != 0) {
            binding.titleText.text = "Edit Bill"
            binding.saveBillButton.text = "SAVE"
            binding.nameInput.setText(existingName ?: "")
            binding.amountInput.setText(if (existingAmount == 0.0) "" else existingAmount.toString())
            binding.dueDateInput.setText(existingDueDate ?: "")
            existingCategory?.let { category ->
                val position = resources.getStringArray(R.array.bill_categories).indexOf(category)
                if (position >= 0) binding.categorySpinner.setSelection(position)
            }
            binding.notesInput.setText(existingNotes ?: "")
            binding.webLinkInput.setText(existingWebLink ?: "")
            binding.recurringSwitch.isChecked = existingRecurring
            binding.frequencySpinner.isEnabled = existingRecurring
            existingFrequency?.let { freq ->
                val position = resources.getStringArray(R.array.recurrence_frequency).indexOf(freq)
                if (position >= 0) binding.frequencySpinner.setSelection(position)
            }
            binding.hasPastDueCheckbox.isChecked = existingHasPastDue
            if (existingHasPastDue && existingPastDueAmount > 0.0) {
                binding.pastDueAmountInput.setText(existingPastDueAmount.toString())
                binding.pastDueSection.visibility = android.view.View.VISIBLE
            }
        } else {
            binding.titleText.text = "Add Bill"
            binding.saveBillButton.text = "SAVE"
            binding.dueDateInput.setText(DateUtils.getCurrentIsoDate())
            binding.recurringSwitch.isChecked = false
            binding.frequencySpinner.isEnabled = false
        }

        // Save button
        binding.saveBillButton.setOnClickListener {
            saveBill()
        }

        // Cancel button
        binding.cancelButton.setOnClickListener {
            finish()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val dateText = binding.dueDateInput.text.toString()
        
        if (dateText.isNotEmpty()) {
            try {
                val date = DateUtils.parseIsoDate(dateText)
                if (date != null) calendar.time = date
            } catch (e: Exception) {
                // Use today's date if parsing fails
            }
        }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(selectedYear, selectedMonth, selectedDay)
                binding.dueDateInput.setText(DateUtils.formatIsoDate(selectedCalendar.time))
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun saveBill() {
        val name = binding.nameInput.text.toString().trim()
        val amountStr = binding.amountInput.text.toString().trim()
        val dueDate = binding.dueDateInput.text.toString().trim()
        val category = binding.categorySpinner.selectedItem?.toString() ?: ""
        val notes = binding.notesInput.text.toString().trim()
        val webLink = binding.webLinkInput.text.toString().trim()
        val recurring = binding.recurringSwitch.isChecked
        val frequency = if (recurring) binding.frequencySpinner.selectedItem?.toString() else null
        val hasPastDue = binding.hasPastDueCheckbox.isChecked
        val pastDueAmountStr = binding.pastDueAmountInput.text.toString().trim()
        val pastDueAmount = if (hasPastDue && pastDueAmountStr.isNotEmpty()) {
            pastDueAmountStr.toDoubleOrNull() ?: 0.0
        } else {
            0.0
        }

        // Validation
        if (name.isEmpty()) {
            Snackbar.make(binding.root, "Bill name required", Snackbar.LENGTH_SHORT).show()
            return
        }

        if (amountStr.isEmpty()) {
            Snackbar.make(binding.root, "Amount required", Snackbar.LENGTH_SHORT).show()
            return
        }

        if (dueDate.isEmpty()) {
            Snackbar.make(binding.root, "Due date required", Snackbar.LENGTH_SHORT).show()
            return
        }

        // Validate date format
        if (DateUtils.parseIsoDate(dueDate) == null) {
            Snackbar.make(binding.root, "Invalid date format (yyyy-MM-dd)", Snackbar.LENGTH_SHORT).show()
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

        // Create bill entity and save/update
        val selectedAccountIndex = binding.accountSpinner.selectedItemPosition
        val accounts = viewModel.allAccounts.value ?: emptyList()
        val accountId = if (selectedAccountIndex >= 0 && selectedAccountIndex < accounts.size) {
            accounts[selectedAccountIndex].id
        } else {
            null
        }

        val bill = BillEntity(
            id = editingBillId,
            userId = 1, // Default user (no auth)
            name = name,
            amount = amount,
            dueDate = dueDate,
            isPaid = if (editingBillId != 0) existingIsPaid else false,
            category = if (category.isEmpty()) null else category,
            webLink = if (webLink.isEmpty()) null else webLink,
            notes = if (notes.isEmpty()) null else notes,
            recurring = recurring,
            frequency = frequency,
            accountId = accountId,
            hasPastDue = hasPastDue,
            pastDueAmount = pastDueAmount
        )

        val message = if (editingBillId == 0) "Bill added!" else "Bill updated!"
        viewModel.saveOrUpdate(bill)
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
        finish()
    }
}
