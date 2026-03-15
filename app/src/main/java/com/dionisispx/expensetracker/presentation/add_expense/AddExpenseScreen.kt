package com.dionisispx.expensetracker.presentation.add_expense

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dionisispx.expensetracker.domain.model.Expense
import com.dionisispx.expensetracker.presentation.ExpenseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    // We need to go back after saving
    onNavigateBack: () -> Unit,
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    // State variables for our form fields
    var storeName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Other") }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    // 12 Preset Categories
    val categories = listOf(
        "Groceries", "Food & Drink", "Transport & Fuel", "Shopping",
        "Entertainment", "Bills & Utilities", "Health & Fitness",
        "Travel", "Home", "Education", "Personal Care", "Other"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Expense") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Store name input field
            OutlinedTextField(
                // Update value to storeName and onValueChange to set storeName
                value = storeName,
                onValueChange = { storeName = it },
                label = { Text("Store name (e.g. Cafe)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Amount input field (Numbers only)
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount (€)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )

            // Category dropdown menu
            ExposedDropdownMenuBox(
                expanded = isDropdownExpanded,
                onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = isDropdownExpanded,
                    onDismissRequest = { isDropdownExpanded = false }
                ) {
                    categories.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                category = selectionOption
                                isDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Save button
            Button(
                onClick = {
                    if (storeName.isNotBlank() && amount.isNotBlank()) {
                        val newExpense = Expense(
                            storeName = storeName,
                            amount = amount.toDoubleOrNull() ?: 0.0,
                            category = category,
                            date = System.currentTimeMillis() // Save current time timestamp
                        )
                        viewModel.addExpense(newExpense)
                        onNavigateBack() // Go back to Home Screen
                    }
                }
            ) {
                Text("Save Expense")
            }
        }
    }
}