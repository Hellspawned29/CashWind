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
    version = 11
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

        // Migration from version 1 to 2 - Initial schema to add recurring fields
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE bills ADD COLUMN recurring INTEGER DEFAULT 0")
                database.execSQL("ALTER TABLE bills ADD COLUMN frequency TEXT")
            }
        }

        // Migration from version 2 to 3 - Add category to bills
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE bills ADD COLUMN category TEXT")
            }
        }

        // Migration from version 3 to 4 - Add notes to bills
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE bills ADD COLUMN notes TEXT")
            }
        }

        // Migration from version 4 to 5 - Add createdAt timestamps
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE bills ADD COLUMN createdAt TEXT")
                database.execSQL("ALTER TABLE accounts ADD COLUMN createdAt TEXT")
                database.execSQL("ALTER TABLE transactions ADD COLUMN createdAt TEXT")
                database.execSQL("ALTER TABLE budgets ADD COLUMN createdAt TEXT")
            }
        }

        // Migration from version 5 to 6 - Add syncedAt timestamps for all tables
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                val timestamp = System.currentTimeMillis()
                database.execSQL("ALTER TABLE bills ADD COLUMN syncedAt INTEGER DEFAULT $timestamp")
                database.execSQL("ALTER TABLE accounts ADD COLUMN syncedAt INTEGER DEFAULT $timestamp")
                database.execSQL("ALTER TABLE transactions ADD COLUMN syncedAt INTEGER DEFAULT $timestamp")
                database.execSQL("ALTER TABLE budgets ADD COLUMN syncedAt INTEGER DEFAULT $timestamp")
                database.execSQL("ALTER TABLE goals ADD COLUMN syncedAt INTEGER DEFAULT $timestamp")
                database.execSQL("ALTER TABLE users ADD COLUMN syncedAt INTEGER DEFAULT $timestamp")
            }
        }

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
                // Check if webLink column already exists
                val cursor = database.query("PRAGMA table_info(bills)")
                var columnExists = false
                while (cursor.moveToNext()) {
                    val columnName = cursor.getString(cursor.getColumnIndex("name"))
                    if (columnName == "webLink") {
                        columnExists = true
                        break
                    }
                }
                cursor.close()
                
                // Only add column if it doesn't exist
                if (!columnExists) {
                    database.execSQL("ALTER TABLE bills ADD COLUMN webLink TEXT")
                }
            }
        }

        // Migration from version 9 to 10 - adds hasPastDue and pastDueAmount to bills
        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Check if columns already exist
                val cursor = database.query("PRAGMA table_info(bills)")
                val existingColumns = mutableSetOf<String>()
                while (cursor.moveToNext()) {
                    existingColumns.add(cursor.getString(cursor.getColumnIndex("name")))
                }
                cursor.close()
                
                // Add hasPastDue column if it doesn't exist (NOT NULL with default)
                if (!existingColumns.contains("hasPastDue")) {
                    database.execSQL("ALTER TABLE bills ADD COLUMN hasPastDue INTEGER NOT NULL DEFAULT 0")
                }
                
                // Add pastDueAmount column if it doesn't exist (NOT NULL with default)
                if (!existingColumns.contains("pastDueAmount")) {
                    database.execSQL("ALTER TABLE bills ADD COLUMN pastDueAmount REAL NOT NULL DEFAULT 0.0")
                }
            }
        }

        // Migration from version 10 to 11 - fix hasPastDue and pastDueAmount to be NOT NULL, create bill_payment_allocations
        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create new bills table with correct schema
                database.execSQL("""
                    CREATE TABLE bills_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        amount REAL NOT NULL,
                        dueDate TEXT NOT NULL,
                        isPaid INTEGER NOT NULL,
                        lastPaidAt TEXT,
                        category TEXT,
                        recurring INTEGER NOT NULL,
                        frequency TEXT,
                        notes TEXT,
                        webLink TEXT,
                        accountId INTEGER,
                        linkedTransactionId INTEGER,
                        hasPastDue INTEGER NOT NULL DEFAULT 0,
                        pastDueAmount REAL NOT NULL DEFAULT 0.0,
                        createdAt TEXT,
                        syncedAt INTEGER NOT NULL
                    )
                """.trimIndent())
                
                // Copy data from old table to new table
                database.execSQL("""
                    INSERT INTO bills_new 
                    SELECT id, userId, name, amount, dueDate, isPaid, lastPaidAt, category, 
                           recurring, frequency, notes, webLink, accountId, linkedTransactionId,
                           COALESCE(hasPastDue, 0), COALESCE(pastDueAmount, 0.0), createdAt, syncedAt
                    FROM bills
                """.trimIndent())
                
                // Drop old table
                database.execSQL("DROP TABLE bills")
                
                // Rename new table to bills
                database.execSQL("ALTER TABLE bills_new RENAME TO bills")
                
                // Create bill_payment_allocations table if it doesn't exist
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS bill_payment_allocations (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        billId INTEGER NOT NULL,
                        userId INTEGER NOT NULL,
                        allocatedAmount REAL NOT NULL,
                        paidAmount REAL NOT NULL,
                        allocationDate TEXT NOT NULL,
                        paycheckDate TEXT,
                        notes TEXT,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        fun getInstance(context: Context): CashwindDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    CashwindDatabase::class.java,
                    "cashwind_db"
                )
                    .addMigrations(
                        MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6,
                        MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11
                    )
                    .build()
                    .also { instance = it }
            }
        }
    }
}
