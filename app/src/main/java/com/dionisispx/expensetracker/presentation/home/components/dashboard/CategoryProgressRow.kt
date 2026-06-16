package com.dionisispx.expensetracker.presentation.home.components.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dionisispx.expensetracker.R
import com.dionisispx.expensetracker.presentation.util.getCategoryDetails
import java.util.Locale

@Composable
fun CategoryProgressRow(
    categoryName: String,
    spentAmount: Float,
    limitAmount: Float,
    currencySymbol: String
) {
    val progress = if (limitAmount > 0f) (spentAmount / limitAmount).coerceIn(0f, 1f) else 0f
    val isOverBudget = spentAmount > limitAmount
    val barColor = if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    val percentText = if (limitAmount > 0f) "${((spentAmount / limitAmount) * 100).toInt()}%" else "0%"

    val formattedSpent = String.format(Locale.US, "%.0f", spentAmount)
    val formattedLimit = limitAmount.toInt().toString()
    val limitText = if (currencySymbol == "$") {
        "$$formattedSpent / $$formattedLimit"
    } else {
        "$formattedSpent $currencySymbol / $formattedLimit $currencySymbol"
    }

    val (icon, bgColor) = getCategoryDetails(categoryName)

    // Check luminance of the app background to guarantee pure white in light mode
    val isAppDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val iconTint = if (isAppDark) MaterialTheme.colorScheme.surface else Color.White

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Box ensures absolute explicit control over the tint
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(bgColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = categoryName,
                    tint = iconTint
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val localizedCategory = when (categoryName) {
                        "Groceries" -> stringResource(R.string.cat_groceries)
                        "Food & Drink" -> stringResource(R.string.cat_food_drink)
                        "Transport & Fuel" -> stringResource(R.string.cat_transport)
                        "Shopping" -> stringResource(R.string.cat_shopping)
                        "Entertainment" -> stringResource(R.string.cat_entertainment)
                        "Bills & Utilities" -> stringResource(R.string.cat_bills)
                        "Health & Fitness" -> stringResource(R.string.cat_health)
                        "Travel" -> stringResource(R.string.cat_travel)
                        "Home" -> stringResource(R.string.cat_home)
                        "Education" -> stringResource(R.string.cat_education)
                        "Personal Care" -> stringResource(R.string.cat_personal)
                        "Other" -> stringResource(R.string.cat_other)
                        else -> categoryName
                    }

                    Text(
                        text = localizedCategory,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = limitText,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.LightGray.copy(alpha = 0.3f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progress)
                                .background(barColor)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = percentText,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray,
                        modifier = Modifier.width(36.dp)
                    )
                }
            }
        }
    }
}
