package com.dionisispx.expensetracker.presentation.util

import java.util.Locale

// Utility object for formatting currency values
object CurrencyUtils {
    // Formats string amount with currency symbol
    fun formatCurrency(amount: String, currencyPreference: String): String {
        return if (currencyPreference == "$") "$$amount" else "$amount $currencyPreference"
    }

// Formats float amount to two decimals with currency symbol
    fun formatCurrency(amount: Float, currencyPreference: String): String {
        val formattedAmount = String.format(Locale.US, "%.2f", amount)
        return formatCurrency(formattedAmount, currencyPreference)
    }
    
// Formats float amount without decimals and with currency symbol
    fun formatCurrencyNoDecimals(amount: Float, currencyPreference: String): String {
        val formattedAmount = String.format(Locale.US, "%.0f", amount)
        return formatCurrency(formattedAmount, currencyPreference)
    }
}
