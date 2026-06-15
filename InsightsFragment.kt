package com.example.open_sourcepart2

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.open_sourcepart2.databinding.FragmentInsightsBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class InsightsFragment : Fragment() {

    private var _binding: FragmentInsightsBinding? = null
    private val binding get() = _binding!!

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager

    private val currencyFormat = NumberFormat.getCurrencyInstance().apply {
        currency = Currency.getInstance("ZAR")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInsightsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        databaseHelper = DatabaseHelper(requireContext())
        sessionManager = SessionManager(requireContext())

        loadInsights()
    }

    private fun loadInsights() {
        val user = sessionManager.getUserDetails() ?: return

        // Calculate monthly insights
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        // Get current month expenses
        calendar.set(currentYear, currentMonth, 1)
        val monthStart = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        calendar.set(currentYear, currentMonth, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        val monthEnd = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

        val monthlyExpenses = databaseHelper.getTotalExpensesByPeriod(user.id, monthStart, monthEnd)
        val monthlyIncome = databaseHelper.getTotalIncomeByUser(user.id)

        // Get budget
        val monthlyBudget = databaseHelper.getBudgetByPeriod(user.id, "monthly")?.amount ?: 0.0

        // Calculate insights
        val savingsRate = if (monthlyIncome > 0) {
            ((monthlyIncome - monthlyExpenses) / monthlyIncome * 100)
        } else 0.0

        val budgetUsage = if (monthlyBudget > 0) {
            (monthlyExpenses / monthlyBudget * 100)
        } else 0.0

        // Update UI
        binding.tvMonthlyIncome.text = "Monthly Income: ${currencyFormat.format(monthlyIncome)}"
        binding.tvMonthlyExpenses.text = "Monthly Expenses: ${currencyFormat.format(monthlyExpenses)}"
        binding.tvMonthlySavings.text = "Monthly Savings: ${currencyFormat.format(monthlyIncome - monthlyExpenses)}"

        binding.tvSavingsRate.text = "Savings Rate: ${String.format("%.1f", savingsRate)}%"
        binding.progressSavings.progress = savingsRate.toInt().coerceIn(0, 100)

        binding.tvBudgetUsage.text = "Budget Usage: ${String.format("%.1f", budgetUsage)}%"
        binding.progressBudget.progress = budgetUsage.toInt().coerceIn(0, 100)

        // Set progress bar colors based on performance
        when {
            savingsRate >= 20 -> binding.progressSavings.progressTintList =
                android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50"))
            savingsRate >= 10 -> binding.progressSavings.progressTintList =
                android.content.res.ColorStateList.valueOf(Color.parseColor("#FF9800"))
            else -> binding.progressSavings.progressTintList =
                android.content.res.ColorStateList.valueOf(Color.parseColor("#F44336"))
        }

        when {
            budgetUsage <= 80 -> binding.progressBudget.progressTintList =
                android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50"))
            budgetUsage <= 100 -> binding.progressBudget.progressTintList =
                android.content.res.ColorStateList.valueOf(Color.parseColor("#FF9800"))
            else -> binding.progressBudget.progressTintList =
                android.content.res.ColorStateList.valueOf(Color.parseColor("#F44336"))
        }

        // Generate insights text
        generateInsightsText(savingsRate, budgetUsage, monthlyExpenses, monthlyBudget)
    }

    private fun generateInsightsText(savingsRate: Double, budgetUsage: Double, expenses: Double, budget: Double) {
        val insights = mutableListOf<String>()

        // Savings insights
        when {
            savingsRate >= 20 -> insights.add("🎉 Excellent! You're saving over 20% of your income.")
            savingsRate >= 10 -> insights.add("👍 Good job! You're saving ${String.format("%.1f", savingsRate)}% of your income.")
            savingsRate > 0 -> insights.add("💡 You're saving ${String.format("%.1f", savingsRate)}%. Try to increase this to 10-20%.")
            else -> insights.add("⚠️ You're not saving money this month. Consider reducing expenses.")
        }

        // Budget insights
        when {
            budgetUsage <= 80 -> insights.add("🎯 Great! You're well within your budget.")
            budgetUsage <= 100 -> insights.add("⚠️ You're close to your budget limit. Monitor spending carefully.")
            else -> insights.add("🚨 You've exceeded your budget by ${String.format("%.1f", budgetUsage - 100)}%.")
        }

        // Spending insights
        if (budget > 0) {
            val remaining = budget - expenses
            if (remaining > 0) {
                insights.add("💰 You have ${currencyFormat.format(remaining)} left in your budget.")
            }
        }

        binding.tvInsights.text = insights.joinToString("\n\n")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}