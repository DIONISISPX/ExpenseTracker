package com.dionisispx.expensetracker.presentation.home.components.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dionisispx.expensetracker.R
import com.dionisispx.expensetracker.domain.model.Expense
import com.dionisispx.expensetracker.presentation.util.CurrencyUtils
import java.util.Locale

@Composable
fun YearlyTotalCard(
    yearlyExpenses: List<Expense>,
    showRemaining: Boolean = false,
    totalBudget: Float = 0f,
    currencyPreference: String
) {
    val yearlySpent = yearlyExpenses.sumOf { it.amount }.toFloat()
    val yearlyBudget = totalBudget * 12
    val isOverBudget = showRemaining && yearlySpent > yearlyBudget

    val displayAmount = if (showRemaining) {
        if (isOverBudget) yearlySpent - yearlyBudget else yearlyBudget - yearlySpent
    } else {
        yearlySpent
    }



    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOverBudget) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (showRemaining) {
                    if (isOverBudget) stringResource(R.string.over_budget) else stringResource(R.string.remaining)
                } else {
                    stringResource(R.string.yearly_total)
                },
                fontWeight = FontWeight.Bold,
                color = if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = CurrencyUtils.formatCurrency(displayAmount, currencyPreference),
                fontWeight = FontWeight.Bold,
                color = if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }
    }
}
