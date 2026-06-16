package com.dionisispx.expensetracker.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

// Defines the screens in the app
sealed class Screen(val route: String, val title: String, val icon: ImageVector?) {

    // Home screen
    object Home : Screen("home", "Home", Icons.Default.Home)

    // Scanner or manual add screen. No icon (FAB)
    object AddExpense : Screen("add_expense", "Add", null)

    // Settings screen
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)

    // Budget settings screen
    object BudgetSettings : Screen("budget_settings", "Budget Settings", null)
}