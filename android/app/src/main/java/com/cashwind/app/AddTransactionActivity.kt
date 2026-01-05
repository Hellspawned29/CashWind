package com.cashwind.app

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.cashwind.app.database.CashwindDatabase
import com.cashwind.app.ui.AccountTransactionViewModel
import java.util.Calendar

class AddTransactionActivity : AppCompatActivity() {
    private val viewModel: AccountTransactionViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                val accountId = intent.getIntExtra("accountId", -1)
                val db = CashwindDatabase.getInstance(this@AddTransactionActivity)
                return AccountTransactionViewModel(db, accountId) as T
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
    private var selectedDate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        typeSpinner = findViewById(R.id.typeSpinner)
        categorySpinner = findViewById(R.id.categorySpinner)
        descriptionInput = findViewById(R.id.descriptionInput)
        amountInput = findViewById(R.id.amountInput)
        dateInput = findViewById(R.id.dateInput)
        recurringCheckbox = findViewById(R.id.recurringCheckbox)
        frequencySpinner = findViewById(R.id.frequencySpinner)
        frequencyLabel = findViewById(R.id.frequencyLabel)

        // Setup category spinner based on transaction type
        updateCategorySpinner(typeSpinner, categorySpinner)
        typeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                updateCategorySpinner(typeSpinner, categorySpinner)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Show/hide frequency spinner based on recurring checkbox
        recurringCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                frequencySpinner.visibility = android.view.View.VISIBLE
                frequencyLabel.visibility = android.view.View.VISIBLE
            } else {
                frequencySpinner.visibility = android.view.View.GONE
                frequencyLabel.visibility = android.view.View.GONE
            }
        }

        selectedDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(Calendar.getInstance().time)
        dateInput.setText(selectedDate)

        dateInput.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, dayOfMonth ->
                val m = (month + 1).toString().padStart(2, '0')
                val d = dayOfMonth.toString().padStart(2, '0')
                selectedDate = "$year-$m-$d"
                dateInput.setText(selectedDate)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        val addButton = findViewById<Button>(R.id.addButton)
        val cancelButton = findViewById<Button>(R.id.cancelButton)

        addButton.setOnClickListener {
            val amount = amountInput.text.toString().toDoubleOrNull() ?: 0.0
            val category = categorySpinner.selectedItem?.toString() ?: "Uncategorized"
            val description = descriptionInput.text.toString()
            val type = when (typeSpinner.selectedItemPosition) {
                0 -> "income"
                else -> "expense"
            }
            val isRecurring = recurringCheckbox.isChecked
            val frequency = if (isRecurring) frequencySpinner.selectedItem?.toString() else null

            if (amount > 0 && selectedDate.isNotBlank()) {
                viewModel.addTransaction(amount, type, category, description, selectedDate, isRecurring, frequency)
                Toast.makeText(this, "Transaction added!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
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
