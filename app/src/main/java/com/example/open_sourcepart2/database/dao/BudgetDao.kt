package com.example.open_sourcepart2.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.open_sourcepart2.database.entities.Budget

@Dao
interface BudgetDao {

    @Insert
    suspend fun insertBudget(budget: Budget): Long

    @Update
    suspend fun updateBudget(budget: Budget): Int

    @Delete
    suspend fun deleteBudget(budget: Budget): Int

    @Query("SELECT * FROM budgets WHERE userId = :userId AND period = :period LIMIT 1")
    suspend fun getBudgetByPeriod(userId: Int, period: String): Budget?

    @Query("SELECT * FROM budgets WHERE userId = :userId")
    suspend fun getAllBudgets(userId: Int): List<Budget>
}