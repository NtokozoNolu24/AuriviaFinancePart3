package com.example.open_sourcepart2

data class CategoryExpenseAnalytics(
    val categoryId: Long,
    val categoryName: String,
    val categoryColor: String,
    val budget: Double,
    val totalSpent: Double,
    val minGoal: Double,
    val maxGoal: Double
) {
    val percentage: Int
        get() = if (budget > 0) {
            ((totalSpent / budget) * 100).toInt().coerceAtMost(100)
        } else 0

    val remaining: Double
        get() = (budget - totalSpent).coerceAtLeast(0.0)

    val status: String
        get() = when {
            totalSpent <= minGoal -> "Excellent! 🎯"
            totalSpent <= budget -> "Good! ⚠️"
            else -> "Over Budget! 🚨"
        }

    val statusColor: String
        get() = when {
            totalSpent <= minGoal -> "#4CAF50"
            totalSpent <= budget -> "#FF9800"
            else -> "#F44336"
        }
}