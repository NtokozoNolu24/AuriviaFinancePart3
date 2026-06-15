package com.example.open_sourcepart2.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.open_sourcepart2.CategoryExpenseSummary
import com.example.open_sourcepart2.ExpenseWithCategory
import com.example.open_sourcepart2.database.entities.Expense
import java.util.Date

@Dao
interface ExpenseDao {

    @Insert
    suspend fun insertExpense(expense: Expense): Long

    @Update
    suspend fun updateExpense(expense: Expense): Int

    @Delete
    suspend fun deleteExpense(expense: Expense): Int

    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY date DESC")
    suspend fun getAllExpenses(userId: Int): List<Expense>

    @Query("""
        SELECT e.id, e.amount, e.description, e.date, e.categoryId, 
               c.name as categoryName, c.color as categoryColor
        FROM expenses e
        JOIN categories c ON e.categoryId = c.id
        WHERE e.userId = :userId
        ORDER BY e.date DESC
    """)
    suspend fun getAllExpensesWithCategory(userId: Int): List<ExpenseWithCategory>

    @Query("SELECT * FROM expenses WHERE userId = :userId AND categoryId = :categoryId ORDER BY date DESC")
    suspend fun getExpensesByCategory(userId: Int, categoryId: Int): List<Expense>

    @Query("SELECT * FROM expenses WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getExpensesByDateRange(userId: Int, startDate: Date, endDate: Date): List<Expense>

    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId AND categoryId = :categoryId")
    suspend fun getTotalByCategory(userId: Int, categoryId: Int): Double?

    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId")
    suspend fun getTotalExpenses(userId: Int): Double?

    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId AND date BETWEEN :startDate AND :endDate")
    suspend fun getTotalExpensesByDateRange(userId: Int, startDate: Date, endDate: Date): Double?

    @Query("""
        SELECT c.id as categoryId, c.name as categoryName, c.color as categoryColor, c.budget as budget, 
               COALESCE(SUM(e.amount), 0) as totalSpent
        FROM categories c
        LEFT JOIN expenses e ON c.id = e.categoryId 
            AND e.date BETWEEN :startDate AND :endDate 
            AND e.userId = :userId
        WHERE c.userId = :userId
        GROUP BY c.id, c.name, c.color, c.budget
        HAVING totalSpent > 0
    """)
    suspend fun getCategoryExpenseSummaries(userId: Int, startDate: Date, endDate: Date): List<CategoryExpenseSummary>
}