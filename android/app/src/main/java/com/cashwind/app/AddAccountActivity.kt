package com.cashwind.app

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.cashwind.app.database.CashwindDatabase
import com.cashwind.app.database.entity.AccountEntity
import com.cashwind.app.databinding.ActivityAddAccountBinding
import com.cashwind.app.ui.AddAccountViewModel

class AddAccountActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddAccountBinding
    private val viewModel: AddAccountViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                val database = CashwindDatabase.getInstance(this@AddAccountActivity)
                return AddAccountViewModel(database) as T
            }
        }
    }

    private var editingAccountId: Int = 0
    private var userId: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ArrayAdapter.createFromResource(
            this,
            R.array.account_types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.typeSpinner.adapter = adapter
        }

        editingAccountId = intent.getIntExtra("id", 0)
        userId = intent.getIntExtra("userId", 1)
        val existingName = intent.getStringExtra("name")
        val existingType = intent.getStringExtra("type")
        val existingBalance = intent.getDoubleExtra("balance", 0.0)
        val existingLabel = intent.getStringExtra("accountType")
        val existingLimit = intent.getDoubleExtra("creditLimit", Double.NaN)
        val existingInterest = intent.getDoubleExtra("interestRate", Double.NaN)
        val existingMinimum = intent.getDoubleExtra("minimumPayment", Double.NaN)
        val existingDueDay = intent.getIntExtra("dueDay", -1)

        if (editingAccountId != 0) {
            binding.titleText.text = "Edit Account"
            binding.saveAccountButton.text = "Save Changes"
            binding.accountNameInput.setText(existingName ?: "")
            binding.accountBalanceInput.setText(if (existingBalance == 0.0) "" else existingBalance.toString())
            binding.accountLabelInput.setText(existingLabel ?: "")
            if (!existingLimit.isNaN()) binding.creditLimitInput.setText(existingLimit.toString())
            if (!existingInterest.isNaN()) binding.interestRateInput.setText(existingInterest.toString())
            if (!existingMinimum.isNaN()) binding.minimumPaymentInput.setText(existingMinimum.toString())
            if (existingDueDay > 0) binding.dueDayInput.setText(existingDueDay.toString())

            existingType?.let { type ->
                val index = resources.getStringArray(R.array.account_types).indexOf(type)
                if (index >= 0) binding.typeSpinner.setSelection(index)
            }
        }

        binding.saveAccountButton.setOnClickListener { saveAccount() }
        binding.cancelButton.setOnClickListener { finish() }
    }

    private fun saveAccount() {
        val name = binding.accountNameInput.text.toString().trim()
        val type = binding.typeSpinner.selectedItem?.toString()?.trim().orEmpty()
        val balanceStr = binding.accountBalanceInput.text.toString().trim()
        val label = binding.accountLabelInput.text.toString().trim().ifEmpty { null }
        val creditLimitStr = binding.creditLimitInput.text.toString().trim()
        val interestRateStr = binding.interestRateInput.text.toString().trim()
        val minimumPaymentStr = binding.minimumPaymentInput.text.toString().trim()
        val dueDayStr = binding.dueDayInput.text.toString().trim()

        if (name.isEmpty()) {
            Snackbar.make(binding.root, "Account name required", Snackbar.LENGTH_SHORT).show()
            return
        }

        if (type.isEmpty()) {
            Snackbar.make(binding.root, "Account type required", Snackbar.LENGTH_SHORT).show()
            return
        }

        if (balanceStr.isEmpty()) {
            Snackbar.make(binding.root, "Balance required", Snackbar.LENGTH_SHORT).show()
            return
        }

        val balance = balanceStr.toDoubleOrNull()
        if (balance == null) {
            Snackbar.make(binding.root, "Invalid balance", Snackbar.LENGTH_SHORT).show()
            return
        }

        val creditLimit = creditLimitStr.toDoubleOrNull()
        val interestRate = interestRateStr.toDoubleOrNull()
        val minimumPayment = minimumPaymentStr.toDoubleOrNull()
        val dueDay = dueDayStr.toIntOrNull()

        val account = AccountEntity(
            id = editingAccountId,
            userId = if (userId == 0) 1 else userId,
            type = type,
            name = name,
            balance = balance,
            accountType = label,
            creditLimit = creditLimit,
            interestRate = interestRate,
            minimumPayment = minimumPayment,
            dueDay = dueDay
        )

        viewModel.saveOrUpdate(account)
        Snackbar.make(binding.root, if (editingAccountId == 0) "Account added" else "Account updated", Snackbar.LENGTH_SHORT).show()
        finish()
    }
}
