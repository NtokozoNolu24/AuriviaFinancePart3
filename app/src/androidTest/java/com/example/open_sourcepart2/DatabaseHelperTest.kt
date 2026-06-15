package com.example.open_sourcepart2

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import java.text.SimpleDateFormat
import java.util.*

@RunWith(AndroidJUnit4::class)
class DatabaseHelperTest {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var testUser: User
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        databaseHelper = DatabaseHelper(context)
        
        // Create test user
        testUser = User(
            name = "Test User",
            email = "test@example.com",
            password = "password123"
        )
    }

    @After
    fun tearDown() {
        // Clean up - delete test data
        val db = databaseHelper.writableDatabase
        db.execSQL("DELETE FROM users WHERE email = 'test@example.com'")
        db.execSQL("DELETE FROM categories WHERE user_id = (SELECT id FROM users WHERE email = 'test@example.com')")
        db.execSQL("DELETE FROM expenses WHERE user_id = (SELECT id FROM users WHERE email = 'test@example.com')")
        db.execSQL("DELETE FROM income WHERE user_id = (SELECT id FROM users WHERE email = 'test@example.com')")
        db.close()
    }

    @Test
    fun testAddUser() {
        val userId = databaseHelper.addUser(testUser)
        assertTrue("User should be added successfully", userId > 0)
    }

    @Test
    fun testGetUser_ValidCredentials() {
        // First add user
        databaseHelper.addUser(testUser)
        
        // Then retrieve user
        val user = databaseHelper.getUser("test@example.com", "password123")
        
        assertNotNull("User should be found", user)
        assertEquals("Email should match", "test@example.com", user?.email)
        assertEquals("Name should match", "Test User", user?.name)
    }

    @Test
    fun testGetUser_InvalidCredentials() {
        databaseHelper.addUser(testUser)
        
        val user = databaseHelper.getUser("test@example.com", "wrongpassword")
        
        assertNull("User should not be found with wrong password", user)
    }

    @Test
    fun testCheckUser_UserExists() {
        databaseHelper.addUser(testUser)
        
        val exists = databaseHelper.checkUser("test@example.com")
        
        assertTrue("User should exist", exists)
    }

    @Test
    fun testCheckUser_UserDoesNotExist() {
        val exists = databaseHelper.checkUser("nonexistent@example.com")
        
        assertFalse("User should not exist", exists)
    }

    @Test
    fun testAddCategory() {
        // First add user
        val userId = databaseHelper.addUser(testUser)
        
        val category = Category(
            name = "Food",
            color = "#FF0000",
            budget = 1000.0,
            userId = userId
        )
        
        val categoryId = databaseHelper.addCategory(category)
        
        assertTrue("Category should be added successfully", categoryId > 0)
    }

    @Test
    fun testGetAllCategories() {
        val userId = databaseHelper.addUser(testUser)
        
        // Add multiple categories
        val category1 = Category(name = "Food", color = "#FF0000", budget = 1000.0, userId = userId)
        val category2 = Category(name = "Transport", color = "#00FF00", budget = 500.0, userId = userId)
        
        databaseHelper.addCategory(category1)
        databaseHelper.addCategory(category2)
        
        val categories = databaseHelper.getAllCategories(userId)
        
        assertTrue("Should have at least 2 categories", categories.size >= 2)
        assertTrue("Should contain Food category", categories.any { it.name == "Food" })
        assertTrue("Should contain Transport category", categories.any { it.name == "Transport" })
    }

    @Test
    fun testUpdateCategory() {
        val userId = databaseHelper.addUser(testUser)
        
        val category = Category(name = "Food", color = "#FF0000", budget = 1000.0, userId = userId)
        val categoryId = databaseHelper.addCategory(category)
        
        // Update category
        val updatedCategory = Category(
            id = categoryId,
            name = "Groceries",
            color = "#FF0000",
            budget = 1500.0,
            userId = userId
        )
        
        val rowsAffected = databaseHelper.updateCategory(updatedCategory)
        
        assertEquals("Should update 1 row", 1, rowsAffected)
        
        // Verify update
        val categories = databaseHelper.getAllCategories(userId)
        val updated = categories.find { it.id == categoryId }
        assertEquals("Name should be updated", "Groceries", updated?.name)
        assertEquals("Budget should be updated", 1500.0, updated?.budget, 0.01)
    }

    @Test
    fun testDeleteCategory() {
        val userId = databaseHelper.addUser(testUser)
        
        val category = Category(name = "Food", color = "#FF0000", budget = 1000.0, userId = userId)
        val categoryId = databaseHelper.addCategory(category)
        
        val rowsAffected = databaseHelper.deleteCategory(categoryId)
        
        assertEquals("Should delete 1 row", 1, rowsAffected)
        
        // Verify deletion
        val categories = databaseHelper.getAllCategories(userId)
        assertFalse("Category should be deleted", categories.any { it.id == categoryId })
    }

    @Test
    fun testAddExpense() {
        val userId = databaseHelper.addUser(testUser)
        
        // Add a category first
        val category = Category(name = "Food", color = "#FF0000", budget = 1000.0, userId = userId)
        val categoryId = databaseHelper.addCategory(category)
        
        val expense = Expense(
            amount = 150.50,
            description = "Lunch at restaurant",
            date = dateFormat.format(Date()),
            categoryId = categoryId,
            userId = userId,
            imagePath = ""
        )
        
        val expenseId = databaseHelper.addExpense(expense)
        
        assertTrue("Expense should be added successfully", expenseId > 0)
    }

    @Test
    fun testGetAllExpenses() {
        val userId = databaseHelper.addUser(testUser)
        
        // Add category
        val category = Category(name = "Food", color = "#FF0000", budget = 1000.0, userId = userId)
        val categoryId = databaseHelper.addCategory(category)
        
        // Add multiple expenses
        val expense1 = Expense(amount = 50.0, description = "Coffee", date = "2024-01-01", categoryId = categoryId, userId = userId)
        val expense2 = Expense(amount = 200.0, description = "Groceries", date = "2024-01-02", categoryId = categoryId, userId = userId)
        
        databaseHelper.addExpense(expense1)
        databaseHelper.addExpense(expense2)
        
        val expenses = databaseHelper.getAllExpenses(userId)
        
        assertTrue("Should have at least 2 expenses", expenses.size >= 2)
    }

    @Test
    fun testDeleteExpense() {
        val userId = databaseHelper.addUser(testUser)
        
        // Add category and expense
        val category = Category(name = "Food", color = "#FF0000", budget = 1000.0, userId = userId)
        val categoryId = databaseHelper.addCategory(category)
        
        val expense = Expense(amount = 50.0, description = "Coffee", date = "2024-01-01", categoryId = categoryId, userId = userId)
        val expenseId = databaseHelper.addExpense(expense)
        
        val rowsAffected = databaseHelper.deleteExpense(expenseId)
        
        assertEquals("Should delete 1 row", 1, rowsAffected)
    }

    @Test
    fun testAddIncome() {
        val userId = databaseHelper.addUser(testUser)
        
        val income = Income(
            amount = 5000.0,
            source = "Salary",
            note = "Monthly salary",
            date = dateFormat.format(Date()),
            userId = userId
        )
        
        val incomeId = databaseHelper.addIncome(income)
        
        assertTrue("Income should be added successfully", incomeId > 0)
    }

    @Test
    fun testGetIncomeByUser() {
        val userId = databaseHelper.addUser(testUser)
        
        val income1 = Income(amount = 5000.0, source = "Salary", note = "", date = "2024-01-01", userId = userId)
        val income2 = Income(amount = 500.0, source = "Freelance", note = "", date = "2024-01-15", userId = userId)
        
        databaseHelper.addIncome(income1)
        databaseHelper.addIncome(income2)
        
        val incomes = databaseHelper.getIncomeByUser(userId)
        
        assertTrue("Should have at least 2 incomes", incomes.size >= 2)
        assertTrue("Should contain salary income", incomes.any { it.source == "Salary" })
    }

    @Test
    fun testGetTotalIncomeByUser() {
        val userId = databaseHelper.addUser(testUser)
        
        databaseHelper.addIncome(Income(amount = 5000.0, source = "Salary", note = "", date = "2024-01-01", userId = userId))
        databaseHelper.addIncome(Income(amount = 500.0, source = "Freelance", note = "", date = "2024-01-15", userId = userId))
        
        val totalIncome = databaseHelper.getTotalIncomeByUser(userId)
        
        assertEquals("Total income should be 5500.0", 5500.0, totalIncome, 0.01)
    }

    @Test
    fun testGetTotalExpensesByUser() {
        val userId = databaseHelper.addUser(testUser)
        
        // Add category
        val category = Category(name = "Food", color = "#FF0000", budget = 1000.0, userId = userId)
        val categoryId = databaseHelper.addCategory(category)
        
        databaseHelper.addExpense(Expense(amount = 150.0, description = "", date = "2024-01-01", categoryId = categoryId, userId = userId))
        databaseHelper.addExpense(Expense(amount = 250.0, description = "", date = "2024-01-02", categoryId = categoryId, userId = userId))
        
        val totalExpenses = databaseHelper.getTotalExpensesByUser(userId)
        
        assertEquals("Total expenses should be 400.0", 400.0, totalExpenses, 0.01)
    }

    @Test
    fun testGetTotalExpensesByPeriod() {
        val userId = databaseHelper.addUser(testUser)
        
        // Add category
        val category = Category(name = "Food", color = "#FF0000", budget = 1000.0, userId = userId)
        val categoryId = databaseHelper.addCategory(category)
        
        databaseHelper.addExpense(Expense(amount = 100.0, description = "", date = "2024-01-01", categoryId = categoryId, userId = userId))
        databaseHelper.addExpense(Expense(amount = 200.0, description = "", date = "2024-01-15", categoryId = categoryId, userId = userId))
        databaseHelper.addExpense(Expense(amount = 300.0, description = "", date = "2024-02-01", categoryId = categoryId, userId = userId))
        
        val totalInJanuary = databaseHelper.getTotalExpensesByPeriod(userId, "2024-01-01", "2024-01-31")
        
        assertEquals("Total expenses in January should be 300.0", 300.0, totalInJanuary, 0.01)
    }

    @Test
    fun testAddBudget() {
        val userId = databaseHelper.addUser(testUser)
        
        val budget = Budget(
            amount = 5000.0,
            period = "monthly",
            startDate = "2024-01-01",
            endDate = "2024-01-31",
            userId = userId
        )
        
        val budgetId = databaseHelper.addBudget(budget)
        
        assertTrue("Budget should be added successfully", budgetId > 0)
    }

    @Test
    fun testGetBudgetByPeriod() {
        val userId = databaseHelper.addUser(testUser)
        
        val budget = Budget(
            amount = 5000.0,
            period = "monthly",
            startDate = "2024-01-01",
            endDate = "2024-01-31",
            userId = userId
        )
        
        databaseHelper.addBudget(budget)
        
        val retrievedBudget = databaseHelper.getBudgetByPeriod(userId, "monthly")
        
        assertNotNull("Budget should be found", retrievedBudget)
        assertEquals("Budget amount should match", 5000.0, retrievedBudget?.amount, 0.01)
    }
}
