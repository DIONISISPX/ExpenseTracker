package com.dionisispx.expensetracker.presentation.util

import java.util.Locale

object CurrencyUtils {
    fun formatCurrency(amount: String, currencyPreference: String): String {
        return if (currencyPreference == "$") "$$amount" else "$amount $currencyPreference"
    }

    fun formatCurrency(amount: Float, currencyPreference: String): String {
        val formattedAmount = String.format(Locale.US, "%.2f", amount)
        return formatCurrency(formattedAmount, currencyPreference)
    }
    
    fun formatCurrencyNoDecimals(amount: Float, currencyPreference: String): String {
        val formattedAmount = String.format(Locale.US, "%.0f", amount)
        return formatCurrency(formattedAmount, currencyPreference)
    }
}
