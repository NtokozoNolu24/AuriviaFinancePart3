package com.example.open_sourcepart2

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.open_sourcepart2.database.AppDatabase
import com.example.open_sourcepart2.database.entities.Category
import com.example.open_sourcepart2.database.entities.Expense
import com.example.open_sourcepart2.databinding.DialogAddExpenseBinding
import com.example.open_sourcepart2.databinding.FragmentExpensesBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class ExpensesFragment : Fragment() {


    private var _binding: FragmentExpensesBinding? = null
    private val binding get() = _binding!!


    private lateinit var database: AppDatabase
    private lateinit var sessionManager: SessionManager
    private lateinit var gamificationManager: GamificationManager
    private lateinit var expenseAdapter: ExpenseAdapter


    private val displayDateFormat =
        SimpleDateFormat(
            "MMM d, yyyy",
            Locale.getDefault()
        )


    private var categoriesList =
        listOf<Category>()


    private var selectedDate =
        Calendar.getInstance().time





    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {


        _binding =
            FragmentExpensesBinding.inflate(
                inflater,
                container,
                false
            )


        return binding.root

    }





    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?

    ){

        super.onViewCreated(
            view,
            savedInstanceState
        )


        database =
            AppDatabase.getDatabase(
                requireContext()
            )


        sessionManager =
            SessionManager(
                requireContext()
            )


        gamificationManager =
            GamificationManager(
                requireContext()
            )


        setupUI()

        loadData()

    }









    private fun setupUI(){


        binding.rvExpenses.layoutManager =
            LinearLayoutManager(
                requireContext()
            )


        expenseAdapter =
            ExpenseAdapter(
                emptyList()
            )


        binding.rvExpenses.adapter =
            expenseAdapter





        binding.fabAddExpense.setOnClickListener {


            showAddExpenseDialog()


        }



        binding.btnApplyFilters.setOnClickListener {


            applyFilters()


        }


    }








    private fun loadData(){


        val user =
            sessionManager.getUserDetails()
                ?: return




        lifecycleScope.launch {



            categoriesList =
                database.categoryDao()
                    .getAllCategories(
                        user.id
                    )



            val categoryNames =
                mutableListOf(
                    "All Categories"
                )



            categoryNames.addAll(
                categoriesList.map {
                    it.name
                }
            )



            val adapter =
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    categoryNames
                )


            adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
            )


            binding.spinnerCategory.adapter =
                adapter






            val expenses =
                database.expenseDao()
                    .getAllExpensesWithCategory(
                        user.id
                    )



            if(expenses.isEmpty()){


                binding.tvNoExpenses.visibility =
                    View.VISIBLE


                binding.rvExpenses.visibility =
                    View.GONE


            }

            else{


                binding.tvNoExpenses.visibility =
                    View.GONE


                binding.rvExpenses.visibility =
                    View.VISIBLE



                expenseAdapter.updateExpenses(
                    expenses
                )


            }



        }


    }









    private fun showAddExpenseDialog(){



        val dialogBinding =
            DialogAddExpenseBinding.inflate(
                layoutInflater
            )



        val dialog =
            AlertDialog.Builder(
                requireContext()
            )
                .setView(
                    dialogBinding.root
                )
                .create()






        val categoryNames =
            categoriesList.map {
                it.name
            }



        if(categoryNames.isEmpty()){


            Toast.makeText(
                requireContext(),
                "Please add categories first",
                Toast.LENGTH_SHORT
            ).show()


            return

        }





        val adapter =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                categoryNames
            )


        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )


        dialogBinding.spinnerCategory.adapter =
            adapter






        dialogBinding.btnSelectDate.text =
            "Date: ${displayDateFormat.format(selectedDate)}"





        dialogBinding.btnSelectDate.setOnClickListener {


            val calendar =
                Calendar.getInstance()


            calendar.time =
                selectedDate



            DatePickerDialog(

                requireContext(),

                {_,year,month,day ->


                    calendar.set(
                        year,
                        month,
                        day
                    )


                    selectedDate =
                        calendar.time



                    dialogBinding.btnSelectDate.text =
                        "Date: ${displayDateFormat.format(selectedDate)}"


                },


                calendar.get(Calendar.YEAR),

                calendar.get(Calendar.MONTH),

                calendar.get(Calendar.DAY_OF_MONTH)

            ).show()


        }







        dialogBinding.btnCancel.setOnClickListener {


            dialog.dismiss()


        }







        dialogBinding.btnSave.setOnClickListener {



            val amountText =
                dialogBinding.etAmount.text.toString()



            val description =
                dialogBinding.etDescription.text.toString()





            if(amountText.isEmpty()
                ||
                description.isEmpty()){


                Toast.makeText(
                    requireContext(),
                    "Fill all fields",
                    Toast.LENGTH_SHORT
                ).show()



                return@setOnClickListener


            }





            lifecycleScope.launch {



                try {



                    val user =
                        sessionManager.getUserDetails()
                            ?: return@launch




                    val amount =
                        amountText.toDouble()



                    val category =
                        categoriesList[
                            dialogBinding.spinnerCategory.selectedItemPosition
                        ]






                    val expense =
                        Expense(

                            amount = amount,

                            description = description,

                            date = selectedDate,

                            categoryId = category.id,

                            userId = user.id

                        )





                    val id =
                        database.expenseDao()
                            .insertExpense(
                                expense
                            )





                    if(id > 0){



                        // 🎮 GAMIFICATION

                        gamificationManager.updateStreak()


                        gamificationManager
                            .checkFirstExpenseAchievement()






                        val totalSpent =
                            database.expenseDao()
                                .getTotalExpenses(
                                    user.id
                                ) ?: 0.0






                        val budget =
                            database.budgetDao()
                                .getBudgetByPeriod(
                                    user.id,
                                    "monthly"
                                )



                        if(budget != null){



                            gamificationManager
                                .checkBudgetAchievements(

                                    totalSpent,

                                    budget.amount

                                )


                        }






                        checkBudgetWarning(
                            user.id,
                            category
                        )







                        Toast.makeText(

                            requireContext(),

                            "💰 Expense added! Keep your streak 🔥",

                            Toast.LENGTH_SHORT

                        ).show()






                        loadData()


                        dialog.dismiss()



                    }



                }

                catch(e:Exception){


                    Toast.makeText(

                        requireContext(),

                        "Invalid amount",

                        Toast.LENGTH_SHORT

                    ).show()


                }



            }




        }





        dialog.show()



    }









    private fun checkBudgetWarning(
        userId:Int,
        category:Category
    ){


        lifecycleScope.launch {


            val total =
                database.expenseDao()
                    .getTotalByCategory(
                        userId,
                        category.id
                    ) ?: 0.0





            if(total > category.budget){


                Toast.makeText(

                    requireContext(),

                    "⚠️ ${category.name} budget exceeded",

                    Toast.LENGTH_LONG

                ).show()


            }



        }


    }









    private fun applyFilters(){

        // keep your existing filter logic here

    }







    override fun onDestroyView(){

        super.onDestroyView()

        _binding = null

    }


}