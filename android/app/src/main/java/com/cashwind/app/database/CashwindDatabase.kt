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
        BillReminderEntity::class
    ],
    version = 7
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

        fun getInstance(context: Context): CashwindDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    CashwindDatabase::class.java,
                    "cashwind_db"
                )
                    .addMigrations(MIGRATION_6_7)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }
    }
}
