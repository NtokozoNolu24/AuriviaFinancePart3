package com.example.open_sourcepart2

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.*

class CategoryAnalyticsAdapter(
    private var categories: List<CategoryExpenseAnalytics>
) : RecyclerView.Adapter<CategoryAnalyticsAdapter.ViewHolder>() {

    private val currencyFormat = NumberFormat.getCurrencyInstance().apply {
        currency = Currency.getInstance("ZAR")
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val viewColorIndicator: View = itemView.findViewById(R.id.view_color_indicator)
        val tvCategoryName: TextView = itemView.findViewById(R.id.tv_category_name)
        val tvCategorySpent: TextView = itemView.findViewById(R.id.tv_category_spent)
        val progressCategory: ProgressBar = itemView.findViewById(R.id.progress_category)
        val tvCategoryBudget: TextView = itemView.findViewById(R.id.tv_category_budget)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_analytics, parent, false)  // UPDATED!
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]

        holder.tvCategoryName.text = category.categoryName
        holder.tvCategorySpent.text = currencyFormat.format(category.totalSpent)

        // Calculate percentage if budget exists
        if (category.budget > 0) {
            val percentage = ((category.totalSpent / category.budget) * 100).toInt().coerceAtMost(100)
            holder.progressCategory.progress = percentage
            holder.tvCategoryBudget.text = "of ${currencyFormat.format(category.budget)}"

            // Set progress bar color based on performance
            val color = when {
                percentage <= 70 -> Color.parseColor("#4CAF50") // Green
                percentage <= 100 -> Color.parseColor("#FF9800") // Orange
                else -> Color.parseColor("#F44336") // Red
            }
            holder.progressCategory.progressTintList =
                android.content.res.ColorStateList.valueOf(color)
        } else {
            holder.progressCategory.progress = 0
            holder.tvCategoryBudget.text = "No budget set"
        }

        // Set category color indicator if it exists
        try {
            holder.viewColorIndicator.setBackgroundColor(Color.parseColor(category.categoryColor))
        } catch (e: Exception) {
            holder.viewColorIndicator.setBackgroundColor(Color.parseColor("#6C63FF"))
        }
    }

    override fun getItemCount(): Int = categories.size

    fun updateCategories(newCategories: List<CategoryExpenseAnalytics>) {
        categories = newCategories
        notifyDataSetChanged()
    }
}