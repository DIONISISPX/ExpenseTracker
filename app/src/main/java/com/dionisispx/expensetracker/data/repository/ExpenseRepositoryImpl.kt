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

    override fun getAllExpenses(): Flow<List<Expense>> {
        return dao.getAllExpenses().map { entities -> entities.map { it.toDomain() } }
    }

    override fun getExpensesByDateRange(startDate: Instant, endDate: Instant): Flow<List<Expense>> {
        return dao.getExpensesByDateRange(startDate, endDate).map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun insertExpense(expense: Expense): Result<Unit> {
        return try {
            dao.insertExpense(expense.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteExpense(expense: Expense): Result<Unit> {
        return try {
            dao.deleteExpense(expense.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAllExpenses(): Result<Unit> {
        return try {
            dao.deleteAllExpenses()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}