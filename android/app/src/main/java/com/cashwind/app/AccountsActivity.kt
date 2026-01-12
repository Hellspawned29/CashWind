package com.cashwind.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.cashwind.app.databinding.ActivityAccountsBinding
import com.cashwind.app.ui.AccountsViewModel
import com.cashwind.app.ui.AccountAdapter
import com.cashwind.app.model.Account

class AccountsActivity : BaseActivity() {
    private lateinit var binding: ActivityAccountsBinding
    private val viewModel: AccountsViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return AccountsViewModel(database) as T
            }
        }
    }
    private lateinit var adapter: AccountAdapter
    private var currentAccounts: List<Account> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = AccountAdapter(
            onClick = { account ->
                val intent = Intent(this, AddAccountActivity::class.java).apply {
                    putExtra("id", account.id)
                    putExtra("userId", account.userId)
                    putExtra("name", account.name)
                    putExtra("type", account.type)
                    putExtra("balance", account.balance)
                    putExtra("accountType", account.accountType)
                    putExtra("creditLimit", account.creditLimit ?: Double.NaN)
                    putExtra("interestRate", account.interestRate ?: Double.NaN)
                    putExtra("minimumPayment", account.minimumPayment ?: Double.NaN)
                    putExtra("dueDay", account.dueDay ?: -1)
                }
                startActivity(intent)
            },
            onTransactions = { account ->
                val intent = Intent(this, AccountTransactionActivity::class.java).apply {
                    putExtra("accountId", account.id)
                }
                startActivity(intent)
            },
            onDelete = { account -> viewModel.deleteAccount(account) }
        )

        binding.accountsRecycler.apply {
            layoutManager = LinearLayoutManager(this@AccountsActivity)
            adapter = this@AccountsActivity.adapter
        }

        viewModel.accounts.observe(this) { list ->
            currentAccounts = list
            applyFilter()
        }

        viewModel.totalBalance.observe(this) { total ->
            binding.totalBalance.text = formatCurrency(total)
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.account_filter_types,
            android.R.layout.simple_spinner_item
        ).also { arr ->
            arr.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.typeSpinner.adapter = arr
        }

        binding.typeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                applyFilter()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.addAccountButton.setOnClickListener {
            startActivity(Intent(this, AddAccountActivity::class.java))
        }

        binding.backButton.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }
    }

    private fun applyFilter() {
        val selected = binding.typeSpinner.selectedItem?.toString() ?: "All"
        val filtered = if (selected == "All") currentAccounts else currentAccounts.filter { it.type.equals(selected, true) }
        adapter.submitList(filtered)
        binding.emptyText.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun formatCurrency(v: Double): String = "$${String.format("%.2f", v)}"
}
