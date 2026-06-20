package com.dionisispx.expensetracker.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dionisispx.expensetracker.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

// Defines data access methods for expense records
@Dao
interface ExpenseDao {

    // Inserts or replaces an expense record
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    // Removes a specific expense record
    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity): Int

    // Retrieves a reactive stream of all expenses
    @Query("SELECT * FROM expenses_table")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    // Retrieves a reactive stream of expenses within a timeframe
    @Query("SELECT * FROM expenses_table WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getExpensesByDateRange(startDate: Long, endDate: Long): Flow<List<ExpenseEntity>>

    // Clears all expense records
    @Query("DELETE FROM expenses_table")
    suspend fun deleteAllExpenses()
}