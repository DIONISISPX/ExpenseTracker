package com.dionisispx.expensetracker.presentation.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dionisispx.expensetracker.R
import com.dionisispx.expensetracker.presentation.SharedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingPrefsScreen(
    viewModel: SharedViewModel,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
    onSkipClick: () -> Unit
) {
    val language by viewModel.languagePreference.collectAsState()
    val currency by viewModel.currencyPreference.collectAsState()
    val theme by viewModel.themePreference.collectAsState()

    var isLanguageDropdownExpanded by remember { mutableStateOf(false) }
    var isCurrencyDropdownExpanded by remember { mutableStateOf(false) }
    var isThemeDropdownExpanded by remember { mutableStateOf(false) }

    val themeOptions = listOf(
        Pair("system", stringResource(R.string.theme_system)),
        Pair("light", stringResource(R.string.theme_light)),
        Pair("dark", stringResource(R.string.theme_dark))
    )

    val currencyOptions = listOf(
        Pair("€", stringResource(R.string.currency_euro)),
        Pair("$", stringResource(R.string.currency_dollar))
    )

    val languageOptions = listOf(
        Pair("el", stringResource(R.string.language_el)),
        Pair("en", stringResource(R.string.language_en))
    )

    val displayCurrency = when (currency) {
        "$" -> stringResource(R.string.currency_dollar)
        "€" -> stringResource(R.string.currency_euro)
        else -> stringResource(R.string.currency_euro)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                windowInsets = WindowInsets(0.dp)
            )
        },
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.onboarding_prefs_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.onboarding_prefs_message),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Language Dropdown
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.language),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = isLanguageDropdownExpanded,
                    onExpandedChange = { isLanguageDropdownExpanded = !isLanguageDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = languageOptions.find { it.first == language }?.second ?: stringResource(R.string.language_el),
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isLanguageDropdownExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = isLanguageDropdownExpanded,
                        onDismissRequest = { isLanguageDropdownExpanded = false }
                    ) {
                        languageOptions.forEach { (value, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    if (language != value) {
                                        viewModel.saveLanguagePreference(value)
                                    }
                                    isLanguageDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Currency Dropdown
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.currency),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = isCurrencyDropdownExpanded,
                    onExpandedChange = { isCurrencyDropdownExpanded = !isCurrencyDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = displayCurrency,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCurrencyDropdownExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = isCurrencyDropdownExpanded,
                        onDismissRequest = { isCurrencyDropdownExpanded = false }
                    ) {
                        currencyOptions.forEach { (symbol, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    viewModel.saveCurrencyPreference(symbol)
                                    isCurrencyDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Theme Dropdown
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.app_theme),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = isThemeDropdownExpanded,
                    onExpandedChange = { isThemeDropdownExpanded = !isThemeDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = themeOptions.find { it.first == theme.lowercase() }?.second ?: stringResource(R.string.theme_system),
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isThemeDropdownExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = isThemeDropdownExpanded,
                        onDismissRequest = { isThemeDropdownExpanded = false }
                    ) {
                        themeOptions.forEach { (value, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    viewModel.saveThemePreference(value)
                                    isThemeDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onSkipClick) {
                    Text(text = stringResource(R.string.skip), fontSize = 16.sp)
                }

                Button(
                    onClick = onNextClick,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(text = stringResource(R.string.next), fontSize = 16.sp)
                }
            }
        }
    }
}
