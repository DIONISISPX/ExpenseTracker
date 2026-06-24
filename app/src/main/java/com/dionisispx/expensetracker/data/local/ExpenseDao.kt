package com.dionisispx.expensetracker.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dionisispx.expensetracker.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant

// Data access methods for expenses
@Dao
interface ExpenseDao {

    // Inserts or replaces an expense
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    // Deletes an expense
    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity): Int

    // Retrieves all expenses
    @Query("SELECT * FROM expenses_table")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    // Retrieves expenses within a date range
    @Query("SELECT * FROM expenses_table WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getExpensesByDateRange(startDate: Instant, endDate: Instant): Flow<List<ExpenseEntity>>

    // Deletes all expenses
    @Query("DELETE FROM expenses_table")
    suspend fun deleteAllExpenses()
}