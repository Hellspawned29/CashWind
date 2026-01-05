package com.cashwind.app.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cashwind.app.database.CashwindDatabase
import com.cashwind.app.database.entity.GoalEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class GoalsViewModel(private val db: CashwindDatabase) : ViewModel() {
    private val goalDao = db.goalDao()
    private val userId = 1 // Default user (no auth)

    val goals: LiveData<List<GoalEntity>> = goalDao.getAllGoalsLive(userId)

    fun addGoal(
        name: String,
        type: String,
        targetAmount: Double,
        targetDate: String,
        monthlyContribution: Double?,
        category: String?,
        notes: String?
    ) {
        viewModelScope.launch {
            val newId = (System.currentTimeMillis() / 1000).toInt()
            val goal = GoalEntity(
                id = newId,
                userId = userId,
                name = name,
                type = type,
                targetAmount = targetAmount,
                currentAmount = 0.0,
                targetDate = targetDate,
                monthlyContribution = monthlyContribution,
                priority = "medium",
                status = "active",
                accountId = null,
                category = category,
                notes = notes
            )
            goalDao.insertGoal(goal)
        }
    }

    fun updateProgress(goal: GoalEntity, amount: Double) {
        viewModelScope.launch {
            val updated = goal.copy(
                currentAmount = (goal.currentAmount + amount).coerceIn(0.0, goal.targetAmount)
            )
            goalDao.updateGoal(updated)
        }
    }

    fun deleteGoal(goal: GoalEntity) {
        viewModelScope.launch {
            goalDao.deleteGoal(goal)
        }
    }
}
