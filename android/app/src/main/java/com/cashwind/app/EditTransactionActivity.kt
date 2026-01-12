package com.cashwind.app

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.activity.viewModels
import com.google.android.material.snackbar.Snackbar
import com.cashwind.app.database.entity.TransactionEntity
import com.cashwind.app.ui.AccountTransactionViewModel
import com.cashwind.app.util.DateUtils
import java.util.Calendar

class EditTransactionActivity : BaseActivity() {
    private val viewModel: AccountTransactionViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                val accountId = intent.getIntExtra("accountId", -1)
                return AccountTransactionViewModel(database, accountId) as T
            }
        }
    }

    private lateinit var typeSpinner: Spinner
    private lateinit var categorySpinner: Spinner
    private lateinit var descriptionInput: EditText
    private lateinit var amountInput: EditText
    private lateinit var dateInput: EditText
    private lateinit var recurringCheckbox: CheckBox
    private lateinit var frequencySpinner: Spinner
    private lateinit var frequencyLabel: TextView
    private lateinit var transaction: TransactionEntity
    private var selectedDate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_transaction)

        // Get transaction data from intent
        val transactionId = intent.getIntExtra("transactionId", -1)
        val amount = intent.getDoubleExtra("amount", 0.0)
        val type = intent.getStringExtra("type") ?: "expense"
        val category = intent.getStringExtra("category") ?: ""
        val description = intent.getStringExtra("description") ?: ""
        val date = intent.getStringExtra("date") ?: ""
        val isRecurring = intent.getBooleanExtra("isRecurring", false)
        val frequency = intent.getStringExtra("frequency")

        transaction = TransactionEntity(
            id = transactionId,
            userId = 0, // Local only for now
            accountId = intent.getIntExtra("accountId", -1),
            amount = amount,
            type = type,
            category = category,
            description = description,
            date = date,
            isRecurring = isRecurring,
            frequency = frequency
        )

        typeSpinner = findViewById(R.id.typeSpinner)
        categorySpinner = findViewById(R.id.categorySpinner)
        descriptionInput = findViewById(R.id.descriptionInput)
        amountInput = findViewById(R.id.amountInput)
        dateInput = findViewById(R.id.dateInput)
        recurringCheckbox = findViewById(R.id.recurringCheckbox)
        frequencySpinner = findViewById(R.id.frequencySpinner)
        frequencyLabel = findViewById(R.id.frequencyLabel)

        // Pre-fill form with transaction data
        typeSpinner.setSelection(if (type == "income") 0 else 1)
        descriptionInput.setText(description)
        amountInput.setText(amount.toString())
        selectedDate = date
        dateInput.setText(date)
        recurringCheckbox.isChecked = isRecurring

        // Set frequency spinner selection if recurring
        if (isRecurring && frequency != null) {
            val frequencyIndex = resources.getStringArray(R.array.recurrence_frequency).indexOf(frequency)
            if (frequencyIndex >= 0) {
                frequencySpinner.setSelection(frequencyIndex)
            }
        }

        // Setup category spinner based on transaction type
        updateCategorySpinner(typeSpinner, categorySpinner)
        typeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                updateCategorySpinner(typeSpinner, categorySpinner)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Show/hide frequency spinner based on recurring checkbox
        if (isRecurring) {
            frequencySpinner.visibility = android.view.View.VISIBLE
            frequencyLabel.visibility = android.view.View.VISIBLE
            // Set frequency spinner selection if recurring
            if (frequency != null) {
                val frequencyArray = resources.getStringArray(R.array.recurrence_frequency)
                val frequencyIndex = frequencyArray.indexOf(frequency)
                if (frequencyIndex >= 0) {
                    frequencySpinner.setSelection(frequencyIndex)
                }
            }
        } else {
            frequencySpinner.visibility = android.view.View.GONE
            frequencyLabel.visibility = android.view.View.GONE
        }
        
        recurringCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                frequencySpinner.visibility = android.view.View.VISIBLE
                frequencyLabel.visibility = android.view.View.VISIBLE
            } else {
                frequencySpinner.visibility = android.view.View.GONE
                frequencyLabel.visibility = android.view.View.GONE
            }
        }

        dateInput.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, dayOfMonth ->
                val m = (month + 1).toString().padStart(2, '0')
                val d = dayOfMonth.toString().padStart(2, '0')
                selectedDate = "$year-$m-$d"
                dateInput.setText(selectedDate)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        val saveButton = findViewById<Button>(R.id.saveButton)
        val deleteButton = findViewById<com.google.android.material.button.MaterialButton>(R.id.deleteButton)
        val cancelButton = findViewById<Button>(R.id.cancelButton)

        saveButton.setOnClickListener {
            val newAmount = amountInput.text.toString().toDoubleOrNull() ?: 0.0
            val newCategory = categorySpinner.selectedItem?.toString() ?: "Uncategorized"
            val newDescription = descriptionInput.text.toString()
            val newType = when (typeSpinner.selectedItemPosition) {
                0 -> "income"
                else -> "expense"
            }
            val newIsRecurring = recurringCheckbox.isChecked
            val newFrequency = if (newIsRecurring) frequencySpinner.selectedItem?.toString() else null

            if (newAmount > 0 && selectedDate.isNotBlank()) {
                viewModel.updateTransaction(
                    id = transaction.id,
                    amount = newAmount,
                    type = newType,
                    category = newCategory,
                    description = newDescription ?: "",
                    date = selectedDate,
                    isRecurring = newIsRecurring,
                    frequency = newFrequency
                )
                Snackbar.make(findViewById(android.R.id.content), "Transaction updated!", Snackbar.LENGTH_SHORT).show()
                finish()
            } else {
                Snackbar.make(findViewById(android.R.id.content), "Please fill in all fields", Snackbar.LENGTH_SHORT).show()
            }
        }

        deleteButton.setOnClickListener {
            viewModel.deleteTransaction(transaction)
            Snackbar.make(findViewById(android.R.id.content), "Transaction deleted!", Snackbar.LENGTH_SHORT).show()
            finish()
        }

        cancelButton.setOnClickListener { finish() }
    }

    private fun updateCategorySpinner(typeSpinner: Spinner, categorySpinner: Spinner) {
        val isIncome = typeSpinner.selectedItemPosition == 0
        val arrayId = if (isIncome) R.array.income_categories else R.array.expense_categories
        
        ArrayAdapter.createFromResource(
            this,
            arrayId,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categorySpinner.adapter = adapter
        }
    }
}


