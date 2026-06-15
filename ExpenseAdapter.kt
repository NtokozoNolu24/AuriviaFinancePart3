package com.example.open_sourcepart2

import android.annotation.SuppressLint
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



    private val currencyFormat =
        NumberFormat.getCurrencyInstance(
            Locale("en", "ZA")
        ).apply {

            currency =
                Currency.getInstance("ZAR")

        }




    private val displayDateFormat =
        SimpleDateFormat(
            "MMM dd, yyyy",
            Locale.getDefault()
        )







    class ViewHolder(

        itemView: View

    ) : RecyclerView.ViewHolder(itemView){



        val amount =
            itemView.findViewById<TextView>(
                R.id.tv_expense_amount
            )



        val description =
            itemView.findViewById<TextView>(
                R.id.tv_expense_description
            )



        val date =
            itemView.findViewById<TextView>(
                R.id.tv_expense_date
            )



        val category =
            itemView.findViewById<TextView>(
                R.id.tv_expense_category
            )



        val categoryColor =
            itemView.findViewById<View>(
                R.id.view_category_color
            )



    }









    override fun onCreateViewHolder(

        parent: ViewGroup,

        viewType: Int

    ): ViewHolder {


        val view = LayoutInflater.from(
            parent.context
        )
            .inflate(

                R.layout.item_expense,

                parent,

                false

            )


        return ViewHolder(view)

    }









    override fun onBindViewHolder(

        holder: ViewHolder,

        position: Int

    ) {


        val expense =
            expenses[position]





        // Money display

        holder.amount.text =
            currencyFormat.format(
                expense.amount
            )





        holder.description.text =
            expense.description





        holder.date.text =
            "📅 ${
                displayDateFormat.format(
                    expense.date
                )
            }"





        holder.category.text =
            "🏷 ${expense.categoryName}"







        // Category colour

        try {


            holder.categoryColor
                .setBackgroundColor(

                    Color.parseColor(
                        expense.categoryColor
                    )

                )


        }

        catch(e:Exception){


            holder.categoryColor
                .setBackgroundColor(
                    Color.GRAY
                )


        }








        // Small animation

        holder.itemView.alpha = 0f

        holder.itemView.animate()

            .alpha(1f)

            .setDuration(250)

            .start()



    }







    override fun getItemCount():Int =

        expenses.size








    @SuppressLint("NotifyDataSetChanged")
    fun updateExpenses(

        newExpenses:List<ExpenseWithCategory>

    ){


        expenses =
            newExpenses


        notifyDataSetChanged()


    }


}