package com.cashwind.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.net.Uri
import com.google.android.material.snackbar.Snackbar
import androidx.lifecycle.lifecycleScope
import androidx.activity.result.contract.ActivityResultContracts
import com.cashwind.app.util.CsvExportUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import com.google.android.material.button.MaterialButton
import java.io.File

class ExportActivity : BaseActivity() {
    private var currentImportType: ImportType? = null

    private enum class ImportType {
        BILLS, ACCOUNTS, TRANSACTIONS, BUDGETS, GOALS, PAYCHECK
    }

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { handleFileImport(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_export)

        setupExportButtons()
        setupImportButtons()
    }

    private fun setupExportButtons() {
        findViewById<MaterialButton>(R.id.exportBillsButton).setOnClickListener { exportBills() }
        findViewById<MaterialButton>(R.id.exportAccountsButton).setOnClickListener { exportAccounts() }
        findViewById<MaterialButton>(R.id.exportTransactionsButton).setOnClickListener { exportTransactions() }
        findViewById<MaterialButton>(R.id.exportBudgetsButton).setOnClickListener { exportBudgets() }
        findViewById<MaterialButton>(R.id.exportGoalsButton).setOnClickListener { exportGoals() }
        findViewById<MaterialButton>(R.id.exportPaycheckButton).setOnClickListener { exportPaycheck() }
        findViewById<MaterialButton>(R.id.exportAllButton).setOnClickListener { exportAll() }
    }

    private fun setupImportButtons() {
        findViewById<MaterialButton>(R.id.importBillsButton).setOnClickListener { 
            currentImportType = ImportType.BILLS
            filePickerLauncher.launch("*/*")
        }
        findViewById<MaterialButton>(R.id.importAccountsButton).setOnClickListener { 
            currentImportType = ImportType.ACCOUNTS
            filePickerLauncher.launch("*/*")
        }
        findViewById<MaterialButton>(R.id.importTransactionsButton).setOnClickListener { 
            currentImportType = ImportType.TRANSACTIONS
            filePickerLauncher.launch("*/*")
        }
        findViewById<MaterialButton>(R.id.importBudgetsButton).setOnClickListener { 
            currentImportType = ImportType.BUDGETS
            filePickerLauncher.launch("*/*")
        }
        findViewById<MaterialButton>(R.id.importGoalsButton).setOnClickListener { 
            currentImportType = ImportType.GOALS
            filePickerLauncher.launch("*/*")
        }
        findViewById<MaterialButton>(R.id.importPaycheckButton).setOnClickListener { 
            currentImportType = ImportType.PAYCHECK
            filePickerLauncher.launch("*/*")
        }
    }

    private fun handleFileImport(uri: Uri) {
        lifecycleScope.launch {
            try {
                val tempFile = withContext(Dispatchers.IO) {
                    val inputStream = contentResolver.openInputStream(uri)
                    val file = File(cacheDir, "import_temp.csv")
                    file.outputStream().use { outputStream ->
                        inputStream?.copyTo(outputStream)
                    }
                    file
                }

                when (currentImportType) {
                    ImportType.BILLS -> importBills(tempFile)
                    ImportType.ACCOUNTS -> importAccounts(tempFile)
                    ImportType.TRANSACTIONS -> importTransactions(tempFile)
                    ImportType.BUDGETS -> importBudgets(tempFile)
                    ImportType.GOALS -> importGoals(tempFile)
                    ImportType.PAYCHECK -> importPaycheck(tempFile)
                    null -> {}
                }

                tempFile.delete()
            } catch (e: Exception) {
                showSnackbar("Import error: ${e.message}")
            }
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
                    showSnackbar("Bills exported successfully")
                } else {
                    showSnackbar("Error exporting bills")
                }
            } catch (e: Exception) {
                showSnackbar("Error: ${e.message}")
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
                    showSnackbar("Accounts exported successfully")
                } else {
                    showSnackbar("Error exporting accounts")
                }
            } catch (e: Exception) {
                showSnackbar("Error: ${e.message}")
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
                    showSnackbar("Transactions exported successfully")
                } else {
                    showSnackbar("Error exporting transactions")
                }
            } catch (e: Exception) {
                showSnackbar("Error: ${e.message}")
            }
        }
    }

    private fun exportBudgets() {
        lifecycleScope.launch {
            try {
                val budgets = withContext(Dispatchers.IO) {
                    database.budgetDao().getAllBudgets(1).first()
                }
                val file = CsvExportUtil.exportBudgetsToCSV(this@ExportActivity, budgets)
                if (file != null) {
                    shareFile(file, "Budgets Export")
                    showSnackbar("Budgets exported successfully")
                } else {
                    showSnackbar("Error exporting budgets")
                }
            } catch (e: Exception) {
                showSnackbar("Error: ${e.message}")
            }
        }
    }

    private fun exportGoals() {
        lifecycleScope.launch {
            try {
                val goals = withContext(Dispatchers.IO) {
                    database.goalDao().getAllGoals(1).first()
                }
                val file = CsvExportUtil.exportGoalsToCSV(this@ExportActivity, goals)
                if (file != null) {
                    shareFile(file, "Goals Export")
                    showSnackbar("Goals exported successfully")
                } else {
                    showSnackbar("Error exporting goals")
                }
            } catch (e: Exception) {
                showSnackbar("Error: ${e.message}")
            }
        }
    }

    private fun exportPaycheck() {
        lifecycleScope.launch {
            try {
                val paycheck = withContext(Dispatchers.IO) {
                    database.paycheckSettingsDao().getPaycheckSettings(1)
                }
                val file = CsvExportUtil.exportPaycheckToCSV(this@ExportActivity, paycheck)
                if (file != null) {
                    shareFile(file, "Paycheck Settings Export")
                    showSnackbar("Paycheck settings exported successfully")
                } else {
                    showSnackbar("Error exporting paycheck settings")
                }
            } catch (e: Exception) {
                showSnackbar("Error: ${e.message}")
            }
        }
    }

    private fun exportAll() {
        lifecycleScope.launch {
            try {
                val bills = withContext(Dispatchers.IO) { database.billDao().getAllBillsDirect() }
                val accounts = withContext(Dispatchers.IO) { database.accountDao().getAllAccountsDirect(1) }
                val transactions = withContext(Dispatchers.IO) { database.transactionDao().getAllTransactionsDirect(1) }
                val budgets = withContext(Dispatchers.IO) { database.budgetDao().getAllBudgets(1).first() }
                val goals = withContext(Dispatchers.IO) { database.goalDao().getAllGoals(1).first() }
                val paycheck = withContext(Dispatchers.IO) { database.paycheckSettingsDao().getPaycheckSettings(1) }
                
                val file = CsvExportUtil.exportAllToCSV(this@ExportActivity, bills, accounts, transactions, budgets, goals, paycheck)
                
                if (file != null) {
                    shareFile(file, "All Data Export")
                    showSnackbar("All data exported successfully")
                } else {
                    showSnackbar("Error exporting data")
                }
            } catch (e: Exception) {
                showSnackbar("Error: ${e.message}")
            }
        }
    }

