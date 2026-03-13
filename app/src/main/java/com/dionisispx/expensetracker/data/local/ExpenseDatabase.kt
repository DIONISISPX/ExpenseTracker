package com.dionisispx.expensetracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dionisispx.expensetracker.domain.model.Expense

// Main database class
@Database(
    entities = [Expense::class],
    version = 1,
    exportSchema = false
)
abstract class ExpenseDatabase : RoomDatabase() {

    // DAO access
    abstract val dao: ExpenseDao
}