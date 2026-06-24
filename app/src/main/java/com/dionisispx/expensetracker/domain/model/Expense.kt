package com.dionisispx.expensetracker.domain.model

import java.time.Instant

// Represents an individual expense record
data class Expense(
    val id: Int = 0,
    val storeName: String,
    val amount: Double,
    val date: Instant,
    val category: ExpenseCategory
)