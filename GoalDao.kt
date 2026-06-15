package com.example.open_sourcepart2.database.dao


import androidx.room.*
import com.example.open_sourcepart2.database.entities.Goal


@Dao
interface GoalDao {


    @Insert
    suspend fun insertGoal(goal: Goal)


    @Query("SELECT * FROM goals WHERE userId = :userId")
    suspend fun getGoals(userId:Int): List<Goal>



    @Update
    suspend fun updateGoal(goal: Goal)



    @Delete
    suspend fun deleteGoal(goal: Goal)

}