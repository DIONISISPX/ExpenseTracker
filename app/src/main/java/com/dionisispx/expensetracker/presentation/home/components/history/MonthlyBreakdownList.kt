package com.dionisispx.expensetracker.presentation.home.components.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dionisispx.expensetracker.R
import java.util.Locale

@Composable
fun MonthlyBreakdownList(monthlyTotals: FloatArray, currencyPreference: String) {
    Text(
        text = stringResource(R.string.monthly_breakdown),
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)
    )

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).padding(bottom = 32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val fullMonths = stringArrayResource(R.array.months_full)

            monthlyTotals.forEachIndexed { index, total ->
                val formattedTotal = String.format(Locale.US, "%.2f", total)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(fullMonths[index])
                    Text(
                        text = if (currencyPreference == "$") "$$formattedTotal" else "$formattedTotal $currencyPreference",
                        fontWeight = if (total > 0f) FontWeight.Bold else FontWeight.Normal,
                        color = if (total > 0f) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }
            }
        }
    }
}
