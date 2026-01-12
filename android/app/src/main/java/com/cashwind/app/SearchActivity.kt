package com.cashwind.app

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cashwind.app.database.entity.AccountEntity
import com.cashwind.app.database.entity.BillEntity
import com.cashwind.app.database.entity.TransactionEntity
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

class SearchActivity : BaseActivity() {

    private lateinit var searchInput: EditText
    private lateinit var searchResultsRecyclerView: RecyclerView
    private lateinit var searchAdapter: SearchResultsAdapter
    private var searchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        searchInput = findViewById(R.id.searchInput)
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView)
        val backButton = findViewById<Button>(R.id.backButton)

        searchAdapter = SearchResultsAdapter()
        searchResultsRecyclerView.layoutManager = LinearLayoutManager(this)
        searchResultsRecyclerView.adapter = searchAdapter

        backButton.setOnClickListener {
            finish()
        }

        // Perform search as user types
        searchInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                performSearch(s.toString())
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        searchInput.requestFocus()
    }

    private fun performSearch(query: String) {
        // Cancel previous search job
        searchJob?.cancel()
        
        if (query.isEmpty()) {
            searchAdapter.setResults(emptyList(), emptyList(), emptyList())
            return
        }

        searchJob = lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Add small delay to debounce rapid typing
                delay(300)
                
                val bills = database.billDao().searchBillsDirect(query)
                val accounts = database.accountDao().searchAccountsDirect(query)
                val transactions = database.transactionDao().searchTransactionsDirect(query)

                // Update UI on main thread
                withContext(Dispatchers.Main) {
                    searchAdapter.setResults(bills, accounts, transactions)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    searchAdapter.setResults(emptyList(), emptyList(), emptyList())
                }
            }
        }
    }
}

