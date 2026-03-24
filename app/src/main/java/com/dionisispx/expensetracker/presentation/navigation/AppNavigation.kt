package com.dionisispx.expensetracker.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dionisispx.expensetracker.presentation.add_expense.AddExpenseScreen
import com.dionisispx.expensetracker.presentation.home.HomeScreen

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    // Controller that manages app navigation
    val navController = rememberNavController()

    // Get current route to highlight the selected item and hide/show bottom bar
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Boolean to check if we should show the bottom bar (hide it on AddExpense screen)
    val showBottomBar = currentRoute != Screen.AddExpense.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // Only show the bottom bar if showBottomBar is true
            if (showBottomBar) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BottomAppBar(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        // Home Icon Button (Left)
                        IconButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        ) {
                            Screen.Home.icon?.let {
                                Icon(
                                    imageVector = it,
                                    contentDescription = Screen.Home.title,
                                    tint = if (currentRoute == Screen.Home.route) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Settings Icon Button (Right)
                        IconButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                navController.navigate(Screen.Settings.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        ) {
                            Screen.Settings.icon?.let {
                                Icon(
                                    imageVector = it,
                                    contentDescription = Screen.Settings.title,
                                    tint = if (currentRoute == Screen.Settings.route) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

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
                            .offset(y = (-20).dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Expense"
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
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
                SettingsScreen()
            }
        }
    }
}

// Dummy screens just to test the navigation
@Composable
fun SettingsScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Settings Screen - Preferences Go Here")
    }
}