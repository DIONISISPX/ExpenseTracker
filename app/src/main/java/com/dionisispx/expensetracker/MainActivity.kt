package com.dionisispx.expensetracker

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.dionisispx.expensetracker.presentation.ExpenseViewModel
import com.dionisispx.expensetracker.presentation.navigation.MainScreen
import com.dionisispx.expensetracker.ui.theme.ExpenseTrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: ExpenseViewModel = hiltViewModel()
            val themePreference by viewModel.themePreference.collectAsState()
            val languagePreference by viewModel.languagePreference.collectAsState()

            val isDarkTheme = when (themePreference.lowercase()) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            val context = LocalContext.current

            // Use context wrapper to override resources while keeping activity context to prevent hilt crashes
            val localizedContext = remember(languagePreference, context) {
                val locale = Locale(languagePreference)
                Locale.setDefault(locale)

                val config = Configuration(context.resources.configuration)
                config.setLocale(locale)
                config.setLayoutDirection(locale)

                // Update configuration directly to ensure localized system dialogs
                @Suppress("DEPRECATION")
                context.resources.updateConfiguration(config, context.resources.displayMetrics)

                object : ContextWrapper(context) {
                    private val localizedResources = context.createConfigurationContext(config).resources
                    override fun getResources() = localizedResources
                }
            }

            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalConfiguration provides localizedContext.resources.configuration
            ) {
                ExpenseTrackerTheme(darkTheme = isDarkTheme) {
                    MainScreen()
                }
            }
        }
    }
}