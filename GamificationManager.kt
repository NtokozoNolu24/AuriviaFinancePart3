package com.example.open_sourcepart2

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*


class GamificationManager(private val context: Context) {


    private val prefs: SharedPreferences =
        context.getSharedPreferences(
            "gamification_prefs",
            Context.MODE_PRIVATE
        )


    private val dateFormat =
        SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.getDefault()
        )



    companion object {


        const val ACHIEVEMENT_FIRST_EXPENSE =
            "first_expense"


        const val ACHIEVEMENT_BUDGET_KEEPER =
            "budget_keeper"


        const val ACHIEVEMENT_STREAK_7 =
            "streak_7"


        const val ACHIEVEMENT_STREAK_30 =
            "streak_30"


        const val ACHIEVEMENT_SAVER =
            "saver"


        const val ACHIEVEMENT_CATEGORY_MASTER =
            "category_master"



        const val POINTS_EXPENSE_LOGGED = 10

        const val POINTS_BUDGET_MET = 50

        const val POINTS_SAVER = 100

        const val POINTS_STREAK = 50

    }






    // =========================
    // ACHIEVEMENTS
    // =========================


    fun checkFirstExpenseAchievement(){


        if(!hasAchievement(ACHIEVEMENT_FIRST_EXPENSE)){


            unlockAchievement(

                ACHIEVEMENT_FIRST_EXPENSE,

                "First Steps 🎯",

                "You logged your first expense"

            )


            addPoints(POINTS_EXPENSE_LOGGED)

        }

    }







    fun checkBudgetAchievements(

        spent:Double,

        budget:Double

    ){


        if(spent <= budget && budget > 0){


            if(!hasAchievement(ACHIEVEMENT_BUDGET_KEEPER)){


                unlockAchievement(

                    ACHIEVEMENT_BUDGET_KEEPER,

                    "Budget Boss 💰",

                    "You stayed within budget"

                )


                addPoints(
                    POINTS_BUDGET_MET
                )

            }


        }

    }









    fun checkSavingsAchievement(saved:Double){


        if(saved >= 500){


            if(!hasAchievement(ACHIEVEMENT_SAVER)){


                unlockAchievement(

                    ACHIEVEMENT_SAVER,

                    "Saving Champion ⭐",

                    "You saved R500"

                )


                addPoints(
                    POINTS_SAVER
                )


            }


        }


    }









    fun updateStreak(){


        val today =
            dateFormat.format(Date())


        val last =
            prefs.getString(
                "last_date",
                ""
            )



        if(today != last){


            val yesterday =
                Calendar.getInstance()
                    .apply {

                        add(
                            Calendar.DAY_OF_YEAR,
                            -1
                        )

                    }



            val yesterdayDate =
                dateFormat.format(
                    yesterday.time
                )




            if(last == yesterdayDate){


                increaseStreak()


            }
            else{


                resetStreak()


            }



            prefs.edit()
                .putString(
                    "last_date",
                    today
                )
                .apply()



            checkStreak()

        }


    }







    private fun checkStreak(){


        val streak =
            getCurrentStreak()



        if(streak >= 7){


            unlockAchievement(

                ACHIEVEMENT_STREAK_7,

                "Week Warrior 🔥",

                "7 day streak"

            )


        }




        if(streak >= 30){


            unlockAchievement(

                ACHIEVEMENT_STREAK_30,

                "Month Master 🏆",

                "30 day streak"

            )


        }


    }








    private fun unlockAchievement(

        key:String,

        title:String,

        message:String

    ){


        if(!hasAchievement(key)){


            prefs.edit()
                .putBoolean(
                    key,
                    true
                )
                .apply()



            Toast.makeText(

                context,

                "🏆 $title unlocked!",

                Toast.LENGTH_LONG

            ).show()


        }


    }








    private fun hasAchievement(
        key:String
    ):Boolean{


        return prefs.getBoolean(
            key,
            false
        )

    }








    // =========================
    // POINT SYSTEM
    // =========================


    private fun addPoints(points:Int){


        val total =
            getTotalPoints()



        prefs.edit()
            .putInt(
                "points",
                total + points
            )
            .apply()

    }





    fun getTotalPoints():Int{


        return prefs.getInt(
            "points",
            0
        )


    }








    // =========================
    // LEVEL SYSTEM
    // =========================



    fun getUserLevel():Int{


        return when(getTotalPoints()){


            in 0..99 -> 1

            in 100..299 -> 2

            in 300..599 -> 3

            in 600..999 -> 4


            else -> 5


        }


    }






    fun getLevelTitle():String{


        return when(getUserLevel()){


            1 ->
                "🌱 Beginner"


            2 ->
                "📊 Tracker"


            3 ->
                "💰 Saver"


            4 ->
                "🎯 Expert"


            else ->
                "👑 Finance Master"


        }


    }







    fun getPointsToNextLevel():Int{


        val points =
            getTotalPoints()



        return when(getUserLevel()){


            1 -> 100 - points

            2 -> 300 - points

            3 -> 600 - points

            4 -> 1000 - points


            else -> 0


        }


    }








    // =========================
    // STREAK
    // =========================



    private fun increaseStreak(){


        prefs.edit()
            .putInt(

                "streak",

                getCurrentStreak()+1

            )
            .apply()


    }



    private fun resetStreak(){


        prefs.edit()
            .putInt(
                "streak",
                1
            )
            .apply()


    }



    fun getCurrentStreak():Int{


        return prefs.getInt(
            "streak",
            0
        )

    }








    fun getAllAchievements():List<Achievement>{


        return listOf(


            Achievement(

                ACHIEVEMENT_FIRST_EXPENSE,

                "First Steps 🎯",

                "Log your first expense",

                hasAchievement(ACHIEVEMENT_FIRST_EXPENSE)

            ),



            Achievement(

                ACHIEVEMENT_BUDGET_KEEPER,

                "Budget Boss 💰",

                "Stay within your budget",

                hasAchievement(ACHIEVEMENT_BUDGET_KEEPER)

            ),



            Achievement(

                ACHIEVEMENT_STREAK_7,

                "Week Warrior 🔥",

                "7 day streak",

                hasAchievement(ACHIEVEMENT_STREAK_7)

            ),



            Achievement(

                ACHIEVEMENT_STREAK_30,

                "Month Master 🏆",

                "30 day streak",

                hasAchievement(ACHIEVEMENT_STREAK_30)

            ),



            Achievement(

                ACHIEVEMENT_SAVER,

                "Saving Champion ⭐",

                "Save R500",

                hasAchievement(ACHIEVEMENT_SAVER)

            )

        )


    }


}




data class Achievement(

    val key:String,

    val title:String,

    val description:String,

    val isUnlocked:Boolean

)