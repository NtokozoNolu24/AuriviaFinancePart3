package com.example.open_sourcepart2.database.entities


import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "goals")
data class Goal(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,


    val userId: Int,


    val title: String,


    val targetAmount: Double,


    val savedAmount: Double = 0.0,


    val deadline: String

)