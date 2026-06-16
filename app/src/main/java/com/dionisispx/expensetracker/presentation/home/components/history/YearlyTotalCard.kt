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
import java.util.Locale

@Composable
fun YearlyTotalCard(yearlyExpenses: List<Expense>, currencyPreference: String) {
    val yearlyTotal = yearlyExpenses.sumOf { it.amount }
    val formattedYearlyTotal = String.format(Locale.US, "%.2f", yearlyTotal)

    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(stringResource(R.string.yearly_total), fontWeight = FontWeight.Bold)
            Text(
                text = if (currencyPreference == "$") "$$formattedYearlyTotal" else "$formattedYearlyTotal $currencyPreference",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
