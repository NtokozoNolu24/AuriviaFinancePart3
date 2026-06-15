package com.example.open_sourcepart2.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.open_sourcepart2.database.entities.Income

@Dao
interface IncomeDao {

    @Insert
    suspend fun insertIncome(income: Income): Long

    @Update
    suspend fun updateIncome(income: Income): Int

    @Delete
    suspend fun deleteIncome(income: Income): Int

    @Query("SELECT * FROM income WHERE userId = :userId ORDER BY date DESC")
    suspend fun getAllIncome(userId: Int): List<Income>

    @Query("SELECT SUM(amount) FROM income WHERE userId = :userId")
    suspend fun getTotalIncome(userId: Int): Double?
}