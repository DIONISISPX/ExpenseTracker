package com.dionisispx.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// Room database table for expenses
@Entity(tableName = "expenses_table")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val storeName: String,
    val amount: Double,
    val date: Long,
    val category: String
)
