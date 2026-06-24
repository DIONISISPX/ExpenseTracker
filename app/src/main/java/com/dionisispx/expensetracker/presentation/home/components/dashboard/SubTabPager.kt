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

import com.dionisispx.expensetracker.domain.model.ExpenseCategory
import com.dionisispx.expensetracker.presentation.home.components.ExpenseItem

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SubTabPager(
    pagerState: PagerState,
    expenses: List<Expense>,
    categoryLimits: Map<ExpenseCategory, Float>,
    totalBudget: Float,
    currencyPreference: String,
    onDeleteExpense: (Expense) -> Unit
) {
    // Displays a swipeable pager for expenses and budget overview
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        if (page == 0) {
            // Shows the list of expenses on the first page
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(expenses) { expense ->
                    ExpenseItem(
                        expense = expense,
                        currencySymbol = currencyPreference,
                        onDeleteClick = { onDeleteExpense(it) }
                    )
                }
            }
        } else {
            // Shows the budget progress overview on the second page
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    // Displays the overall budget progress
                    MasterProgressCard(expenses = expenses, totalBudget = totalBudget, currencySymbol = currencyPreference)
                }
                // Displays progress for each category
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
