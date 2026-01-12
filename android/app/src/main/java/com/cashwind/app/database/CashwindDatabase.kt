package com.cashwind.app.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.cashwind.app.database.entity.*
import com.cashwind.app.database.dao.*

@Database(
    entities = [
        UserEntity::class,
        BillEntity::class,
        AccountEntity::class,
        TransactionEntity::class,
        BudgetEntity::class,
        GoalEntity::class,
        PaycheckSettingsEntity::class,
        BillReminderEntity::class,
        BillPaymentAllocationEntity::class
    ],
    version = 10
)
abstract class CashwindDatabase : RoomDatabase() {
    abstract fun billDao(): BillDao
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun goalDao(): GoalDao
    abstract fun userDao(): UserDao
    abstract fun paycheckSettingsDao(): PaycheckSettingsDao
    abstract fun billReminderDao(): BillReminderDao
    abstract fun billPaymentAllocationDao(): BillPaymentAllocationDao

    companion object {
        @Volatile
        private var instance: CashwindDatabase? = null

        // Migration from version 6 to 7 - adds accountId and linkedTransactionId to bills
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new columns to bills table (NULL by default to preserve existing data)
                database.execSQL("ALTER TABLE bills ADD COLUMN accountId INTEGER")
                database.execSQL("ALTER TABLE bills ADD COLUMN linkedTransactionId INTEGER")
            }
        }

        // Migration from version 7 to 8 - adds lastPaidAt to bills
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add lastPaidAt column to bills table
                database.execSQL("ALTER TABLE bills ADD COLUMN lastPaidAt TEXT")
            }
        }

        // Migration from version 8 to 9 - adds webLink to bills
        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add webLink column to bills table
                database.execSQL("ALTER TABLE bills ADD COLUMN webLink TEXT")
            }
        }

        // Migration from version 9 to 10 - adds hasPastDue and pastDueAmount to bills
        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add hasPastDue and pastDueAmount columns to bills table
                database.execSQL("ALTER TABLE bills ADD COLUMN hasPastDue INTEGER DEFAULT 0")
                database.execSQL("ALTER TABLE bills ADD COLUMN pastDueAmount REAL DEFAULT 0.0")
            }
        }

        fun getInstance(context: Context): CashwindDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    CashwindDatabase::class.java,
                    "cashwind_db"
                )
                    .addMigrations(MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }
    }
}
