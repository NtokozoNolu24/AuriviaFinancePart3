package com.example.open_sourcepart2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.open_sourcepart2.databinding.FragmentHomeBinding
import java.text.NumberFormat
import java.util.*
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.open_sourcepart2.database.AppDatabase
import com.example.open_sourcepart2.database.entities.Income
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: AppDatabase
    private lateinit var sessionManager: SessionManager
    private lateinit var gamificationManager: GamificationManager
    private lateinit var expenseAdapter: ExpenseAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = AppDatabase.getDatabase(requireContext())
        sessionManager = SessionManager(requireContext())
        gamificationManager = GamificationManager(requireContext())

        setupUI()
        loadData()
        setupClickListeners()
    }

    private fun setupUI() {
        val user = sessionManager.getUserDetails()
        binding.tvWelcome.text = "Welcome, ${user?.name ?: "User"}!"

        // Set profile info on the profile card
        binding.tvProfileName.text = user?.name ?: "User Name"
        binding.tvProfileEmail.text = user?.email ?: "user@example.com"

        binding.rvRecentExpenses.layoutManager = LinearLayoutManager(requireContext())
        expenseAdapter = ExpenseAdapter(emptyList())
        binding.rvRecentExpenses.adapter = expenseAdapter
    }

    private fun setupClickListeners() {
        // Income button
        binding.btnIncome.setOnClickListener {
            showAddIncomeDialog()
        }

        // Expense button
        binding.btnOutcome.setOnClickListener {
            findNavController().navigate(R.id.navigation_expenses)
        }

        // Rewards card - Navigate to Gamification
        binding.cardRewards.setOnClickListener {
            findNavController().navigate(R.id.navigation_gamification)
        }

        // Insights card - Navigate to Insights
        binding.cardInsights.setOnClickListener {
            findNavController().navigate(R.id.navigation_insights)
        }

        // Gamification card - Navigate to Gamification
        binding.cardGamification.setOnClickListener {
            findNavController().navigate(R.id.navigation_gamification)
        }

        // Profile card - Navigate to Profile
        binding.cardProfile.setOnClickListener {
            findNavController().navigate(R.id.navigation_profile)
        }

        // Logout button
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun loadData() {
        val user = sessionManager.getUserDetails() ?: return

        lifecycleScope.launch {
            // Get recent expenses
            val expenses = database.expenseDao().getAllExpensesWithCategory(user.id).take(5)
            if (expenses.isEmpty()) {
                binding.tvNoExpenses.visibility = View.VISIBLE
                binding.rvRecentExpenses.visibility = View.GONE
            } else {
                binding.tvNoExpenses.visibility = View.GONE
                binding.rvRecentExpenses.visibility = View.VISIBLE
                expenseAdapter.updateExpenses(expenses)
            }

            // Calculate total balance
            val totalExpenses = database.expenseDao().getTotalExpenses(user.id) ?: 0.0
            val totalIncome = database.incomeDao().getTotalIncome(user.id) ?: 0.0
            val balance = totalIncome - totalExpenses

            val formatter = NumberFormat.getCurrencyInstance().apply {
                currency = Currency.getInstance("ZAR")
            }
            binding.tvTotalBalance.text = formatter.format(balance)

            // Update gamification data
            updateGamificationData()
        }
    }

    private fun updateGamificationData() {
        val totalPoints = gamificationManager.getTotalPoints()
        val currentLevel = gamificationManager.getUserLevel()
        val currentStreak = gamificationManager.getCurrentStreak()
        val levelTitle = gamificationManager.getLevelTitle()

        binding.tvUserLevel.text = "$levelTitle (Level $currentLevel)"
        binding.tvTotalPoints.text = "⭐ $totalPoints pts"
        binding.tvCurrentStreak.text = "$currentStreak days"
    }

    private fun showAddIncomeDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_income, null)
        val etAmount = dialogView.findViewById<EditText>(R.id.etIncomeAmount)
        val etSource = dialogView.findViewById<EditText>(R.id.etIncomeSource)
        val etNote = dialogView.findViewById<EditText>(R.id.etIncomeNote)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancelIncome)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSaveIncome)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnSave.setOnClickListener {
            val amountStr = etAmount.text.toString().trim()
            val source = etSource.text.toString().trim()
            val note = etNote.text.toString().trim()

            if (amountStr.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter an amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (source.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a source", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val amount = amountStr.toDouble()
                val user = sessionManager.getUserDetails()

                if (user != null) {
                    lifecycleScope.launch {
                        val income = Income(
                            amount = amount,
                            source = source,
                            note = note,
                            date = Date(),
                            userId = user.id
                        )

                        val id = database.incomeDao().insertIncome(income)

                        if (id > 0) {
                            Toast.makeText(requireContext(), "Income added successfully", Toast.LENGTH_SHORT).show()
                            loadData() // Refresh data
                            dialog.dismiss()

                            // Check for achievements
                            gamificationManager.logExpenseActivity()
                        } else {
                            Toast.makeText(requireContext(), "Failed to add income. Please try again.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "User session not found. Please log in again.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("IncomeDialog", "Error adding income", e)
            }
        }

        dialog.show()
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

    override fun onResume() {
        super.onResume()
        loadData() // Refresh data when returning to fragment
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}