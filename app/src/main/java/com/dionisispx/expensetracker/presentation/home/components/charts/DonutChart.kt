package com.dionisispx.expensetracker.presentation.home.components.charts

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dionisispx.expensetracker.R
import com.dionisispx.expensetracker.presentation.util.CurrencyUtils
import com.dionisispx.expensetracker.domain.model.Expense
import com.dionisispx.expensetracker.presentation.util.getCategoryDetails

// Displays a donut chart of expenses
@Composable
fun DonutChart(expenses: List<Expense>, showRemaining: Boolean, totalBudget: Float, currencySymbol: String) {
    // Calculate total spent amount
    val totalSpent = expenses.sumOf { it.amount }.toFloat()

    val errorColor = MaterialTheme.colorScheme.error

    Box(
        modifier = Modifier
            .size(220.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Enforce explicit canvas size and aspect ratio
        Canvas(modifier = Modifier.size(180.dp).aspectRatio(1f)) {
            // Draw background circle
            drawArc(
                color = Color.LightGray.copy(alpha = 0.3f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 50f, cap = StrokeCap.Butt)
            )

            if (expenses.isNotEmpty() && totalSpent > 0f) {
                var currentStartAngle = -90f
                // Group expenses by category
                val groupedExpenses = expenses.groupBy { it.category }
                val scaleBase = if (showRemaining && totalBudget > 0f) totalBudget else totalSpent

                if (showRemaining && totalSpent > totalBudget) {
                    // Draw red arc if over budget
                    drawArc(
                        color = errorColor,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 50f, cap = StrokeCap.Butt)
                    )
                } else {
                    // Draw colored arcs for each category
                    groupedExpenses.entries.forEach { entry ->
                        val categoryTotal = entry.value.sumOf { it.amount }.toFloat()
                        val sweepAngle = (categoryTotal / scaleBase) * 360f
                        val color = getCategoryDetails(entry.key).second

                        drawArc(
                            color = color,
                            startAngle = currentStartAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = 50f, cap = StrokeCap.Butt)
                        )
                        currentStartAngle += sweepAngle
                    }
                }
            }
        }

        // Display center text with amount
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.animateContentSize()) {
            if (showRemaining) {
                val remaining = (totalBudget - totalSpent).coerceAtLeast(0f)
                val isOver = totalSpent > totalBudget
                val centerValue = if (isOver) totalSpent - totalBudget else remaining


                Text(
                    text = if (isOver) stringResource(R.string.over_budget) else stringResource(R.string.remaining),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isOver) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = CurrencyUtils.formatCurrency(centerValue, currencySymbol),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isOver) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
            } else {
                Text(
                    text = stringResource(R.string.spent),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = CurrencyUtils.formatCurrency(totalSpent, currencySymbol),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
