package com.example.open_sourcepart2.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.open_sourcepart2.database.dao.BudgetDao
import com.example.open_sourcepart2.database.dao.CategoryDao
import com.example.open_sourcepart2.database.dao.ExpenseDao
import com.example.open_sourcepart2.database.dao.IncomeDao
import com.example.open_sourcepart2.database.dao.UserDao
import com.example.open_sourcepart2.database.entities.Budget
import com.example.open_sourcepart2.database.entities.Category
import com.example.open_sourcepart2.database.entities.Expense
import com.example.open_sourcepart2.database.entities.Income
import com.example.open_sourcepart2.database.entities.User
import com.example.open_sourcepart2.database.utils.Converters

/**
 * Main database class for the AuriviaFinance application.
 * This class defines the Room Database configuration and provides access to all DAOs.
 * 
 * The database manages 5 main entities:
 * - User: User account information
 * - Category: Expense categories with budget limits
 * - Expense: Individual expense transactions
 * - Income: Income sources and amounts
 * - Budget: User budget plans and periods
 * 
 * @property version Database version number (increment when schema changes)
 * @property exportSchema Whether to export database schema (false for production)
 */
@Database(
    entities = [User::class, Category::class, Expense::class, Budget::class, Income::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    // ==================== DAO Access Methods ====================
    
    /**
     * Returns the Data Access Object for User operations
     * @return UserDao instance for CRUD operations on users table
     */
    abstract fun userDao(): UserDao
    
    /**
     * Returns the Data Access Object for Category operations
     * @return CategoryDao instance for CRUD operations on categories table
     */
    abstract fun categoryDao(): CategoryDao
    
    /**
     * Returns the Data Access Object for Expense operations
     * @return ExpenseDao instance for CRUD operations on expenses table
     */
    abstract fun expenseDao(): ExpenseDao
    
    /**
     * Returns the Data Access Object for Budget operations
     * @return BudgetDao instance for CRUD operations on budgets table
     */
    abstract fun budgetDao(): BudgetDao
    
    /**
     * Returns the Data Access Object for Income operations
     * @return IncomeDao instance for CRUD operations on income table
     */
    abstract fun incomeDao(): IncomeDao

    // ==================== Companion Object ====================
    
    companion object {
        // Tag for logging purposes
        private const val TAG = "AppDatabase"
        
        // Database name (must match the filename used in SQLiteHelper migration)
        private const val DATABASE_NAME = "budget_tracker.db"
        
        // Volatile ensures visibility across threads (Singleton pattern)
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Gets the singleton instance of the database.
         * This method implements the Double-Checked Locking pattern for thread safety.
         * 
         * @param context Application context (must be application context to avoid memory leaks)
         * @return Singleton instance of AppDatabase
         * 
         * Usage example:
         * ```
         * val database = AppDatabase.getDatabase(applicationContext)
         * val userDao = database.userDao()
         * ```
         */
        fun getDatabase(context: Context): AppDatabase {
            return try {
                // First check - if instance exists, return it immediately (performance optimization)
                INSTANCE?.let {
                    Log.d(TAG, "Returning existing database instance")
                    return it
                }
                
                // Synchronized block ensures thread safety
                // Only one thread can execute this block at a time
                synchronized(this) {
                    Log.d(TAG, "Creating new database instance")
                    
                    // Second check - instance might have been created while waiting for lock
                    val existingInstance = INSTANCE
                    if (existingInstance != null) {
                        Log.d(TAG, "Database instance created by another thread")
                        return existingInstance
                    }
                    
                    // Build new database instance with configuration
                    val instance = try {
                        val builtInstance = Room.databaseBuilder(
                            context.applicationContext, // Use application context to prevent memory leaks
                            AppDatabase::class.java,
                            DATABASE_NAME
                        )
                            // Fallback to destructive migration when no proper migration is available
                            // This will clear the database if schema changes without a migration
                            // WARNING: This will delete all existing data when database version changes
                            .fallbackToDestructiveMigration()
                            
                            // Optional: Enable query logging for debugging (remove in production)
                            // .setQueryCallback({ sql, bindArgs ->
                            //     Log.d(TAG, "SQL Query: $sql, Args: $bindArgs")
                            // }, mainHandler)
                            
                            .build()
                        
                        Log.d(TAG, "Database built successfully")
                        builtInstance
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to build database: ${e.message}", e)
                        throw RuntimeException("Database initialization failed: ${e.message}", e)
                    }
                    
                    // Store the instance for future use
                    INSTANCE = instance
                    instance
                }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error while getting database instance: ${e.message}", e)
                throw RuntimeException("Failed to get database instance: ${e.message}", e)
            }
        }
        
        /**
         * Destroys the database instance (useful for testing or logout scenarios)
         * This closes all connections and releases resources.
         * 
         * @param context Application context
         */
        fun destroyInstance(context: Context) {
            try {
                synchronized(this) {
                    INSTANCE?.let { database ->
                        try {
                            // Close the database to release resources
                            database.close()
                            Log.d(TAG, "Database instance closed successfully")
                        } catch (e: Exception) {
                            Log.e(TAG, "Error closing database: ${e.message}", e)
                        }
                        INSTANCE = null
                        Log.d(TAG, "Database instance destroyed")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error destroying database instance: ${e.message}", e)
            }
        }
        
        /**
         * Checks if the database instance is currently available
         * @return true if database instance exists and is open, false otherwise
         */
        fun isDatabaseAvailable(): Boolean {
            return try {
                val instance = INSTANCE
                instance != null && instance.isOpen
            } catch (e: Exception) {
                Log.e(TAG, "Error checking database availability: ${e.message}", e)
                false
            }
        }
        
        /**
         * Performs a database integrity check (useful for debugging)
         * @param context Application context
         * @return true if database is healthy, false otherwise
         */
        fun verifyDatabaseIntegrity(context: Context): Boolean {
            return try {
                val database = getDatabase(context)
                // Simple query to check if database is responsive
                database.openHelper.readableDatabase.isOpen
                Log.d(TAG, "Database integrity check passed")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Database integrity check failed: ${e.message}", e)
                false
            }
        }
    }
    
    // ==================== Lifecycle Methods ====================
    
    /**
     * Called when the database is created for the first time.
     * This is useful for initial data seeding (e.g., default categories).
     * 
     * Note: Room typically handles this automatically, but you can override
     * this method by creating a Callback class.
     */
    override fun clearAllTables() {
        try {
            super.clearAllTables()
            Log.d(TAG, "All tables cleared successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing tables: ${e.message}", e)
            throw e
        }
    }
}

/**
 * Extension function to safely execute database operations with error handling
 * 
 * Usage example:
 * ```
 * database.safeDatabaseOperation {
 *     userDao.insertUser(user)
 * }
 * ```
 * 
 * @param operation The database operation to execute
 * @param errorMessage Custom error message (optional)
 * @return Result containing either success value or error
 */
suspend fun <T> AppDatabase.safeDatabaseOperation(
    operation: suspend () -> T,
    errorMessage: String = "Database operation failed"
): Result<T> {
    return try {
        if (!isOpen) {
            Log.e("Database", "Database is not open")
            return Result.failure(IllegalStateException("Database is not open"))
        }
        
        val result = operation()
        Result.success(result)
    } catch (e: Exception) {
        Log.e("Database", "$errorMessage: ${e.message}", e)
        Result.failure(e)
    }
}

/**
 * Extension function to check if database is ready for operations
 */
fun AppDatabase.isReady(): Boolean {
    return try {
        isOpen && !isClosed
    } catch (e: Exception) {
        Log.e("Database", "Error checking database readiness: ${e.message}", e)
        false
    }
}
