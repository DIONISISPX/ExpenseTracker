package com.dionisispx.expensetracker.data.mapper

import com.dionisispx.expensetracker.data.local.entity.ExpenseEntity
import com.dionisispx.expensetracker.domain.model.Expense

fun ExpenseEntity.toDomain(): Expense {
    return Expense(
        id = id,
        storeName = storeName,
        amount = amount,
        date = date,
        category = category
    )
}

fun Expense.toEntity(): ExpenseEntity {
    return ExpenseEntity(
        id = id,
        storeName = storeName,
        amount = amount,
        date = date,
        category = category
    )
}
