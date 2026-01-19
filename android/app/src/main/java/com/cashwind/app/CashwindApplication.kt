package com.cashwind.app

import android.app.Application
import android.util.Log
import com.cashwind.app.database.CashwindDatabase

class CashwindApplication : Application() {
    
    companion object {
        lateinit var database: CashwindDatabase
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        
        try {
            Log.d("CashwindApplication", "=== APPLICATION STARTING ===")
            Log.d("CashwindApplication", "Android Version: ${android.os.Build.VERSION.SDK_INT}")
            Log.d("CashwindApplication", "Manufacturer: ${android.os.Build.MANUFACTURER}")
            Log.d("CashwindApplication", "Model: ${android.os.Build.MODEL}")
            
            // Initialize database BEFORE any activities
            database = CashwindDatabase.getInstance(this)
            Log.d("CashwindApplication", "✅ Database initialized successfully in Application")
            
        } catch (e: Exception) {
            Log.e("CashwindApplication", "❌ FATAL: Database init failed", e)
            throw e
        }
    }
}
