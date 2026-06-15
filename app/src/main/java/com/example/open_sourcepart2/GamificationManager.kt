package com.example.open_sourcepart2

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*

class GamificationManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("gamification_prefs", Context.MODE_PRIVATE)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    companion object {
        // Achievement Keys
        const val ACHIEVEMENT_FIRST_EXPENSE = "first_expense"
        const val ACHIEVEMENT_BUDGET_KEEPER = "budget_keeper"
        const val ACHIEVEMENT_STREAK_7 = "streak_7"
        const val ACHIEVEMENT_STREAK_30 = "streak_30"
        const val ACHIEVEMENT_SAVER = "saver"
        const val ACHIEVEMENT_CATEGORY_MASTER = "category_master"
        const val ACHIEVEMENT_MONTH_COMPLETE = "month_complete"

        // Points
        const val POINTS_EXPENSE_LOGGED = 10
        const val POINTS_BUDGET_MET = 50
        const val POINTS_STREAK_BONUS = 25
        const val POINTS_UNDER_BUDGET = 100
    }

    fun checkFirstExpenseAchievement() {
        if (!hasAchievement(ACHIEVEMENT_FIRST_EXPENSE)) {
            unlockAchievement(ACHIEVEMENT_FIRST_EXPENSE, "First Steps! 🎯", "You've logged your first expense!")
            addPoints(POINTS_EXPENSE_LOGGED)
        }
    }

    fun checkBudgetAchievements(totalSpent: Double, totalBudget: Double, minGoal: Double) {
        // Budget Keeper - stayed within budget
        if (totalSpent <= totalBudget && totalBudget > 0) {
            if (!hasAchievement(ACHIEVEMENT_BUDGET_KEEPER)) {
                unlockAchievement(ACHIEVEMENT_BUDGET_KEEPER, "Budget Keeper! 💰", "You stayed within your budget!")
                addPoints(POINTS_BUDGET_MET)
            }
        }

        // Super Saver - under minimum goal
        if (totalSpent <= minGoal && minGoal > 0) {
            if (!hasAchievement(ACHIEVEMENT_SAVER)) {
                unlockAchievement(ACHIEVEMENT_SAVER, "Super Saver! ⭐", "You spent less than your minimum goal!")
                addPoints(POINTS_UNDER_BUDGET)
            }
        }
    }

    fun checkStreakAchievements() {
        val currentStreak = getCurrentStreak()

        when (currentStreak) {
            7 -> {
                if (!hasAchievement(ACHIEVEMENT_STREAK_7)) {
                    unlockAchievement(ACHIEVEMENT_STREAK_7, "Week Warrior! 🔥", "7 days of consistent logging!")
                    addPoints(POINTS_STREAK_BONUS * 2)
                }
            }
            30 -> {
                if (!hasAchievement(ACHIEVEMENT_STREAK_30)) {
                    unlockAchievement(ACHIEVEMENT_STREAK_30, "Month Master! 🏆", "30 days of consistent logging!")
                    addPoints(POINTS_STREAK_BONUS * 4)
                }
            }
        }
    }

    fun checkCategoryMasterAchievement(categoriesUsed: Int) {
        if (categoriesUsed >= 5 && !hasAchievement(ACHIEVEMENT_CATEGORY_MASTER)) {
            unlockAchievement(ACHIEVEMENT_CATEGORY_MASTER, "Category Master! 📊", "You've used 5+ categories!")
            addPoints(POINTS_STREAK_BONUS)
        }
    }

    fun logExpenseActivity() {
        // Update streak
        val today = dateFormat.format(Date())
        val lastActivity = prefs.getString("last_activity_date", "") ?: ""

        if (lastActivity != today) {
            prefs.edit().putString("last_activity_date", today).apply()

            // Check if streak continues
            val yesterday = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -1)
            }
            val yesterdayStr = dateFormat.format(yesterday.time)

            if (lastActivity == yesterdayStr || lastActivity.isEmpty()) {
                incrementStreak()
            } else {
                resetStreak()
            }

            addPoints(POINTS_EXPENSE_LOGGED)
            checkStreakAchievements()
        }
    }

    private fun unlockAchievement(key: String, title: String, description: String) {
        prefs.edit().putBoolean(key, true).apply()

        // Show achievement notification
        Toast.makeText(context, "🏆 Achievement Unlocked!\n$title\n$description", Toast.LENGTH_LONG).show()

        // You could also show a custom dialog or notification here
    }

    private fun hasAchievement(key: String): Boolean {
        return prefs.getBoolean(key, false)
    }

    private fun addPoints(points: Int) {
        val currentPoints = getTotalPoints()
        prefs.edit().putInt("total_points", currentPoints + points).apply()
    }

    fun getTotalPoints(): Int {
        return prefs.getInt("total_points", 0)
    }

    private fun incrementStreak() {
        val currentStreak = getCurrentStreak()
        prefs.edit().putInt("current_streak", currentStreak + 1).apply()
    }

    private fun resetStreak() {
        prefs.edit().putInt("current_streak", 1).apply()
    }

    fun getCurrentStreak(): Int {
        return prefs.getInt("current_streak", 0)
    }

    fun getAllAchievements(): List<Achievement> {
        return listOf(
            Achievement(
                ACHIEVEMENT_FIRST_EXPENSE,
                "First Steps! 🎯",
                "Log your first expense",
                hasAchievement(ACHIEVEMENT_FIRST_EXPENSE)
            ),
            Achievement(
                ACHIEVEMENT_BUDGET_KEEPER,
                "Budget Keeper! 💰",
                "Stay within your budget",
                hasAchievement(ACHIEVEMENT_BUDGET_KEEPER)
            ),
            Achievement(
                ACHIEVEMENT_STREAK_7,
                "Week Warrior! 🔥",
                "Log expenses for 7 consecutive days",
                hasAchievement(ACHIEVEMENT_STREAK_7)
            ),
            Achievement(
                ACHIEVEMENT_STREAK_30,
                "Month Master! 🏆",
                "Log expenses for 30 consecutive days",
                hasAchievement(ACHIEVEMENT_STREAK_30)
            ),
            Achievement(
                ACHIEVEMENT_SAVER,
                "Super Saver! ⭐",
                "Spend less than your minimum goal",
                hasAchievement(ACHIEVEMENT_SAVER)
            ),
            Achievement(
                ACHIEVEMENT_CATEGORY_MASTER,
                "Category Master! 📊",
                "Use 5 or more categories",
                hasAchievement(ACHIEVEMENT_CATEGORY_MASTER)
            )
        )
    }

    fun getUserLevel(): Int {
        val points = getTotalPoints()
        return when {
            points < 100 -> 1
            points < 300 -> 2
            points < 600 -> 3
            points < 1000 -> 4
            else -> 5
        }
    }

    fun getPointsToNextLevel(): Int {
        val currentPoints = getTotalPoints()
        val currentLevel = getUserLevel()

        return when (currentLevel) {
            1 -> 100 - currentPoints
            2 -> 300 - currentPoints
            3 -> 600 - currentPoints
            4 -> 1000 - currentPoints
            else -> 0
        }
    }

    fun getLevelTitle(): String {
        return when (getUserLevel()) {
            1 -> "Beginner 🌱"
            2 -> "Tracker 📊"
            3 -> "Saver 💰"
            4 -> "Expert 🎯"
            5 -> "Master 👑"
            else -> "Unknown"
        }
    }

    fun getProgressToNextLevel(): Int {
        val currentPoints = getTotalPoints()
        val pointsToNext = getPointsToNextLevel()

        if (pointsToNext == 0) return 100 // Max level reached

        val totalPointsForCurrentLevel = when (getUserLevel()) {
            1 -> 100
            2 -> 200 // 300 - 100
            3 -> 300 // 600 - 300
            4 -> 400 // 1000 - 600
            else -> 100
        }

        val pointsInCurrentLevel = totalPointsForCurrentLevel - pointsToNext
        return ((pointsInCurrentLevel.toFloat() / totalPointsForCurrentLevel) * 100).toInt()
    }
}

data class Achievement(
    val key: String,
    val title: String,
    val description: String,
    val isUnlocked: Boolean
)
