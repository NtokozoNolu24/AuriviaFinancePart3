package com.example.open_sourcepart2.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val amount: Double,
    val minAmount: Double = 0.0,
    val period: String,
    val startDate: String,
    val endDate: String,
    val userId: Int
)