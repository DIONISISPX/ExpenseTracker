package com.dionisispx.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.dionisispx.expensetracker.presentation.ExpenseViewModel
import com.dionisispx.expensetracker.presentation.navigation.MainScreen
import com.dionisispx.expensetracker.ui.theme.ExpenseTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Get the ViewModel to access DataStore
            val viewModel: ExpenseViewModel = hiltViewModel()

            // Get theme preference
            val themePreference by viewModel.themePreference.collectAsState()

            // Enable dark mode based on the user's saved choice
            val isDarkTheme = when (themePreference.lowercase()) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            ExpenseTrackerTheme(darkTheme = isDarkTheme) {
                MainScreen()
            }
        }
    }
}