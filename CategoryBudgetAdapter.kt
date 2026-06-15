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

class CategoryBudgetAdapter(
    private var categories: List<CategorySummary>
) : RecyclerView.Adapter<CategoryBudgetAdapter.ViewHolder>() {

    private val currencyFormat = NumberFormat.getCurrencyInstance().apply {
        currency = Currency.getInstance("ZAR")
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val viewColor: View = itemView.findViewById(R.id.viewColor)
        val tvCategoryName: TextView = itemView.findViewById(R.id.tvCategoryName)
        val tvBudgetAmount: TextView = itemView.findViewById(R.id.tvBudgetAmount)
        val progressCategory: ProgressBar = itemView.findViewById(R.id.progressCategory)
        val tvSpent: TextView = itemView.findViewById(R.id.tvSpent)
        val tvRemaining: TextView = itemView.findViewById(R.id.tvRemaining)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_budget, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]

        holder.tvCategoryName.text = category.name
        holder.tvBudgetAmount.text = currencyFormat.format(category.budget)
        holder.tvSpent.text = "Spent: ${currencyFormat.format(category.totalSpent)}"

        val remaining = (category.budget - category.totalSpent).coerceAtLeast(0.0)
        holder.tvRemaining.text = "Remaining: ${currencyFormat.format(remaining)}"

        val percentage = if (category.budget > 0) {
            ((category.totalSpent / category.budget) * 100).toInt().coerceAtMost(100)
        } else 0

        holder.progressCategory.progress = percentage

        val (progressColor, textColor) = when {
            percentage >= 100 -> Pair("#F44336", "#F44336")
            percentage >= 80 -> Pair("#FF9800", "#FF9800")
            else -> Pair("#E040FB", "#CE93D8")
        }

        holder.progressCategory.progressTintList =
            android.content.res.ColorStateList.valueOf(Color.parseColor(progressColor))

        if (percentage >= 80) {
            holder.tvRemaining.setTextColor(Color.parseColor(textColor))
        } else {
            holder.tvRemaining.setTextColor(Color.parseColor("#CE93D8"))
        }

        holder.viewColor.setBackgroundColor(Color.parseColor("#E040FB"))
    }

    override fun getItemCount(): Int = categories.size

    fun updateCategories(newCategories: List<CategorySummary>) {
        categories = newCategories
        notifyDataSetChanged()
    }
}