package com.example.open_sourcepart2.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val budget: Double,
    val userId: Int,
    val icon: String? = null,
    val color: String? = null
)