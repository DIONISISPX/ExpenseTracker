package com.dionisispx.expensetracker.presentation.home.components.history

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.dionisispx.expensetracker.domain.model.Expense
import com.dionisispx.expensetracker.presentation.ExpenseViewModel
import com.dionisispx.expensetracker.presentation.home.components.charts.YearlyBarChart

@Composable
fun HistoryBreakdown(
    yearlyExpenses: List<Expense>,
    monthlyTotals: FloatArray,
    currentYear: Int,
    currencyPreference: String,
    isLandscape: Boolean,
    showRemaining: Boolean,
    totalBudget: Float,
    expenseViewModel: ExpenseViewModel,
    modifier: Modifier = Modifier
) {
    if (isLandscape) {
        Column(modifier = modifier) {
            HistoryHeader(
                currentYear = currentYear,
                onPreviousYearClick = { expenseViewModel.previousYear() },
                onNextYearClick = { expenseViewModel.nextYear() }
            )
            Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    YearlyBarChart(
                        monthlyTotals = monthlyTotals,
                        showRemaining = showRemaining,
                        totalBudget = totalBudget
                    )
                    YearlyTotalCard(
                        yearlyExpenses = yearlyExpenses,
                        showRemaining = showRemaining,
                        totalBudget = totalBudget,
                        currencyPreference = currencyPreference
                    )
                }
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState())
                ) {
                    MonthlyBreakdownList(
                        monthlyTotals = monthlyTotals,
                        showRemaining = showRemaining,
                        totalBudget = totalBudget,
                        currencyPreference = currencyPreference
                    )
                }
            }
        }
    } else {
        Column(modifier = modifier.verticalScroll(rememberScrollState())) {
            HistoryHeader(
                currentYear = currentYear,
                onPreviousYearClick = { expenseViewModel.previousYear() },
                onNextYearClick = { expenseViewModel.nextYear() }
            )
            YearlyBarChart(
                monthlyTotals = monthlyTotals,
                showRemaining = showRemaining,
                totalBudget = totalBudget
            )
            YearlyTotalCard(
                yearlyExpenses = yearlyExpenses,
                showRemaining = showRemaining,
                totalBudget = totalBudget,
                currencyPreference = currencyPreference
            )
            MonthlyBreakdownList(
                monthlyTotals = monthlyTotals,
                showRemaining = showRemaining,
                totalBudget = totalBudget,
                currencyPreference = currencyPreference
            )
        }
    }
}
