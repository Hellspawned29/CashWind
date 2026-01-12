package com.cashwind.app

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import com.cashwind.app.ui.AnalyticsViewModel

class AnalyticsActivity : BaseActivity() {
    private val viewModel: AnalyticsViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return AnalyticsViewModel(database) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)

        val backButton = findViewById<Button>(R.id.backButton)
        val refreshButton = findViewById<Button>(R.id.refreshButton)
        
        // Summary cards
        val totalIncomeText = findViewById<TextView>(R.id.totalIncomeText)
        val totalExpensesText = findViewById<TextView>(R.id.totalExpensesText)
        val netCashFlowText = findViewById<TextView>(R.id.netCashFlowText)
        val savingsRateText = findViewById<TextView>(R.id.savingsRateText)
        val largestCategoryText = findViewById<TextView>(R.id.largestCategoryText)
        val upcomingBillsText = findViewById<TextView>(R.id.upcomingBillsText)
        val activeGoalsText = findViewById<TextView>(R.id.activeGoalsText)
        
        // Lists
        val categoryList = findViewById<LinearLayout>(R.id.categoryList)
        val budgetList = findViewById<LinearLayout>(R.id.budgetList)
        val trendsList = findViewById<LinearLayout>(R.id.trendsList)
        
        // Observe analytics summary
        viewModel.analyticsSummary.observe(this) { summary ->
            totalIncomeText.text = formatCurrency(summary.totalIncome)
            totalExpensesText.text = formatCurrency(summary.totalExpenses)
            netCashFlowText.text = formatCurrency(summary.netCashFlow)
            val color = if (summary.netCashFlow >= 0) Color.parseColor("#2E7D32") else Color.parseColor("#C62828")
            netCashFlowText.setTextColor(color)
            
            savingsRateText.text = "${summary.savingsRate.toInt()}%"
            largestCategoryText.text = summary.largestExpenseCategory
            upcomingBillsText.text = "${summary.upcomingBillsCount} bills (${formatCurrency(summary.upcomingBillsTotal)})"
            activeGoalsText.text = "${summary.goalsOnTrack}/${summary.activeGoalsCount} on track"
        }
        
        // Observe spending by category
        viewModel.spendingByCategory.observe(this) { spending ->
            categoryList.removeAllViews()
            spending.forEach { category ->
                val item = TextView(this).apply {
                    text = "${category.category}: ${formatCurrency(category.amount)} (${category.percentage.toInt()}%)"
                    textSize = 14f
                    setPadding(0, 8, 0, 8)
                }
                categoryList.addView(item)
            }
        }
        
        // Observe monthly trends
        viewModel.monthlyTrends.observe(this) { trends ->
            trendsList.removeAllViews()
            trends.forEach { trend ->
                val item = TextView(this).apply {
                    text = "${trend.month}: Income ${formatCurrency(trend.income)}, Expenses ${formatCurrency(trend.expenses)}, Net ${formatCurrency(trend.net)}"
                    textSize = 14f
                    setPadding(0, 8, 0, 8)
                    val color = if (trend.net >= 0) Color.parseColor("#2E7D32") else Color.parseColor("#C62828")
                    setTextColor(color)
                }
                trendsList.addView(item)
            }
        }
        
        // Observe budget performance
        viewModel.budgetPerformance.observe(this) { performance ->
            budgetList.removeAllViews()
            performance.forEach { budget ->
                val item = TextView(this).apply {
                    val status = if (budget.isOverBudget) "⚠️ OVER" else "✓"
                    text = "$status ${budget.category}: ${formatCurrency(budget.actualSpent)}/${formatCurrency(budget.budgetLimit)} (${budget.percentageUsed.toInt()}%)"
                    textSize = 14f
                    setPadding(0, 8, 0, 8)
                    val color = if (budget.isOverBudget) Color.RED else Color.parseColor("#2E7D32")
                    setTextColor(color)
                }
                budgetList.addView(item)
            }
        }
        
        backButton.setOnClickListener { finish() }
        refreshButton.setOnClickListener { viewModel.loadAnalytics() }
    }
    
    private fun formatCurrency(value: Double): String = "$${String.format("%.2f", value)}"
}
