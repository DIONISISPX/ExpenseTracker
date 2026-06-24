package com.dionisispx.expensetracker.domain.model

// Data class representing receipt information
data class ReceiptData(
    val storeName: String,
    val amount: String,
    val category: ExpenseCategory
)
