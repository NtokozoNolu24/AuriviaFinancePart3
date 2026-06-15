package com.example.open_sourcepart2

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class ExpenseAdapter(
    private var expenses: List<ExpenseWithCategory>
) : RecyclerView.Adapter<ExpenseAdapter.ViewHolder>() {

    private val currencyFormat = NumberFormat.getCurrencyInstance().apply {
        currency = Currency.getInstance("ZAR")
    }

    private val displayDateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    private val dbDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvExpenseAmount: TextView = itemView.findViewById(R.id.tv_expense_amount)
        val tvExpenseDescription: TextView = itemView.findViewById(R.id.tv_expense_description)
        val tvExpenseDate: TextView = itemView.findViewById(R.id.tv_expense_date)
        val tvExpenseCategory: TextView = itemView.findViewById(R.id.tv_expense_category)
        val viewCategoryColor: View = itemView.findViewById(R.id.view_category_color)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val expense = expenses[position]

        holder.tvExpenseAmount.text = currencyFormat.format(expense.amount)
        holder.tvExpenseDescription.text = expense.description

        try {
            val date = dbDateFormat.parse(expense.date)
            holder.tvExpenseDate.text = displayDateFormat.format(date!!)
        } catch (e: Exception) {
            holder.tvExpenseDate.text = expense.date
        }

        holder.tvExpenseCategory.text = expense.categoryName

        try {
            holder.viewCategoryColor.setBackgroundColor(Color.parseColor(expense.categoryColor))
        } catch (e: Exception) {
            holder.viewCategoryColor.setBackgroundColor(Color.GRAY)
        }
    }

    override fun getItemCount(): Int = expenses.size

    fun updateExpenses(newExpenses: List<ExpenseWithCategory>) {
        this.expenses = newExpenses
        notifyDataSetChanged()
    }
}