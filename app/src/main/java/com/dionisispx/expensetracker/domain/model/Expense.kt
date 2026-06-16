package com.dionisispx.expensetracker.domain.model

data class Expense(
    val id: Int = 0,
    val storeName: String,
    val amount: Double,
    val date: Long,
    val category: String
)