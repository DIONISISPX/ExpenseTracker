package com.dionisispx.expensetracker.domain.repository

import com.dionisispx.expensetracker.domain.model.Expense
import kotlinx.coroutines.flow.Flow

// Contract for expense data operations
interface ExpenseRepository {

    // Get a continuous stream of expenses
    fun getAllExpenses(): Flow<List<Expense>>

    // Fetch expenses by date range
    fun getExpensesByDateRange(startDate: Long, endDate: Long): Flow<List<Expense>>

    // Save a new expense
    suspend fun insertExpense(expense: Expense)

    // Delete an existing expense
    suspend fun deleteExpense(expense: Expense)
}