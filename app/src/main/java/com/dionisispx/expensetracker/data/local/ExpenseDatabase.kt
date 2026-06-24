package com.dionisispx.expensetracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dionisispx.expensetracker.data.local.entity.ExpenseEntity

import androidx.room.TypeConverters

// Represents the Room database for expenses
@Database(
    entities = [ExpenseEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ExpenseDatabase: RoomDatabase() {

    // Provides database operations for expenses
    abstract val dao: ExpenseDao
}