package com.example.open_sourcepart2

import java.util.Date

data class ExpenseWithCategory(
    val id: Int,
    val amount: Double,
    val description: String?,
    val date: Date,
    val categoryId: Int,
    val categoryName: String,
    val categoryColor: String
)