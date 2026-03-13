package com.dionisispx.expensetracker.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Room database table for expenses
@Entity(tableName = "expenses_table")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val storeName: String,
    val totalAmount: Double,
    val date: Long,
    val category: String
)