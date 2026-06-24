package com.dionisispx.expensetracker.data.repository

import com.dionisispx.expensetracker.data.local.ExpenseDao
import com.dionisispx.expensetracker.data.mapper.toDomain
import com.dionisispx.expensetracker.data.mapper.toEntity
import com.dionisispx.expensetracker.domain.model.Expense
import com.dionisispx.expensetracker.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

// Implements data operations using the local database
class ExpenseRepositoryImpl @Inject constructor(
    private val dao: ExpenseDao
) : ExpenseRepository {

    // Retrieves all expenses mapped to domain models
    override fun getAllExpenses(): Flow<List<Expense>> {
        return dao.getAllExpenses().map { entities -> entities.map { it.toDomain() } }
    }

    // Retrieves expenses within a given date range
    override fun getExpensesByDateRange(startDate: Instant, endDate: Instant): Flow<List<Expense>> {
        return dao.getExpensesByDateRange(startDate, endDate).map { entities -> entities.map { it.toDomain() } }
    }

    // Inserts an expense and returns success or failure
    override suspend fun insertExpense(expense: Expense): Result<Unit> {
        return try {
            dao.insertExpense(expense.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Deletes a specific expense and returns the result
    override suspend fun deleteExpense(expense: Expense): Result<Unit> {
        return try {
            dao.deleteExpense(expense.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Clears all expenses from the database
    override suspend fun deleteAllExpenses(): Result<Unit> {
        return try {
            dao.deleteAllExpenses()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}