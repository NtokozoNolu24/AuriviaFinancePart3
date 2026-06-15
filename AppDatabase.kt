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
import com.example.open_sourcepart2.database.dao.GoalDao

import com.example.open_sourcepart2.database.entities.Budget
import com.example.open_sourcepart2.database.entities.Category
import com.example.open_sourcepart2.database.entities.Expense
import com.example.open_sourcepart2.database.entities.Income
import com.example.open_sourcepart2.database.entities.User
import com.example.open_sourcepart2.database.entities.Goal

import com.example.open_sourcepart2.database.utils.Converters


@Database(
    entities = [
        User::class,
        Category::class,
        Expense::class,
        Budget::class,
        Income::class,
        Goal::class
    ],
    version = 6,
    exportSchema = false
)

@TypeConverters(Converters::class)

abstract class AppDatabase : RoomDatabase() {


    abstract fun userDao(): UserDao

    abstract fun categoryDao(): CategoryDao

    abstract fun expenseDao(): ExpenseDao

    abstract fun budgetDao(): BudgetDao

    abstract fun incomeDao(): IncomeDao

    // NEW GOALS FEATURE
    abstract fun goalDao(): GoalDao



    companion object {


        private const val TAG = "AppDatabase"

        private const val DATABASE_NAME = "budget_tracker.db"



        @Volatile
        private var INSTANCE: AppDatabase? = null



        fun getDatabase(context: Context): AppDatabase {


            return INSTANCE ?: synchronized(this) {


                val instance = Room.databaseBuilder(

                    context.applicationContext,

                    AppDatabase::class.java,

                    DATABASE_NAME

                )

                    // handles version change
                    .fallbackToDestructiveMigration()

                    .build()



                INSTANCE = instance


                Log.d(
                    TAG,
                    "Database created successfully"
                )


                instance

            }

        }



        fun destroyInstance(){

            synchronized(this){

                INSTANCE?.close()

                INSTANCE = null

            }

        }



        fun isDatabaseAvailable(): Boolean {


            return try {


                INSTANCE != null &&
                        INSTANCE!!.isOpen


            } catch(e:Exception){


                false

            }


        }


    }

}





// ==============================
// DATABASE SAFE OPERATION
// ==============================


suspend fun <T> AppDatabase.safeDatabaseOperation(

    operation: suspend () -> T,

    errorMessage:String = "Database operation failed"

):Result<T>{



    return try{


        val result = operation()


        Result.success(result)



    }catch(e:Exception){


        Log.e(
            "Database",
            "$errorMessage ${e.message}"
        )


        Result.failure(e)


    }



}





fun AppDatabase.isReady():Boolean{


    return try{


        isOpen


    }catch(e:Exception){


        false

    }


}