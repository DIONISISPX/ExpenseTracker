package com.dionisispx.expensetracker.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

// Defines the screens in the app
sealed class Screen(val route: String, val icon: ImageVector?) {

    // Home screen
    object Home : Screen("home", Icons.Default.Home)

    // Scanner or manual add screen. No icon (FAB)
    object AddExpense : Screen("add_expense", null)

    // Settings screen
    object Settings : Screen("settings", Icons.Default.Settings)

    // Budget settings screen
    object BudgetSettings : Screen("budget_settings", null)

    // Onboarding screens
    object OnboardingWelcome : Screen("onboarding_welcome", null)
    object OnboardingPrefs : Screen("onboarding_prefs", null)
    object OnboardingBudget : Screen("onboarding_budget", null)
    object OnboardingBudgetDetailed : Screen("onboarding_budget_detailed", null)
}