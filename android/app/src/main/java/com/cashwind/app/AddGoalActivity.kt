package com.cashwind.app

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import com.cashwind.app.databinding.ActivityAddGoalBinding
import com.cashwind.app.ui.GoalsViewModel
import com.cashwind.app.util.DateUtils
import java.util.*

class AddGoalActivity : BaseActivity() {
    private lateinit var binding: ActivityAddGoalBinding
    private val viewModel: GoalsViewModel by lazy {
        androidx.lifecycle.ViewModelProvider(
            this,
            object : androidx.lifecycle.ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return GoalsViewModel(database) as T
                }
            }
        )[GoalsViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddGoalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set default target date (1 year from now)
        val defaultDate = DateUtils.formatIsoDate(
            Calendar.getInstance().apply { add(Calendar.YEAR, 1) }.time
        )
        binding.goalTargetDateInput.setText(defaultDate)

        binding.saveGoalButton.setOnClickListener { saveGoal() }
        binding.cancelButton.setOnClickListener { finish() }
    }

    private fun saveGoal() {
        val name = binding.goalNameInput.text.toString().trim()
        val targetAmountStr = binding.goalTargetAmountInput.text.toString().trim()
        val targetDate = binding.goalTargetDateInput.text.toString().trim()
        val monthlyStr = binding.goalMonthlyInput.text.toString().trim()
        val category = binding.goalCategoryInput.text.toString().trim()
        val notes = binding.goalNotesInput.text.toString().trim()

        if (name.isEmpty()) {
            Snackbar.make(binding.root, "Goal name required", Snackbar.LENGTH_SHORT).show()
            return
        }

        if (targetAmountStr.isEmpty()) {
            Snackbar.make(binding.root, "Target amount required", Snackbar.LENGTH_SHORT).show()
            return
        }

        val targetAmount = try {
            targetAmountStr.toDouble()
        } catch (e: NumberFormatException) {
            Snackbar.make(binding.root, "Invalid target amount", Snackbar.LENGTH_SHORT).show()
            return
        }

        if (targetAmount <= 0) {
            Snackbar.make(binding.root, "Target amount must be greater than 0", Snackbar.LENGTH_SHORT).show()
            return
        }

        if (targetDate.isEmpty()) {
            Snackbar.make(binding.root, "Target date required", Snackbar.LENGTH_SHORT).show()
            return
        }

        // Validate date format
        if (DateUtils.parseIsoDate(targetDate) == null) {
            Snackbar.make(binding.root, "Invalid date format (yyyy-MM-dd)", Snackbar.LENGTH_SHORT).show()
            return
        }

        val monthly = if (monthlyStr.isNotEmpty()) {
            try {
                monthlyStr.toDouble()
            } catch (e: NumberFormatException) {
                Snackbar.make(binding.root, "Invalid monthly contribution amount", Snackbar.LENGTH_SHORT).show()
                return
            }
        } else {
            null
        }

        val finalCategory = category.ifBlank { null }
        val finalNotes = notes.ifBlank { null }

        viewModel.addGoal(name, "savings", targetAmount, targetDate, monthly, finalCategory, finalNotes)
        Snackbar.make(binding.root, "Goal created!", Snackbar.LENGTH_SHORT).show()
        finish()
    }
}
