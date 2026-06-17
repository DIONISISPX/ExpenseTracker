package com.dionisispx.expensetracker.presentation.add_expense.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.unit.dp
import com.dionisispx.expensetracker.R
import com.dionisispx.expensetracker.domain.model.Expense
import com.dionisispx.expensetracker.presentation.SharedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualExpenseForm(
    storeName: String, onStoreNameChange: (String) -> Unit,
    amount: String, onAmountChange: (String) -> Unit,
    category: String, onCategoryChange: (String) -> Unit,
    currencySymbol: String,
    onNavigateBack: () -> Unit,
    viewModel: SharedViewModel
) {
    var isDropdownExpanded by remember { mutableStateOf(false) }
    val categories = listOf(
        Pair("Groceries", stringResource(R.string.cat_groceries)),
        Pair("Food & Drink", stringResource(R.string.cat_food_drink)),
        Pair("Transport & Fuel", stringResource(R.string.cat_transport)),
        Pair("Shopping", stringResource(R.string.cat_shopping)),
        Pair("Entertainment", stringResource(R.string.cat_entertainment)),
        Pair("Bills & Utilities", stringResource(R.string.cat_bills)),
        Pair("Health & Fitness", stringResource(R.string.cat_health)),
        Pair("Travel", stringResource(R.string.cat_travel)),
        Pair("Home", stringResource(R.string.cat_home)),
        Pair("Education", stringResource(R.string.cat_education)),
        Pair("Personal Care", stringResource(R.string.cat_personal)),
        Pair("Other", stringResource(R.string.cat_other))
    )

    val amountLabel = if (currencySymbol == "$") {
        stringResource(R.string.amount_label_left, currencySymbol)
    } else {
        stringResource(R.string.amount_label_right, currencySymbol)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = storeName,
            onValueChange = onStoreNameChange,
            label = { Text(stringResource(R.string.store_name)) },
            placeholder = { Text(stringResource(R.string.store_name_placeholder), color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = { text ->
                TransformedText(
                    AnnotatedString(text.text.uppercase()),
                    OffsetMapping.Identity
                )
            }
        )

        OutlinedTextField(
            value = amount, onValueChange = onAmountChange,
            label = { Text(amountLabel) },
            placeholder = { Text(stringResource(R.string.amount_placeholder), color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true
        )

        ExposedDropdownMenuBox(expanded = isDropdownExpanded, onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }) {
            OutlinedTextField(
                value = categories.find { it.first == category }?.second ?: stringResource(R.string.cat_other),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.category)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier
                    .menuAnchor(
                        type = androidx.compose.material3.MenuAnchorType.PrimaryNotEditable,
                        enabled = true
                    )
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = isDropdownExpanded, onDismissRequest = { isDropdownExpanded = false }) {
                categories.forEach { (internalName, displayName) ->
                    DropdownMenuItem(
                        text = { Text(displayName) },
                        onClick = {
                            onCategoryChange(internalName)
                            isDropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            modifier = Modifier.fillMaxWidth().height(56.dp),
            onClick = {
                if (storeName.isNotBlank() && amount.isNotBlank()) {
                    val newExpense = Expense(
                        storeName = storeName.uppercase(),
                        amount = amount.replace(",", ".").toDoubleOrNull() ?: 0.0,
                        category = category,
                        date = System.currentTimeMillis()
                    )
                    viewModel.addExpense(newExpense)
                    onNavigateBack()
                }
            }
        ) {
            Text(stringResource(R.string.save_expense), fontWeight = FontWeight.Bold)
        }
    }
}
