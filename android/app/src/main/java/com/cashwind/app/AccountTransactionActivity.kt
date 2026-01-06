package com.cashwind.app

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.cashwind.app.database.CashwindDatabase
import com.cashwind.app.database.entity.TransactionEntity
import com.cashwind.app.ui.AccountTransactionViewModel
import java.util.Calendar

class AccountTransactionActivity : AppCompatActivity() {
    private val viewModel: AccountTransactionViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                val accountId = intent.getIntExtra("accountId", -1)
                val db = CashwindDatabase.getInstance(this@AccountTransactionActivity)
                return AccountTransactionViewModel(db, accountId) as T
            }
        }
    }

    private lateinit var transactionListView: ListView
    private lateinit var accountNameView: TextView
    private lateinit var accountBalanceView: TextView
    private lateinit var addTransactionButton: Button
    private lateinit var backButton: Button
    private var selectedDate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_transaction)

        transactionListView = findViewById(R.id.transactionListView)
        accountNameView = findViewById(R.id.accountNameView)
        accountBalanceView = findViewById(R.id.accountBalanceView)
        addTransactionButton = findViewById(R.id.addTransactionButton)
        backButton = findViewById(R.id.backButton)

        Log.d("AccountTransactionActivity", "addTransactionButton is null: ${addTransactionButton == null}")
        Log.d("AccountTransactionActivity", "backButton is null: ${backButton == null}")

        // Observe account info
        viewModel.account.observe(this) { account ->
            if (account != null) {
                accountNameView.text = account.name
                accountBalanceView.text = formatCurrency(account.balance)
            }
        }

        // Observe transactions
        viewModel.transactions.observe(this) { transactions ->
            val adapter = TransactionAdapter(this, transactions.toMutableList(), 
                { transaction ->
                    // Delete directly without dialog
                    viewModel.deleteTransaction(transaction)
                    Snackbar.make(findViewById(android.R.id.content), "Transaction deleted", Snackbar.LENGTH_SHORT).show()
                },
                { transaction ->
                    // Launch edit activity
                    startActivity(Intent(this, EditTransactionActivity::class.java).apply {
                        putExtra("accountId", intent.getIntExtra("accountId", -1))
                        putExtra("transactionId", transaction.id)
                        putExtra("amount", transaction.amount)
                        putExtra("type", transaction.type)
                        putExtra("category", transaction.category)
                        putExtra("description", transaction.description)
                        putExtra("date", transaction.date)
                        putExtra("isRecurring", transaction.isRecurring)
                        putExtra("frequency", transaction.frequency)
                    })
                }
            )
            transactionListView.adapter = adapter
        }

        addTransactionButton.setOnClickListener { 
            Log.d("AccountTransactionActivity", "Add button clicked!")
            startActivity(Intent(this, AddTransactionActivity::class.java).apply {
                putExtra("accountId", intent.getIntExtra("accountId", -1))
            })
        }
        
        backButton.setOnClickListener { finish() }
    }

    private fun showAddTransactionDialog() {
        // Test with EXACT same pattern as working delete dialog
        AlertDialog.Builder(this)
            .setMessage("TEST - Does this dialog show?")
            .setPositiveButton("Yes") { _, _ ->
                Snackbar.make(findViewById(android.R.id.content), "Dialog worked!", Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton("No", null)
            .show()
    }
    
    private fun showAddTransactionDialog_OLD() {
        try {
            Log.d("AccountTransactionActivity", "showAddTransactionDialog called")
            val builder = AlertDialog.Builder(this, androidx.appcompat.R.style.Theme_AppCompat_Light_Dialog_Alert)
            val view = layoutInflater.inflate(R.layout.dialog_add_transaction, null)
            Log.d("AccountTransactionActivity", "Dialog view inflated successfully")
            
            val typeSpinner = view.findViewById<android.widget.Spinner>(R.id.typeSpinner)
            val categorySpinner = view.findViewById<android.widget.Spinner>(R.id.categorySpinner)
            val descriptionInput = view.findViewById<EditText>(R.id.descriptionInput)
            val amountInput = view.findViewById<EditText>(R.id.amountInput)
            val dateInput = view.findViewById<EditText>(R.id.dateInput)
            val recurringCheckbox = view.findViewById<android.widget.CheckBox>(R.id.recurringCheckbox)
            val frequencySpinner = view.findViewById<android.widget.Spinner>(R.id.frequencySpinner)
            val frequencyLabel = view.findViewById<android.widget.TextView>(R.id.frequencyLabel)

        // Setup category spinner based on transaction type
        updateCategorySpinner(typeSpinner, categorySpinner)
        typeSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                updateCategorySpinner(typeSpinner, categorySpinner)
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        // Show/hide frequency spinner based on recurring checkbox
        recurringCheckbox.setOnCheckedChangeListener { _, isChecked ->
            frequencySpinner.visibility = if (isChecked) android.view.View.VISIBLE else android.view.View.GONE
            frequencyLabel.visibility = if (isChecked) android.view.View.VISIBLE else android.view.View.GONE
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

        val dialog = builder.setView(view)
            .setTitle("Add Transaction")
            .setPositiveButton("Add") { _, _ ->
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
                    Snackbar.make(findViewById(android.R.id.content), "Transaction added!", Snackbar.LENGTH_SHORT).show()
                } else {
                    Snackbar.make(findViewById(android.R.id.content), "Please fill in all fields", Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.WHITE))
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        Log.d("AccountTransactionActivity", "Add dialog shown")
        Snackbar.make(findViewById(android.R.id.content), "Add dialog is open", Snackbar.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("AccountTransactionActivity", "Error showing add dialog", e)
            Snackbar.make(findViewById(android.R.id.content), "Error: ${e.message}", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun updateCategorySpinner(typeSpinner: android.widget.Spinner, categorySpinner: android.widget.Spinner) {
        val isIncome = typeSpinner.selectedItemPosition == 0
        val arrayId = if (isIncome) R.array.income_categories else R.array.expense_categories
        
        android.widget.ArrayAdapter.createFromResource(
            this,
            arrayId,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categorySpinner.adapter = adapter
        }
    }

    private fun showEditTransactionDialog(transaction: TransactionEntity) {
        try {
            Log.d("AccountTransactionActivity", "showEditTransactionDialog called for: ${transaction.description}")
            val builder = AlertDialog.Builder(this, androidx.appcompat.R.style.Theme_AppCompat_Light_Dialog_Alert)
            val view = layoutInflater.inflate(R.layout.dialog_add_transaction, null)
            Log.d("AccountTransactionActivity", "Edit dialog view inflated successfully")
            
            val typeSpinner = view.findViewById<android.widget.Spinner>(R.id.typeSpinner)
            val categorySpinner = view.findViewById<android.widget.Spinner>(R.id.categorySpinner)
            val descriptionInput = view.findViewById<EditText>(R.id.descriptionInput)
            val amountInput = view.findViewById<EditText>(R.id.amountInput)
            val dateInput = view.findViewById<EditText>(R.id.dateInput)
            val recurringCheckbox = view.findViewById<android.widget.CheckBox>(R.id.recurringCheckbox)
            val frequencySpinner = view.findViewById<android.widget.Spinner>(R.id.frequencySpinner)
            val frequencyLabel = view.findViewById<android.widget.TextView>(R.id.frequencyLabel)

        // Pre-populate with existing transaction data
        val typeIndex = if (transaction.type == "income") 0 else 1
        typeSpinner.setSelection(typeIndex)
        descriptionInput.setText(transaction.description)
        amountInput.setText(transaction.amount.toString())
        dateInput.setText(transaction.date)
        selectedDate = transaction.date
        recurringCheckbox.isChecked = transaction.isRecurring
        
        // Setup category spinner based on transaction type
        updateCategorySpinner(typeSpinner, categorySpinner)
        typeSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                updateCategorySpinner(typeSpinner, categorySpinner)
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        // Set category selection
        val categoryArray = if (typeIndex == 0) resources.getStringArray(R.array.income_categories) 
                            else resources.getStringArray(R.array.expense_categories)
        val categoryIndex = categoryArray.indexOf(transaction.category)
        if (categoryIndex >= 0) categorySpinner.setSelection(categoryIndex)

        // Show/hide frequency spinner based on recurring checkbox
        recurringCheckbox.setOnCheckedChangeListener { _, isChecked ->
            frequencySpinner.visibility = if (isChecked) android.view.View.VISIBLE else android.view.View.GONE
            frequencyLabel.visibility = if (isChecked) android.view.View.VISIBLE else android.view.View.GONE
        }

        // Set frequency if recurring
        if (transaction.isRecurring && transaction.frequency != null) {
            val frequencyArray = resources.getStringArray(R.array.recurrence_frequency)
            val freqIndex = frequencyArray.indexOf(transaction.frequency)
            if (freqIndex >= 0) frequencySpinner.setSelection(freqIndex)
        }

        dateInput.setOnClickListener {
            val cal = Calendar.getInstance()
            if (selectedDate.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                val parts = selectedDate.split("-")
                cal.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
            }
            DatePickerDialog(this, { _, year, month, dayOfMonth ->
                val m = (month + 1).toString().padStart(2, '0')
                val d = dayOfMonth.toString().padStart(2, '0')
                selectedDate = "$year-$m-$d"
                dateInput.setText(selectedDate)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        val dialog = builder.setView(view)
            .setTitle("Edit Transaction")
            .setPositiveButton("Update") { _, _ ->
                val amount = amountInput.text.toString().toDoubleOrNull() ?: 0.0
                val category = categorySpinner.selectedItem?.toString() ?: "Uncategorized"
                val description = descriptionInput.text.toString()
                val type = if (typeSpinner.selectedItemPosition == 0) "income" else "expense"
                val isRecurring = recurringCheckbox.isChecked
                val frequency = if (recurringCheckbox.isChecked) frequencySpinner.selectedItem?.toString() else null

                if (amount > 0 && selectedDate.isNotBlank()) {
                    viewModel.updateTransaction(
                        transaction.id,
                        amount,
                        type,
                        category,
                        description,
                        selectedDate,
                        isRecurring,
                        frequency
                    )
                    Snackbar.make(findViewById(android.R.id.content), "Transaction updated!", Snackbar.LENGTH_SHORT).show()
                } else {
                    Snackbar.make(findViewById(android.R.id.content), "Please fill in all fields", Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.WHITE))
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        Log.d("AccountTransactionActivity", "Edit dialog shown")
        Snackbar.make(findViewById(android.R.id.content), "Edit dialog is open", Snackbar.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("AccountTransactionActivity", "Error showing edit dialog", e)
            Snackbar.make(findViewById(android.R.id.content), "Error: ${e.message}", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun formatCurrency(value: Double): String = "$${String.format("%.2f", value)}"
}


