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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.dionisispx.expensetracker.presentation.budget.BudgetViewModel
import com.dionisispx.expensetracker.presentation.preferences.PreferencesViewModel
import com.dionisispx.expensetracker.presentation.budget.MinimalistIntegerInput

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingBudgetScreen(
    budgetViewModel: BudgetViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    prefsViewModel: PreferencesViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    onBackClick: () -> Unit,
    onSetCategoryLimitsClick: () -> Unit,
    onDoneClick: () -> Unit,
    onSkipClick: () -> Unit
) {
    val savedTotalBudget by budgetViewModel.totalBudget.collectAsState()
    val savedCategoryLimits by budgetViewModel.categoryLimits.collectAsState()
    val currencySymbol by prefsViewModel.currencyPreference.collectAsState()

    var overallBudgetInput by remember { mutableStateOf("") }
    var isInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(savedTotalBudget) {
        if (!isInitialized && savedTotalBudget > 0) {
            overallBudgetInput = savedTotalBudget.toString()
            isInitialized = true
        }
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
                imageVector = Icons.Default.AccountBalanceWallet,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.onboarding_budget_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.onboarding_budget_message),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = stringResource(R.string.total_monthly_budget),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            MinimalistIntegerInput(
                value = overallBudgetInput,
                onValueChange = { input -> overallBudgetInput = input },
                labelSymbol = currencySymbol,
                maxValue = 1000000L,
                fontSize = 40.sp,
                isSymbolOnLeft = (currencySymbol == "$"),
                modifier = Modifier.width(200.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = { 
                    val budgetValue = overallBudgetInput.toIntOrNull() ?: savedTotalBudget
                    budgetViewModel.saveBudgetAndLimits(budgetValue, savedCategoryLimits)
                    onSetCategoryLimitsClick() 
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
            ) {
                Text(
                    text = stringResource(R.string.limits_per_category),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onSkipClick) {
                    Text(text = stringResource(R.string.skip), fontSize = 16.sp)
                }

                Button(
                    onClick = {
                        val budgetValue = overallBudgetInput.toIntOrNull() ?: savedTotalBudget
                        budgetViewModel.saveBudgetAndLimits(budgetValue, savedCategoryLimits)
                        onDoneClick()
                    },
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(text = stringResource(R.string.done), fontSize = 16.sp)
                }
            }
        }
    }
}
