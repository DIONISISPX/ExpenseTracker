package com.dionisispx.expensetracker.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dionisispx.expensetracker.domain.model.Expense
import kotlinx.coroutines.flow.Flow

// Ιnterface to interact with the database
@Dao
interface ExpenseDao {

    // Insert or update expense
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    // Delete expense from database
    @Delete
    suspend fun deleteExpense(expense: Expense): Int

    // Get all expenses ordered by newest first
    @Query("SELECT * FROM expenses_table ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<Expense>>
}