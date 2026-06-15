package com.example.open_sourcepart2

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.example.open_sourcepart2.database.AppDatabase
import com.example.open_sourcepart2.databinding.FragmentProfileBinding
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: AppDatabase
    private lateinit var sessionManager: SessionManager
    private lateinit var gamificationManager: GamificationManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = AppDatabase.getDatabase(requireContext())
        sessionManager = SessionManager(requireContext())
        gamificationManager = GamificationManager(requireContext())

        loadUserInfo()
        setupLogoutButton()
    }

    private fun loadUserInfo() {
        val user = sessionManager.getUserDetails()
        if (user != null) {
            binding.tvUserName.text = user.name
            binding.tvUserEmail.text = user.email

            lifecycleScope.launch {
                // Load user stats
                val totalExpensesList = database.expenseDao().getAllExpenses(user.id)
                val categories = database.categoryDao().getAllCategories(user.id)
                val totalPoints = gamificationManager.getTotalPoints()
                val currentLevel = gamificationManager.getUserLevel()
                val currentStreak = gamificationManager.getCurrentStreak()

                val currencyFormat = NumberFormat.getCurrencyInstance().apply {
                    currency = Currency.getInstance("ZAR")
                }

                val totalSpent = totalExpensesList.sumOf { it.amount }

                binding.tvTotalExpenses.text = "💰 Total Spent: ${currencyFormat.format(totalSpent)}"
                binding.tvCategoriesCount.text = "📁 Categories: ${categories.size}"
                binding.tvTotalPoints.text = "⭐ Total Points: $totalPoints"
                binding.tvCurrentLevel.text = "🏆 Level: $currentLevel - ${gamificationManager.getLevelTitle()}"
                binding.tvCurrentStreak.text = "🔥 Streak: $currentStreak days"
            }
        }
    }

    private fun setupLogoutButton() {
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                performLogout()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun performLogout() {
        // Clear session
        sessionManager.logout()

        // Show logout message
        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()

        // Navigate to Login Activity
        val intent = Intent(requireActivity(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}