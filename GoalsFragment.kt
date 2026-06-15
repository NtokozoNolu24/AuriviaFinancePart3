package com.example.open_sourcepart2


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.*
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.open_sourcepart2.database.AppDatabase
import com.example.open_sourcepart2.database.entities.Goal
import kotlinx.coroutines.launch



class GoalsFragment: Fragment(){



    private lateinit var database: AppDatabase

    private lateinit var sessionManager: SessionManager



    override fun onCreateView(

        inflater: LayoutInflater,

        container: ViewGroup?,

        savedInstanceState: Bundle?

    ):View{


        return inflater.inflate(

            R.layout.fragment_goals,

            container,

            false

        )

    }






    override fun onViewCreated(

        view:View,

        savedInstanceState:Bundle?

    ){


        database =
            AppDatabase.getDatabase(requireContext())


        sessionManager =
            SessionManager(requireContext())



        view.findViewById<View>(

            R.id.btnCreateGoal

        ).setOnClickListener{


            createGoal()


        }



    }







    private fun createGoal(){



        val user =
            sessionManager.getUserDetails()
                ?: return




        lifecycleScope.launch{


            val goal = Goal(

                userId = user.id,

                title = "Dream Purchase",

                targetAmount = 5000.0,

                deadline = "30 Dec 2026"

            )



            database.goalDao()
                .insertGoal(goal)



            Toast.makeText(

                requireContext(),

                "🎯 Goal Created +20 XP",

                Toast.LENGTH_LONG

            ).show()



        }



    }

    private fun Unit.insertGoal(goal: Goal) {}


}