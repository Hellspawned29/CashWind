package com.cashwind.app

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.cashwind.app.databinding.ActivityAddBillBinding
import com.cashwind.app.ui.AddBillViewModel
import com.cashwind.app.database.CashwindDatabase
import com.cashwind.app.database.entity.BillEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddBillActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddBillBinding
    private val viewModel: AddBillViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                val database = CashwindDatabase.getInstance(this@AddBillActivity)
                return AddBillViewModel(database) as T
            }
        }
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
        existingIsPaid = intent.getBooleanExtra("isPaid", false)

        if (editingBillId != 0) {
            binding.titleText.text = "Edit Bill"
            binding.saveBillButton.text = "Save Changes"
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
        } else {
            binding.titleText.text = "Add Bill"
            binding.saveBillButton.text = "Save Bill"
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().time)
            binding.dueDateInput.setText(today)
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
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val date = sdf.parse(dateText)
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
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                binding.dueDateInput.setText(sdf.format(selectedCalendar.time))
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

        // Validation
        if (name.isEmpty()) {
            Toast.makeText(this, "Bill name required", Toast.LENGTH_SHORT).show()
            return
        }

        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Amount required", Toast.LENGTH_SHORT).show()
            return
        }

        if (dueDate.isEmpty()) {
            Toast.makeText(this, "Due date required", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = try {
            amountStr.toDouble()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
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
            accountId = accountId
        )

        val message = if (editingBillId == 0) "Bill added!" else "Bill updated!"
        viewModel.saveOrUpdate(bill)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        finish()
    }
}
