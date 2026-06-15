package com.example.open_sourcepart2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.open_sourcepart2.databinding.FragmentExpensesBinding
import android.app.DatePickerDialog
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.open_sourcepart2.database.AppDatabase
import com.example.open_sourcepart2.database.entities.Category
import com.example.open_sourcepart2.database.entities.Expense
import com.example.open_sourcepart2.databinding.DialogAddExpenseBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ExpensesFragment : Fragment() {

    private var _binding: FragmentExpensesBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: AppDatabase
    private lateinit var sessionManager: SessionManager
    private lateinit var gamificationManager: GamificationManager
    private lateinit var expenseAdapter: ExpenseAdapter

    private val displayDateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    private var categoriesList = listOf<Category>()
    private var selectedDate: Date = Calendar.getInstance().time

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpensesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = AppDatabase.getDatabase(requireContext())
        sessionManager = SessionManager(requireContext())
        gamificationManager = GamificationManager(requireContext())

        setupUI()
        loadData()
    }

    private fun setupUI() {
        binding.rvExpenses.layoutManager = LinearLayoutManager(requireContext())
        expenseAdapter = ExpenseAdapter(emptyList())
        binding.rvExpenses.adapter = expenseAdapter

        // Setup date spinner
        val dateOptions = arrayOf("All Time", "Today", "This Week", "This Month", "This Year")
        val dateAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, dateOptions)
        dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDate.adapter = dateAdapter

        binding.fabAddExpense.setOnClickListener {
            showAddExpenseDialog()
        }

        binding.btnApplyFilters.setOnClickListener {
            applyFilters()
        }
    }

    private fun loadData() {
        val user = sessionManager.getUserDetails() ?: return

        lifecycleScope.launch {
            // Load categories for spinner
            categoriesList = database.categoryDao().getAllCategories(user.id)
            val categoryNames = mutableListOf("All Categories")
            categoryNames.addAll(categoriesList.map { it.name })

            val categoryAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                categoryNames
            )
            categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerCategory.adapter = categoryAdapter

            // Load expenses
            val expenses = database.expenseDao().getAllExpensesWithCategory(user.id)
            if (expenses.isEmpty()) {
                binding.tvNoExpenses.visibility = View.VISIBLE
                binding.rvExpenses.visibility = View.GONE
            } else {
                binding.tvNoExpenses.visibility = View.GONE
                binding.rvExpenses.visibility = View.VISIBLE
                expenseAdapter.updateExpenses(expenses)
            }
        }
    }

    private fun showAddExpenseDialog() {
        val dialogBinding = DialogAddExpenseBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        // Setup category spinner
        val categoryNames = categoriesList.map { it.name }
        if (categoryNames.isEmpty()) {
            Toast.makeText(requireContext(), "Please add categories first", Toast.LENGTH_SHORT).show()
            return
        }

        val categoryAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categoryNames
        )
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerCategory.adapter = categoryAdapter

        // Setup date selection
        dialogBinding.btnSelectDate.text = "Date: ${displayDateFormat.format(selectedDate)}"
        dialogBinding.btnSelectDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.time = selectedDate

            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    selectedDate = calendar.time
                    dialogBinding.btnSelectDate.text = "Date: ${displayDateFormat.format(selectedDate)}"
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnSave.setOnClickListener {
            val amountStr = dialogBinding.etAmount.text.toString()
            val description = dialogBinding.etDescription.text.toString()
            val categoryPosition = dialogBinding.spinnerCategory.selectedItemPosition

            if (amountStr.isEmpty() || description.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val amount = amountStr.toDouble()
                val user = sessionManager.getUserDetails() ?: return@setOnClickListener
                val category = categoriesList[categoryPosition]

                lifecycleScope.launch {
                    val expense = Expense(
                        amount = amount,
                        description = description,
                        date = selectedDate,
                        categoryId = category.id,
                        userId = user.id
                    )

                    val id = database.expenseDao().insertExpense(expense)
                    if (id > 0) {
                        // Check for budget warning
                        checkBudgetWarning(user.id, category)

                        // Log gamification activity
                        gamificationManager.logExpenseActivity()
                        gamificationManager.checkFirstExpenseAchievement()

                        Toast.makeText(requireContext(), "Expense added successfully", Toast.LENGTH_SHORT).show()
                        loadData() // Reload data
                        dialog.dismiss()
                    } else {
                        Toast.makeText(requireContext(), "Failed to add expense", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Invalid amount", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun checkBudgetWarning(userId: Int, category: Category) {
        lifecycleScope.launch {
            // Category budget check
            val categoryTotal = database.expenseDao().getTotalByCategory(userId, category.id) ?: 0.0
            if (categoryTotal > category.budget) {
                Toast.makeText(requireContext(), "⚠️ Warning: You've exceeded the budget for ${category.name}!", Toast.LENGTH_LONG).show()
            }

            // Monthly budget check
            val budget = database.budgetDao().getBudgetByPeriod(userId, "monthly")
            if (budget != null) {
                val totalSpent = database.expenseDao().getTotalExpenses(userId) ?: 0.0
                if (totalSpent > budget.amount) {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Budget Exceeded")
                        .setMessage("You have exceeded your monthly budget of ${budget.amount}. Current spending: $totalSpent")
                        .setPositiveButton("OK", null)
                        .show()
                } else if (totalSpent > budget.amount * 0.9) {
                    Toast.makeText(requireContext(), "⚠️ You have used 90% of your monthly budget!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun applyFilters() {
        val user = sessionManager.getUserDetails() ?: return
        val categoryPosition = binding.spinnerCategory.selectedItemPosition
        val datePosition = binding.spinnerDate.selectedItemPosition

        lifecycleScope.launch {
            // Get all expenses
            var filteredExpenses = database.expenseDao().getAllExpensesWithCategory(user.id)

            // Apply category filter
            if (categoryPosition > 0) { // 0 is "All Categories"
                val category = categoriesList[categoryPosition - 1]
                filteredExpenses = filteredExpenses.filter { it.categoryId == category.id }
            }

            // Apply date filter
            val calendar = Calendar.getInstance()
            when (datePosition) {
                1 -> { // Today
                    val today = calendar.time
                    filteredExpenses = filteredExpenses.filter { isSameDay(it.date, today) }
                }
                2 -> { // This Week
                    calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                    val weekStart = calendar.time
                    calendar.add(Calendar.DAY_OF_WEEK, 6)
                    val weekEnd = calendar.time
                    filteredExpenses = filteredExpenses.filter { it.date in weekStart..weekEnd }
                }
                3 -> { // This Month
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    val monthStart = calendar.time
                    calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                    val monthEnd = calendar.time
                    filteredExpenses = filteredExpenses.filter { it.date in monthStart..monthEnd }
                }
                4 -> { // This Year
                    calendar.set(Calendar.DAY_OF_YEAR, 1)
                    val yearStart = calendar.time
                    calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR))
                    val yearEnd = calendar.time
                    filteredExpenses = filteredExpenses.filter { it.date in yearStart..yearEnd }
                }
            }

            // Update UI
            if (filteredExpenses.isEmpty()) {
                binding.tvNoExpenses.visibility = View.VISIBLE
                binding.rvExpenses.visibility = View.GONE
            } else {
                binding.tvNoExpenses.visibility = View.GONE
                binding.rvExpenses.visibility = View.VISIBLE
                expenseAdapter.updateExpenses(filteredExpenses)
            }
        }
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance()
        cal1.time = date1
        val cal2 = Calendar.getInstance()
        cal2.time = date2
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}