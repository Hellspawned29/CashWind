package com.cashwind.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cashwind.app.ui.AccountsViewModel
import com.cashwind.app.ui.AccountAdapter
import com.cashwind.app.model.Account
import android.widget.Spinner
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AccountsActivity : BaseActivity() {
    private val viewModel: AccountsViewModel by lazy {
        androidx.lifecycle.ViewModelProvider(
            this,
            object : androidx.lifecycle.ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return AccountsViewModel(database) as T
                }
            }
        )[AccountsViewModel::class.java]
    }
    private lateinit var adapter: AccountAdapter
    private var currentAccounts: List<Account> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accounts)

        val accountsRecycler = findViewById<RecyclerView>(R.id.accountsRecycler)
        val totalBalance = findViewById<TextView>(R.id.totalBalance)
        val typeSpinner = findViewById<Spinner>(R.id.typeSpinner)
        val addAccountButton = findViewById<FloatingActionButton>(R.id.addAccountButton)
        val backButton = findViewById<View>(R.id.backButton)
        val emptyText = findViewById<TextView>(R.id.emptyText)

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

        accountsRecycler.apply {
            layoutManager = LinearLayoutManager(this@AccountsActivity)
            adapter = this@AccountsActivity.adapter
        }

        viewModel.accounts.observe(this) { list ->
            currentAccounts = list
            applyFilter()
        }

        viewModel.totalBalance.observe(this) { total ->
            totalBalance.text = formatCurrency(total)
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.account_filter_types,
            android.R.layout.simple_spinner_item
        ).also { arr ->
            arr.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            typeSpinner.adapter = arr
        }

        typeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                applyFilter()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        addAccountButton.setOnClickListener {
            startActivity(Intent(this, AddAccountActivity::class.java))
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun applyFilter() {
        val typeSpinner = findViewById<Spinner>(R.id.typeSpinner)
        val emptyText = findViewById<TextView>(R.id.emptyText)
        val selected = typeSpinner.selectedItem?.toString() ?: "All"
        val filtered = if (selected == "All") currentAccounts else currentAccounts.filter { it.type.equals(selected, true) }
        adapter.submitList(filtered)
        emptyText.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun formatCurrency(v: Double): String = "$${String.format("%.2f", v)}"
}
