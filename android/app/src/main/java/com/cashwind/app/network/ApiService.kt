package com.cashwind.app.network

import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Body
import com.cashwind.app.model.*

interface AuthService {
    @POST("/auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("/auth/login")
    suspend fun login(@Body request: AuthRequest): AuthResponse
}

interface BillService {
    @GET("/bills")
    suspend fun listBills(): List<Bill>

    @POST("/bills")
    suspend fun createBill(@Body request: CreateBillRequest): Bill
}

interface AccountService {
    @GET("/accounts")
    suspend fun listAccounts(): List<Account>

    @POST("/accounts")
    suspend fun createAccount(@Body request: CreateAccountRequest): Account
}

interface TransactionService {
    @GET("/transactions")
    suspend fun listTransactions(): List<Transaction>

    @POST("/transactions")
    suspend fun createTransaction(@Body request: CreateTransactionRequest): Transaction

    @GET("/transactions/analytics")
    suspend fun getAnalytics(): TransactionAnalytics
}

interface BudgetService {
    @GET("/budgets")
    suspend fun listBudgets(): List<Budget>

    @POST("/budgets")
    suspend fun createBudget(@Body request: CreateBudgetRequest): Budget

    @GET("/budgets/progress")
    suspend fun getBudgetProgress(): List<BudgetProgress>
}

interface GoalService {
    @GET("/goals")
    suspend fun listGoals(): List<Goal>

    @POST("/goals")
    suspend fun createGoal(@Body request: CreateGoalRequest): Goal

    @GET("/goals/progress")
    suspend fun getGoalProgress(): List<GoalProgress>
}
