package com.dionisispx.expensetracker.data.repository

import com.dionisispx.expensetracker.data.local.ExpenseDao
import com.dionisispx.expensetracker.domain.model.Expense
import com.dionisispx.expensetracker.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// Actual implementation that talks to the room database
class ExpenseRepositoryImpl @Inject constructor(
    private val dao: ExpenseDao
) : ExpenseRepository {

    override fun getAllExpenses(): Flow<List<Expense>> {
        return dao.getAllExpenses()
    }

    override fun getExpensesByDateRange(startDate: Long, endDate: Long): Flow<List<Expense>> {
        return dao.getExpensesByDateRange(startDate, endDate)
    }

    override suspend fun insertExpense(expense: Expense) {
        dao.insertExpense(expense)
    }

    override suspend fun deleteExpense(expense: Expense) {
        dao.deleteExpense(expense)
    }
}