package com.cashwind.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button

import com.google.android.material.snackbar.Snackbar
import androidx.lifecycle.lifecycleScope
import com.cashwind.app.util.CsvExportUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.android.material.button.MaterialButton

class ExportActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_export)

        val backButton = findViewById<Button>(R.id.backButton)
        val exportBillsButton = findViewById<MaterialButton>(R.id.exportBillsButton)
        val exportAccountsButton = findViewById<MaterialButton>(R.id.exportAccountsButton)
        val exportTransactionsButton = findViewById<MaterialButton>(R.id.exportTransactionsButton)
        val exportAllButton = findViewById<MaterialButton>(R.id.exportAllButton)

        backButton.setOnClickListener {
            finish()
        }

        exportBillsButton.setOnClickListener {
            exportBills()
        }

        exportAccountsButton.setOnClickListener {
            exportAccounts()
        }

        exportTransactionsButton.setOnClickListener {
            exportTransactions()
        }

        exportAllButton.setOnClickListener {
            exportAll()
        }
    }

    private fun exportBills() {
        lifecycleScope.launch {
            try {
                val bills = withContext(Dispatchers.IO) {
                    database.billDao().getAllBillsDirect()
                }
                val file = CsvExportUtil.exportBillsToCSV(this@ExportActivity, bills)
                if (file != null) {
                    shareFile(file, "Bills Export")
                    Snackbar.make(findViewById(android.R.id.content), "Bills exported successfully", Snackbar.LENGTH_SHORT).show()
                } else {
                    Snackbar.make(findViewById(android.R.id.content), "Error exporting bills", Snackbar.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Snackbar.make(findViewById(android.R.id.content), "Error: ${e.message}", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun exportAccounts() {
        lifecycleScope.launch {
            try {
                val accounts = withContext(Dispatchers.IO) {
                    database.accountDao().getAllAccountsDirect(1)
                }
                val file = CsvExportUtil.exportAccountsToCSV(this@ExportActivity, accounts)
                if (file != null) {
                    shareFile(file, "Accounts Export")
                    Snackbar.make(findViewById(android.R.id.content), "Accounts exported successfully", Snackbar.LENGTH_SHORT).show()
                } else {
                    Snackbar.make(findViewById(android.R.id.content), "Error exporting accounts", Snackbar.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Snackbar.make(findViewById(android.R.id.content), "Error: ${e.message}", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun exportTransactions() {
        lifecycleScope.launch {
            try {
                val transactions = withContext(Dispatchers.IO) {
                    database.transactionDao().getAllTransactionsDirect(1)
                }
                val file = CsvExportUtil.exportTransactionsToCSV(this@ExportActivity, transactions)
                if (file != null) {
                    shareFile(file, "Transactions Export")
                    Snackbar.make(findViewById(android.R.id.content), "Transactions exported successfully", Snackbar.LENGTH_SHORT).show()
                } else {
                    Snackbar.make(findViewById(android.R.id.content), "Error exporting transactions", Snackbar.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Snackbar.make(findViewById(android.R.id.content), "Error: ${e.message}", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun exportAll() {
        lifecycleScope.launch {
            try {
                val bills = withContext(Dispatchers.IO) {
                    database.billDao().getAllBillsDirect()
                }
                val accounts = withContext(Dispatchers.IO) {
                    database.accountDao().getAllAccountsDirect(1)
                }
                val transactions = withContext(Dispatchers.IO) {
                    database.transactionDao().getAllTransactionsDirect(1)
                }
                
                val billFile = CsvExportUtil.exportBillsToCSV(this@ExportActivity, bills)
                val accountFile = CsvExportUtil.exportAccountsToCSV(this@ExportActivity, accounts)
                val transactionFile = CsvExportUtil.exportTransactionsToCSV(this@ExportActivity, transactions)
                
                if (billFile != null && accountFile != null && transactionFile != null) {
                    Snackbar.make(findViewById(android.R.id.content), "All data exported successfully", Snackbar.LENGTH_SHORT).show()
                } else {
                    Snackbar.make(findViewById(android.R.id.content), "Error exporting some data", Snackbar.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Snackbar.make(findViewById(android.R.id.content), "Error: ${e.message}", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun shareFile(file: java.io.File, title: String) {
        val uri = androidx.core.content.FileProvider.getUriForFile(
            this,
            "${packageName}.provider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        startActivity(Intent.createChooser(intent, "Share $title"))
    }
}


