package com.cashwind.app.network

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.cashwind.app.util.TokenManager

object RetrofitProvider {
    private const val BASE_URL = "http://127.0.0.1:4000/"
    private lateinit var context: Context
    private lateinit var tokenManager: TokenManager

    fun init(applicationContext: Context) {
        context = applicationContext
        tokenManager = TokenManager(applicationContext)
    }

    private val client: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(AuthInterceptor(tokenManager))
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    val authService: AuthService by lazy { retrofit.create(AuthService::class.java) }
    val billService: BillService by lazy { retrofit.create(BillService::class.java) }
    val accountService: AccountService by lazy { retrofit.create(AccountService::class.java) }
    val transactionService: TransactionService by lazy { retrofit.create(TransactionService::class.java) }
    val budgetService: BudgetService by lazy { retrofit.create(BudgetService::class.java) }
    val goalService: GoalService by lazy { retrofit.create(GoalService::class.java) }
}
