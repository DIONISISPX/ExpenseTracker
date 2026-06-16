package com.dionisispx.expensetracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dionisispx.expensetracker.data.local.entity.ExpenseEntity

// Holds the entire database
@Database(
    entities = [ExpenseEntity::class],
    version = 2,
    exportSchema = false
)
abstract class ExpenseDatabase: RoomDatabase() {

    abstract val dao: ExpenseDao
}