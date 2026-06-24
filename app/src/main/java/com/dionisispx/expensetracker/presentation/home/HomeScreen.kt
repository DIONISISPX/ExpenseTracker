package com.dionisispx.expensetracker.presentation.home

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.dionisispx.expensetracker.R
import com.dionisispx.expensetracker.presentation.ExpenseViewModel
import com.dionisispx.expensetracker.presentation.BudgetViewModel
import com.dionisispx.expensetracker.presentation.PreferencesViewModel
import com.dionisispx.expensetracker.presentation.home.components.dashboard.MonthSelectorAndChart
import com.dionisispx.expensetracker.presentation.home.components.dashboard.SubTabPager
import com.dionisispx.expensetracker.presentation.home.components.history.HistoryBreakdown

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    expenseViewModel: ExpenseViewModel = hiltViewModel(),
    budgetViewModel: BudgetViewModel = hiltViewModel(),
    prefsViewModel: PreferencesViewModel = hiltViewModel()
) {
    // Observe database and view model states
    val expenses by expenseViewModel.expenses.collectAsState()
    val yearlyExpenses by expenseViewModel.yearlyExpenses.collectAsState()
    val monthlyTotals by expenseViewModel.monthlyTotals.collectAsState()
    val currentMonth by expenseViewModel.currentMonth.collectAsState()
    val currentYear by expenseViewModel.currentYear.collectAsState()
    val languagePreference by prefsViewModel.languagePreference.collectAsState()
    val totalBudget by budgetViewModel.totalBudget.collectAsState()
    val categoryLimits by budgetViewModel.categoryLimits.collectAsState()
    val currencyPreference by prefsViewModel.currencyPreference.collectAsState()
    val showRemaining by budgetViewModel.showRemaining.collectAsState()

    // Setup interactive states
    var selectedMainTab by remember { mutableIntStateOf(0) }
    var selectedSubTab by remember { mutableIntStateOf(0) }

    val pagerState = rememberPagerState(pageCount = { 2 })
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val mainTabs = listOf(
        stringResource(R.string.tab_now),
        stringResource(R.string.tab_history)
    )
    val subTabs = listOf(
        stringResource(R.string.tab_transactions),
        stringResource(R.string.tab_limits)
    )

    // Sync sub tab clicks with pager swipes
    LaunchedEffect(selectedSubTab) {
        pagerState.animateScrollToPage(selectedSubTab)
    }
    LaunchedEffect(pagerState.currentPage) {
        selectedSubTab = pagerState.currentPage
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.app_name), fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { budgetViewModel.toggleShowRemaining() }) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Toggle budget view",
                            tint = if (showRemaining) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                windowInsets = WindowInsets(0.dp)
            )
        },
        contentWindowInsets = WindowInsets(0.dp)
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

            // Main tabs remain full width regardless of orientation
            TabRow(selectedTabIndex = selectedMainTab) {
                mainTabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedMainTab == index,
                        onClick = { selectedMainTab = index },
                        text = { Text(title, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            if (selectedMainTab == 0) {
                if (isLandscape) {
                    // Split layout for Now tab in landscape
                    Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState())) {
                            MonthSelectorAndChart(currentMonth, expenses, showRemaining, totalBudget.toFloat(), currencyPreference, languagePreference, expenseViewModel)
                        }
                        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            TabRow(selectedTabIndex = selectedSubTab) {
                                subTabs.forEachIndexed { index, title ->
                                    Tab(
                                        selected = selectedSubTab == index,
                                        onClick = { selectedSubTab = index },
                                        text = { Text(title) }
                                    )
                                }
                            }
                            SubTabPager(
                                pagerState = pagerState, expenses = expenses, categoryLimits = categoryLimits,
                                totalBudget = totalBudget.toFloat(), currencyPreference = currencyPreference,
                                onDeleteExpense = { expenseViewModel.deleteExpense(it) }
                            )
                        }
                    }
                } else {
                    // Standard portrait layout utilizing remaining height properly
                    Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        MonthSelectorAndChart(currentMonth, expenses, showRemaining, totalBudget.toFloat(), currencyPreference, languagePreference, expenseViewModel)

                        TabRow(selectedTabIndex = selectedSubTab) {
                            subTabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedSubTab == index,
                                    onClick = { selectedSubTab = index },
                                    text = { Text(title) }
                                )
                            }
                        }

                        SubTabPager(
                            pagerState = pagerState, expenses = expenses, categoryLimits = categoryLimits,
                            totalBudget = totalBudget.toFloat(), currencyPreference = currencyPreference,
                            onDeleteExpense = { expenseViewModel.deleteExpense(it) }
                        )
                    }
                }
            } else {
                // History tab securely fills remaining weight
                HistoryBreakdown(
                    yearlyExpenses = yearlyExpenses,
                    monthlyTotals = monthlyTotals,
                    currentYear = currentYear,
                    currencyPreference = currencyPreference,
                    isLandscape = isLandscape,
                    showRemaining = showRemaining,
                    totalBudget = totalBudget.toFloat(),
                    expenseViewModel = expenseViewModel,
                    modifier = Modifier.weight(1f).fillMaxWidth()
                )
            }
        }
    }
}