package com.dionisispx.expensetracker.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.material3.Text
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dionisispx.expensetracker.R
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.dionisispx.expensetracker.presentation.PreferencesViewModel
import com.dionisispx.expensetracker.presentation.add_expense.AddExpenseScreen
import com.dionisispx.expensetracker.presentation.home.HomeScreen
import com.dionisispx.expensetracker.presentation.budget.BudgetSettingsScreen
import com.dionisispx.expensetracker.presentation.settings.SettingsScreen
import com.dionisispx.expensetracker.presentation.onboarding.OnboardingWelcomeScreen
import com.dionisispx.expensetracker.presentation.onboarding.OnboardingPrefsScreen
import com.dionisispx.expensetracker.presentation.onboarding.OnboardingBudgetScreen

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val prefsViewModel: PreferencesViewModel = hiltViewModel()
    val isFirstRun by prefsViewModel.isFirstRun.collectAsState()

    if (isFirstRun == null) {
        return // Wait for preference to load
    }

    val startDestination = remember {
        if (isFirstRun == true) Screen.OnboardingWelcome.route else Screen.Home.route
    }

    // Controller that manages app navigation
    val navController = rememberNavController()

    // Get current route to highlight the selected item and hide/show bottom bar
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Boolean to check if we should show the bottom bar (hide it on AddExpense, Budget settings, and onboarding)
    val showBottomBar = currentRoute != Screen.AddExpense.route && 
                        currentRoute != Screen.BudgetSettings.route &&
                        currentRoute != Screen.OnboardingWelcome.route &&
                        currentRoute != Screen.OnboardingPrefs.route &&
                        currentRoute != Screen.OnboardingBudget.route &&
                        currentRoute != Screen.OnboardingBudgetDetailed.route

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            // Only show the bottom bar if showBottomBar is true
            if (showBottomBar) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    NavigationBar(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Home Icon Button (Left)
                        val isHomeActive = currentRoute == Screen.Home.route
                        NavigationBarItem(
                            selected = isHomeActive,
                            onClick = {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(Screen.Home.icon!!, contentDescription = stringResource(R.string.tab_home)) },
                            label = { Text(stringResource(R.string.tab_home)) }
                        )

                        // Empty space for the center FAB
                        Spacer(modifier = Modifier.weight(1f))

                        // Settings Icon Button (Right)
                        val isSettingsActive = currentRoute == Screen.Settings.route
                        NavigationBarItem(
                            selected = isSettingsActive,
                            onClick = {
                                navController.navigate(Screen.Settings.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(Screen.Settings.icon!!, contentDescription = stringResource(R.string.tab_settings)) },
                            label = { Text(stringResource(R.string.tab_settings)) }
                        )
                    }
                    // Add Expense FAB (Center)
                    FloatingActionButton(
                        onClick = {
                            navController.navigate(Screen.AddExpense.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = (-24).dp)
                            .size(64.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Expense",
                            modifier = Modifier
                                .size(32.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding()
            ),
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None }
        ) {
            composable(Screen.Home.route) {
                HomeScreen()
            }
            composable(Screen.AddExpense.route) {
                AddExpenseScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateToBudget = {
                        navController.navigate(Screen.BudgetSettings.route)
                    }
                )
            }
            composable(Screen.BudgetSettings.route) {
                BudgetSettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.OnboardingWelcome.route) {
                OnboardingWelcomeScreen(
                    onNextClick = { navController.navigate(Screen.OnboardingPrefs.route) },
                    onSkipClick = {
                        prefsViewModel.setFirstRunCompleted()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.OnboardingWelcome.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.OnboardingPrefs.route) {
                OnboardingPrefsScreen(
                    onBackClick = { navController.popBackStack() },
                    onNextClick = { navController.navigate(Screen.OnboardingBudget.route) },
                    onSkipClick = {
                        prefsViewModel.setFirstRunCompleted()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.OnboardingWelcome.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.OnboardingBudget.route) {
                OnboardingBudgetScreen(
                    onBackClick = { navController.popBackStack() },
                    onSetCategoryLimitsClick = { navController.navigate(Screen.OnboardingBudgetDetailed.route) },
                    onDoneClick = {
                        prefsViewModel.setFirstRunCompleted()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.OnboardingWelcome.route) { inclusive = true }
                        }
                    },
                    onSkipClick = {
                        prefsViewModel.setFirstRunCompleted()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.OnboardingWelcome.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.OnboardingBudgetDetailed.route) {
                BudgetSettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
