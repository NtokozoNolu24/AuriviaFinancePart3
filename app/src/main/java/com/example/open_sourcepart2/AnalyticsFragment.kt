package com.example.open_sourcepart2

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.open_sourcepart2.database.AppDatabase
import com.example.open_sourcepart2.databinding.FragmentAnalyticsBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.anychart.AnyChart
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.charts.Pie
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.util.Pair as AndroidPair

class AnalyticsFragment : Fragment() {

    private var _binding: FragmentAnalyticsBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: AppDatabase
    private lateinit var sessionManager: SessionManager
    private lateinit var gamificationManager: GamificationManager
    private lateinit var categoryAnalyticsAdapter: CategoryAnalyticsAdapter

    private var customStartDate: Date? = null
    private var customEndDate: Date? = null

    private var pie: Pie? = null

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val currencyFormat = NumberFormat.getCurrencyInstance().apply {
        currency = Currency.getInstance("ZAR")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyticsBinding.inflate(inflater, container, false)
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
        // Initialize Pie Chart
        pie = AnyChart.pie()
        binding.anyChartView.setChart(pie)

        // Setup period selector
        val periods = arrayOf("This Week", "This Month", "Last 3 Months", "This Year")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, periods)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPeriod.adapter = adapter

        // Setup RecyclerView for category breakdown
        binding.rvCategoryBreakdown.layoutManager = LinearLayoutManager(requireContext())
        categoryAnalyticsAdapter = CategoryAnalyticsAdapter(emptyList())
        binding.rvCategoryBreakdown.adapter = categoryAnalyticsAdapter

        binding.btnRefresh.setOnClickListener {
            loadAnalytics()
        }

        binding.btnCustomRange.setOnClickListener {
            showDateRangePicker()
        }
    }

    private fun showDateRangePicker() {
        val builder = MaterialDatePicker.Builder.dateRangePicker()
        builder.setTitleText("Select date range")
        val picker = builder.build()
        picker.show(childFragmentManager, picker.toString())

        picker.addOnPositiveButtonClickListener { range ->
            customStartDate = Date(range.first)
            customEndDate = Date(range.second)
            loadAnalytics()
        }
    }

    private fun loadAnalytics() {
        val user = sessionManager.getUserDetails() ?: return
        val selectedPeriod = binding.spinnerPeriod.selectedItemPosition

        val (startDate, endDate) = if (customStartDate != null && customEndDate != null) {
            Pair(customStartDate!!, customEndDate!!)
        } else {
            getDateRange(selectedPeriod)
        }
        
        lifecycleScope.launch {
            val budget = database.budgetDao().getBudgetByPeriod(user.id, "monthly")
            val summaries = database.expenseDao().getCategoryExpenseSummaries(user.id, startDate, endDate)
            val categoryExpenses = summaries.map {
                CategoryExpenseAnalytics(
                    categoryId = it.categoryId,
                    categoryName = it.categoryName,
                    categoryColor = it.categoryColor,
                    budget = it.budget,
                    totalSpent = it.totalSpent,
                    minGoal = it.budget * 0.7, // Keep this as fallback or use something else
                    maxGoal = it.budget
                )
            }

            // Update goal progress with actual goals
            if (budget != null) {
                updateGoalProgress(categoryExpenses, budget.minAmount, budget.amount)
            } else {
                updateGoalProgress(categoryExpenses)
            }

            // Update category breakdown
            updateCategoryBreakdown(categoryExpenses)

            // Update statistics
            updateStatistics(categoryExpenses)

            // Update Chart
            updateChart(categoryExpenses)
        }
    }

    private fun updateChart(categoryExpenses: List<CategoryExpenseAnalytics>) {
        val data = categoryExpenses.map { 
            ValueDataEntry(it.categoryName, it.totalSpent)
        }
        
        pie?.data(data)
        pie?.title("Spending by Category")
        pie?.background()?.fill("#1E1E1E")
        pie?.labels()?.position("outside")
        pie?.legend()?.title()?.enabled(true)
        pie?.legend()?.title()?.text("Categories")
        pie?.legend()?.position("bottom")
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

    private fun updateGoalProgress(categoryExpenses: List<CategoryExpenseAnalytics>, customMinGoal: Double? = null, customMaxGoal: Double? = null) {
        val totalSpent = categoryExpenses.sumOf { it.totalSpent }
        val totalBudget = customMaxGoal ?: categoryExpenses.sumOf { it.budget }
        val totalMinGoal = customMinGoal ?: categoryExpenses.sumOf { it.minGoal }

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
        if (categoryExpenses.isEmpty()) {
            binding.tvNoCategoryData.visibility = View.VISIBLE
            binding.rvCategoryBreakdown.visibility = View.GONE
        } else {
            binding.tvNoCategoryData.visibility = View.GONE
            binding.rvCategoryBreakdown.visibility = View.VISIBLE
            categoryAnalyticsAdapter.updateCategories(categoryExpenses)
        }
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