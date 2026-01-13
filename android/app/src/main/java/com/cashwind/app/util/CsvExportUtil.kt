package com.cashwind.app.util

import android.content.Context
import android.os.Environment
import com.cashwind.app.database.entity.AccountEntity
import com.cashwind.app.database.entity.BillEntity
import com.cashwind.app.database.entity.TransactionEntity
import com.cashwind.app.database.entity.BudgetEntity
import com.cashwind.app.database.entity.GoalEntity
import com.cashwind.app.database.entity.PaycheckSettingsEntity
import java.io.File
import java.io.BufferedReader

object CsvExportUtil {

    fun exportBillsToCSV(context: Context, bills: List<BillEntity>): File? {
        return try {
            val fileName = "cashwind_bills_${System.currentTimeMillis()}.csv"
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

            file.writeText(buildBillsCSV(bills))
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun exportAccountsToCSV(context: Context, accounts: List<AccountEntity>): File? {
        return try {
            val fileName = "cashwind_accounts_${System.currentTimeMillis()}.csv"
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

            file.writeText(buildAccountsCSV(accounts))
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun exportTransactionsToCSV(context: Context, transactions: List<TransactionEntity>): File? {
        return try {
            val fileName = "cashwind_transactions_${System.currentTimeMillis()}.csv"
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

            file.writeText(buildTransactionsCSV(transactions))
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun exportBudgetsToCSV(context: Context, budgets: List<BudgetEntity>): File? {
        return try {
            val fileName = "cashwind_budgets_${System.currentTimeMillis()}.csv"
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

            file.writeText(buildBudgetsCSV(budgets))
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun exportGoalsToCSV(context: Context, goals: List<GoalEntity>): File? {
        return try {
            val fileName = "cashwind_goals_${System.currentTimeMillis()}.csv"
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

            file.writeText(buildGoalsCSV(goals))
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun exportPaycheckToCSV(context: Context, paycheck: PaycheckSettingsEntity?): File? {
        return try {
            val fileName = "cashwind_paycheck_${System.currentTimeMillis()}.csv"
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

            file.writeText(buildPaycheckCSV(paycheck))
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun exportAllToCSV(context: Context, 
                       bills: List<BillEntity>,
                       accounts: List<AccountEntity>,
                       transactions: List<TransactionEntity>,
                       budgets: List<BudgetEntity>,
                       goals: List<GoalEntity>,
                       paycheck: PaycheckSettingsEntity?): File? {
        return try {
            val fileName = "cashwind_all_data_${System.currentTimeMillis()}.csv"
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

            val allData = StringBuilder()
            allData.append("=== BILLS ===\n")
            allData.append(buildBillsCSV(bills))
            allData.append("\n=== ACCOUNTS ===\n")
            allData.append(buildAccountsCSV(accounts))
            allData.append("\n=== TRANSACTIONS ===\n")
            allData.append(buildTransactionsCSV(transactions))
            allData.append("\n=== BUDGETS ===\n")
            allData.append(buildBudgetsCSV(budgets))
            allData.append("\n=== GOALS ===\n")
            allData.append(buildGoalsCSV(goals))
            allData.append("\n=== PAYCHECK ===\n")
            allData.append(buildPaycheckCSV(paycheck))

            file.writeText(allData.toString())
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun buildBillsCSV(bills: List<BillEntity>): String {
        val csvBuilder = StringBuilder()
        csvBuilder.append("ID,Name,Amount,Category,Due Date,Status,Notes\n")

        bills.forEach { bill ->
            val dueDate = bill.dueDate // Already in yyyy-MM-dd format
            val status = if (bill.isPaid) "Paid" else "Unpaid"
            val category = bill.category ?: ""
            val notes = bill.notes ?: ""
            csvBuilder.append("${bill.id},${escapeCsv(bill.name)},${bill.amount},${escapeCsv(category)},${dueDate},${status},${escapeCsv(notes)}\n")
        }

        return csvBuilder.toString()
    }

    private fun buildAccountsCSV(accounts: List<AccountEntity>): String {
        val csvBuilder = StringBuilder()
        csvBuilder.append("ID,Name,Type,Balance\n")

        accounts.forEach { account ->
            csvBuilder.append("${account.id},${escapeCsv(account.name)},${account.type},${account.balance}\n")
        }

        return csvBuilder.toString()
    }

    private fun buildTransactionsCSV(transactions: List<TransactionEntity>): String {
        val csvBuilder = StringBuilder()
        csvBuilder.append("ID,Description,Amount,Category,Date,Type\n")

        transactions.forEach { transaction ->
            val date = transaction.date // Already in yyyy-MM-dd format
            val type = if (transaction.amount > 0) "Income" else "Expense"
            val category = transaction.category ?: ""
            val description = transaction.description ?: ""
            csvBuilder.append("${transaction.id},${escapeCsv(description)},${Math.abs(transaction.amount)},${escapeCsv(category)},${date},${type}\n")
        }

        return csvBuilder.toString()
    }

    private fun buildBudgetsCSV(budgets: List<BudgetEntity>): String {
        val csvBuilder = StringBuilder()
        csvBuilder.append("ID,Name,Category,Amount,Period\n")

        budgets.forEach { budget ->
            val category = budget.category ?: ""
            csvBuilder.append("${budget.id},${escapeCsv(budget.name)},${escapeCsv(category)},${budget.amount},${budget.period}\n")
        }

        return csvBuilder.toString()
    }

    private fun buildGoalsCSV(goals: List<GoalEntity>): String {
        val csvBuilder = StringBuilder()
        csvBuilder.append("ID,Name,Type,Target Amount,Current Amount,Target Date,Notes\n")

        goals.forEach { goal ->
            val notes = goal.notes ?: ""
            csvBuilder.append("${goal.id},${escapeCsv(goal.name)},${goal.type},${goal.targetAmount},${goal.currentAmount},${goal.targetDate},${escapeCsv(notes)}\n")
        }

        return csvBuilder.toString()
    }

    private fun buildPaycheckCSV(paycheck: PaycheckSettingsEntity?): String {
        val csvBuilder = StringBuilder()
        csvBuilder.append("User ID,Amount,Frequency Days\n")

        if (paycheck != null) {
            csvBuilder.append("${paycheck.userId},${paycheck.amount},${paycheck.frequencyDays}\n")
        }

        return csvBuilder.toString()
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }

    // Import functions
    fun importBillsFromCSV(file: File, userId: Int): List<BillEntity>? {
        return try {
            val lines = file.readLines()
            if (lines.size < 2) return null // No data

            val bills = mutableListOf<BillEntity>()
            // Skip header
            for (i in 1 until lines.size) {
                val parts = parseCsvLine(lines[i])
                if (parts.size >= 6) {
                    bills.add(BillEntity(
                        id = 0, // Auto-generate new ID
                        userId = userId,
                        name = parts[1],
                        amount = parts[2].toDoubleOrNull() ?: 0.0,
                        category = parts[3].ifEmpty { null },
                        dueDate = parts[4],
                        isPaid = parts[5] == "Paid",
                        notes = parts.getOrNull(6)?.ifEmpty { null }
                    ))
                }
            }
            bills
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun importAccountsFromCSV(file: File, userId: Int): List<AccountEntity>? {
        return try {
            val lines = file.readLines()
            if (lines.size < 2) return null

            val accounts = mutableListOf<AccountEntity>()
            for (i in 1 until lines.size) {
                val parts = parseCsvLine(lines[i])
                if (parts.size >= 4) {
                    accounts.add(AccountEntity(
                        id = 0,
                        userId = userId,
                        name = parts[1],
                        type = parts[2],
                        balance = parts[3].toDoubleOrNull() ?: 0.0
                    ))
                }
            }
            accounts
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun importTransactionsFromCSV(file: File, userId: Int): List<TransactionEntity>? {
        return try {
            val lines = file.readLines()
            if (lines.size < 2) return null

            val transactions = mutableListOf<TransactionEntity>()
            for (i in 1 until lines.size) {
                val parts = parseCsvLine(lines[i])
                if (parts.size >= 6) {
                    val amount = parts[2].toDoubleOrNull() ?: 0.0
                    val finalAmount = if (parts[5] == "Expense") -amount else amount
                    
                    transactions.add(TransactionEntity(
                        id = 0,
                        userId = userId,
                        accountId = 1, // Default account
                        description = parts[1].ifEmpty { null },
                        amount = finalAmount,
                        category = parts[3],
                        type = parts[5],
                        date = parts[4]
                    ))
                }
            }
            transactions
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun importBudgetsFromCSV(file: File, userId: Int): List<BudgetEntity>? {
        return try {
            val lines = file.readLines()
            if (lines.size < 2) return null

            val budgets = mutableListOf<BudgetEntity>()
            for (i in 1 until lines.size) {
                val parts = parseCsvLine(lines[i])
                if (parts.size >= 5) {
                    budgets.add(BudgetEntity(
                        id = 0,
                        userId = userId,
                        name = parts[1],
                        category = parts[2].ifEmpty { null },
                        amount = parts[3].toDoubleOrNull() ?: 0.0,
                        period = parts[4]
                    ))
                }
            }
            budgets
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun importGoalsFromCSV(file: File, userId: Int): List<GoalEntity>? {
        return try {
            val lines = file.readLines()
            if (lines.size < 2) return null

            val goals = mutableListOf<GoalEntity>()
            for (i in 1 until lines.size) {
                val parts = parseCsvLine(lines[i])
                if (parts.size >= 6) {
                    goals.add(GoalEntity(
                        id = 0,
                        userId = userId,
                        name = parts[1],
                        type = parts[2],
                        targetAmount = parts[3].toDoubleOrNull() ?: 0.0,
                        currentAmount = parts[4].toDoubleOrNull() ?: 0.0,
                        targetDate = parts[5],
                        notes = parts.getOrNull(6)?.ifEmpty { null }
                    ))
                }
            }
            goals
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun importPaycheckFromCSV(file: File): PaycheckSettingsEntity? {
        return try {
            val lines = file.readLines()
            if (lines.size < 2) return null

            val parts = parseCsvLine(lines[1])
            if (parts.size >= 3) {
                PaycheckSettingsEntity(
                    userId = parts[0].toIntOrNull() ?: 1,
                    amount = parts[1].toDoubleOrNull() ?: 0.0,
                    frequencyDays = parts[2].toIntOrNull() ?: 14
                )
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false

        for (i in line.indices) {
            val char = line[i]
            when {
                char == '"' -> inQuotes = !inQuotes
                char == ',' && !inQuotes -> {
                    result.add(current.toString())
                    current = StringBuilder()
                }
                else -> current.append(char)
            }
        }
        result.add(current.toString())

        return result.map { it.replace("\"\"", "\"") }
    }
}