private fun importBills(file: File) {
        lifecycleScope.launch {
            try {
                val bills = CsvExportUtil.importBillsFromCSV(file, 1)
                if (bills != null && bills.isNotEmpty()) {
                    withContext(Dispatchers.IO) {
                        database.billDao().insertBills(bills)
                    }
                    showSnackbar("Imported ${bills.size} bills successfully")
                } else {
                    showSnackbar("No bills found in file")
                }
            } catch (e: Exception) {
                showSnackbar("Import error: ${e.message}")
            }
        }
    }

    private fun importAccounts(file: File) {
        lifecycleScope.launch {
            try {
                val accounts = CsvExportUtil.importAccountsFromCSV(file, 1)
                if (accounts != null && accounts.isNotEmpty()) {
                    withContext(Dispatchers.IO) {
                        database.accountDao().insertAccounts(accounts)
                    }
                    showSnackbar("Imported ${accounts.size} accounts successfully")
                } else {
                    showSnackbar("No accounts found in file")
                }
            } catch (e: Exception) {
                showSnackbar("Import error: ${e.message}")
            }
        }
    }

    private fun importTransactions(file: File) {
        lifecycleScope.launch {
            try {
                val transactions = CsvExportUtil.importTransactionsFromCSV(file, 1)
                if (transactions != null && transactions.isNotEmpty()) {
                    withContext(Dispatchers.IO) {
                        database.transactionDao().insertTransactions(transactions)
                    }
                    showSnackbar("Imported ${transactions.size} transactions successfully")
                } else {
                    showSnackbar("No transactions found in file")
                }
            } catch (e: Exception) {
                showSnackbar("Import error: ${e.message}")
            }
        }
    }

    private fun importBudgets(file: File) {
        lifecycleScope.launch {
            try {
                val budgets = CsvExportUtil.importBudgetsFromCSV(file, 1)
                if (budgets != null && budgets.isNotEmpty()) {
                    withContext(Dispatchers.IO) {
                        database.budgetDao().insertBudgets(budgets)
                    }
                    showSnackbar("Imported ${budgets.size} budgets successfully")
                } else {
                    showSnackbar("No budgets found in file")
                }
            } catch (e: Exception) {
                showSnackbar("Import error: ${e.message}")
            }
        }
    }

    private fun importGoals(file: File) {
        lifecycleScope.launch {
            try {
                val goals = CsvExportUtil.importGoalsFromCSV(file, 1)
                if (goals != null && goals.isNotEmpty()) {
                    withContext(Dispatchers.IO) {
                        database.goalDao().insertGoals(goals)
                    }
                    showSnackbar("Imported ${goals.size} goals successfully")
                } else {
                    showSnackbar("No goals found in file")
                }
            } catch (e: Exception) {
                showSnackbar("Import error: ${e.message}")
            }
        }
    }

    private fun importPaycheck(file: File) {
        lifecycleScope.launch {
            try {
                val paycheck = CsvExportUtil.importPaycheckFromCSV(file)
                if (paycheck != null) {
                    withContext(Dispatchers.IO) {
                        database.paycheckSettingsDao().savePaycheckSettings(paycheck)
                    }
                    showSnackbar("Paycheck settings imported successfully")
                } else {
                    showSnackbar("No paycheck settings found in file")
                }
            } catch (e: Exception) {
                showSnackbar("Import error: ${e.message}")
            }
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show()
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


