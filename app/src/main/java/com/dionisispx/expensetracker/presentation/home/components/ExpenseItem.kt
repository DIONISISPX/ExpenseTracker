package com.dionisispx.expensetracker.presentation.home.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dionisispx.expensetracker.R
import com.dionisispx.expensetracker.domain.model.Expense
import com.dionisispx.expensetracker.presentation.util.CurrencyUtils
import com.dionisispx.expensetracker.presentation.util.getCategoryDetails
import com.dionisispx.expensetracker.presentation.util.getLocalizedCategoryName
import java.time.Instant

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExpenseItem(
    expense: Expense,
    currencySymbol: String,
    onDeleteClick: (Expense) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    val expenseText = "- " + CurrencyUtils.formatCurrency(expense.amount.toFloat(), currencySymbol)

    // Evaluate string resources outside the dialog to prevent context reset
    val dialogTitle = stringResource(R.string.delete_expense_title)
    val dialogMessage = stringResource(R.string.delete_expense_message, expense.storeName)
    val btnDelete = stringResource(R.string.delete)
    val btnCancel = stringResource(R.string.cancel)

    val (icon, bgColor) = getCategoryDetails(expense.category)

    // Check luminance of the app background to guarantee pure white in light mode
    val isAppDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val iconTint = if (isAppDark) MaterialTheme.colorScheme.surfaceVariant else Color.White

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { },
                onLongClick = { showDeleteDialog = true }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Box ensures absolute explicit control over the tint
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(bgColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = expense.category,
                    tint = iconTint
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.storeName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                val localizedCategory = getLocalizedCategoryName(expense.category)

                Text(
                    text = localizedCategory,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = expenseText,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(dialogTitle) },
            text = { Text(dialogMessage) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick(expense)
                        showDeleteDialog = false
                    }
                ) {
                    Text(btnDelete, color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(btnCancel)
                }
            }
        )
    }
}
