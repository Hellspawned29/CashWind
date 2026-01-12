package com.cashwind.app.util

import android.content.Context
import android.os.Environment
import com.cashwind.app.database.entity.AccountEntity
import com.cashwind.app.database.entity.BillEntity
import com.cashwind.app.database.entity.TransactionEntity
import java.io.File

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

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
}
