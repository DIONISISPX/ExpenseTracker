package com.dionisispx.expensetracker.domain.repository

import com.dionisispx.expensetracker.domain.model.Expense
import kotlinx.coroutines.flow.Flow
import java.time.Instant

// Contract for expense data operations
interface ExpenseRepository {

    // Get a continuous stream of expenses
    fun getAllExpenses(): Flow<List<Expense>>

    // Fetch expenses by date range
    fun getExpensesByDateRange(startDate: Instant, endDate: Instant): Flow<List<Expense>>

    // Save a new expense
    suspend fun insertExpense(expense: Expense): Result<Unit>

    // Delete an existing expense
    suspend fun deleteExpense(expense: Expense): Result<Unit>

    // Delete all expenses
    suspend fun deleteAllExpenses(): Result<Unit>
}