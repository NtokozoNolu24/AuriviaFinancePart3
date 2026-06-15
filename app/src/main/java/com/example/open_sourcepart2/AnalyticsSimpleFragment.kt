package com.example.open_sourcepart2

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.open_sourcepart2.database.AppDatabase
import com.example.open_sourcepart2.databinding.FragmentAnalyticsSimpleBinding
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class AnalyticsSimpleFragment : Fragment() {

    private var _binding: FragmentAnalyticsSimpleBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: AppDatabase
    private lateinit var sessionManager: SessionManager
    private lateinit var gamificationManager: GamificationManager
    private lateinit var categoryAnalyticsAdapter: CategoryAnalyticsAdapter

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val currencyFormat = NumberFormat.getCurrencyInstance().apply {
        currency = Currency.getInstance("ZAR")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyticsSimpleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = AppDatabase.getDatabase(requireContext())
        sessionManager = SessionManager(requireContext())
        gamificationManager = GamificationManager(requireContext())

        setupUI()
        loadAnalytics()
    }

    private fun setupUI() {
        // Setup period selector
        val periods = arrayOf("This Week", "This Month", "Last 3 Months", "This Year")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, periods)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPeriod.adapter = adapter

        // Setup RecyclerView
        binding.rvCategoryAnalytics.layoutManager = LinearLayoutManager(requireContext())
        categoryAnalyticsAdapter = CategoryAnalyticsAdapter(emptyList())
        binding.rvCategoryAnalytics.adapter = categoryAnalyticsAdapter

        binding.btnRefresh.setOnClickListener {
            loadAnalytics()
        }
    }

    private fun loadAnalytics() {
        val user = sessionManager.getUserDetails() ?: return
        val selectedPeriod = binding.spinnerPeriod.selectedItemPosition

        val (startDate, endDate) = getDateRange(selectedPeriod)
        
        lifecycleScope.launch {
            val summaries = database.expenseDao().getCategoryExpenseSummaries(user.id, startDate, endDate)
            val categoryExpenses = summaries.map {
                CategoryExpenseAnalytics(
                    categoryId = it.categoryId,
                    categoryName = it.categoryName,
                    categoryColor = it.categoryColor,
                    budget = it.budget,
                    totalSpent = it.totalSpent,
                    minGoal = it.budget * 0.7,
                    maxGoal = it.budget
                )
            }

            // Update goal progress
            updateGoalProgress(categoryExpenses)

            // Update category breakdown
            updateCategoryBreakdown(categoryExpenses)

            // Update statistics
            updateStatistics(categoryExpenses)
        }
    }

    private fun getDateRange(periodIndex: Int): Pair<Date, Date> {
        val calendar = Calendar.getInstance()
        val endDate = calendar.time

        when (periodIndex) {
            0 -> calendar.add(Calendar.DAY_OF_YEAR, -7)
            1 -> calendar.add(Calendar.MONTH, -1)
            2 -> calendar.add(Calendar.MONTH, -3)
            3 -> calendar.add(Calendar.YEAR, -1)
        }

        val startDate = calendar.time
        return Pair(startDate, endDate)
    }

    private fun updateGoalProgress(categoryExpenses: List<CategoryExpenseAnalytics>) {
        val totalSpent = categoryExpenses.sumOf { it.totalSpent }
        val totalBudget = categoryExpenses.sumOf { it.budget }
        val totalMinGoal = categoryExpenses.sumOf { it.minGoal }

        val progressPercentage = if (totalBudget > 0) {
            ((totalSpent / totalBudget) * 100).toInt()
        } else 0

        binding.progressGoal.progress = progressPercentage.coerceAtMost(100)
        binding.tvProgressText.text = "$progressPercentage% of budget used"

        // Update goal status
        val goalStatus = when {
            totalSpent <= totalMinGoal -> {
                binding.tvGoalStatus.setTextColor(Color.parseColor("#4CAF50"))
                "🎯 Excellent! Under minimum goal"
            }
            totalSpent <= totalBudget -> {
                binding.tvGoalStatus.setTextColor(Color.parseColor("#FF9800"))
                "⚠️ Good! Within budget range"
            }
            else -> {
                binding.tvGoalStatus.setTextColor(Color.parseColor("#F44336"))
                "🚨 Over budget! Consider reducing expenses"
            }
        }

        binding.tvGoalStatus.text = goalStatus

        // Check for achievements
        gamificationManager.checkBudgetAchievements(totalSpent, totalBudget, totalMinGoal)
    }

    private fun updateCategoryBreakdown(categoryExpenses: List<CategoryExpenseAnalytics>) {
        categoryAnalyticsAdapter.updateCategories(categoryExpenses)
    }

    private fun updateStatistics(categoryExpenses: List<CategoryExpenseAnalytics>) {
        val totalSpent = categoryExpenses.sumOf { it.totalSpent }
        val avgSpent = if (categoryExpenses.isNotEmpty()) totalSpent / categoryExpenses.size else 0.0
        val highestCategory = categoryExpenses.maxByOrNull { it.totalSpent }

        binding.tvTotalSpent.text = "Total Spent: ${currencyFormat.format(totalSpent)}"
        binding.tvAvgSpent.text = "Average per Category: ${currencyFormat.format(avgSpent)}"
        binding.tvHighestCategory.text = "Highest: ${highestCategory?.categoryName ?: "N/A"}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}