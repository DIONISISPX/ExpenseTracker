package com.dionisispx.expensetracker.presentation.home.components.dashboard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dionisispx.expensetracker.domain.model.Expense
import com.dionisispx.expensetracker.presentation.ExpenseViewModel
import com.dionisispx.expensetracker.presentation.home.components.ExpenseItem

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SubTabPager(
    pagerState: PagerState,
    expenses: List<Expense>,
    categoryLimits: Map<String, Float>,
    totalBudget: Float,
    currencyPreference: String,
    viewModel: ExpenseViewModel
) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        if (page == 0) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(expenses) { expense ->
                    ExpenseItem(
                        expense = expense,
                        currencySymbol = currencyPreference,
                        onDeleteClick = { viewModel.deleteExpense(it) }
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    MasterProgressCard(expenses = expenses, totalBudget = totalBudget, currencySymbol = currencyPreference)
                }
                items(categoryLimits.entries.toList()) { limitEntry ->
                    val category = limitEntry.key
                    val limitAmount = limitEntry.value
                    val spentInCategory = expenses.filter { it.category == category }.sumOf { it.amount }.toFloat()

                    CategoryProgressRow(
                        categoryName = category,
                        spentAmount = spentInCategory,
                        limitAmount = limitAmount,
                        currencySymbol = currencyPreference
                    )
                }
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}
