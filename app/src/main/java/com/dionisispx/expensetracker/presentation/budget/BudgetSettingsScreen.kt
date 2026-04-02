package com.dionisispx.expensetracker.presentation.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetSettingsScreen(
    onNavigateBack: () -> Unit,
    onSave: () -> Unit = {}
) {
    // Top level budget state
    var overallBudgetInput by remember { mutableStateOf("1000") }
    val overallBudget = overallBudgetInput.toFloatOrNull() ?: 0f

    // Toggle between euro and percent modes
    var isPercentMode by remember { mutableStateOf(false) }

    // Map to hold the limits in raw euros
    var categoryLimits by remember { mutableStateOf(mapOf<String, Float>()) }

    // Define categories with their specific icons and pastel colors
    val categories = listOf(
        CategoryData("Groceries", Icons.Default.ShoppingCart, Color(0xFFC8E6C9)),
        CategoryData("Food & Drink", Icons.Default.Restaurant, Color(0xFFFFCCBC)),
        CategoryData("Transport & Fuel", Icons.Default.DirectionsCar, Color(0xFFBBDEFB)),
        CategoryData("Shopping", Icons.Default.LocalMall, Color(0xFFE1BEE7)),
        CategoryData("Entertainment", Icons.Default.Movie, Color(0xFFFFF9C4)),
        CategoryData("Bills & Utilities", Icons.Default.Receipt, Color(0xFFB2DFDB)),
        CategoryData("Health & Fitness", Icons.Default.Favorite, Color(0xFFFFCDD2)),
        CategoryData("Travel", Icons.Default.Flight, Color(0xFFB2EBF2)),
        CategoryData("Home", Icons.Default.Home, Color(0xFFD7CCC8)),
        CategoryData("Education", Icons.Default.School, Color(0xFFF5F5F5)),
        CategoryData("Personal Care", Icons.Default.Spa, Color(0xFFF8BBD0)),
        CategoryData("Other", Icons.Default.MoreHoriz, Color(0xFFCFD8DC))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Set budget limits", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onSave) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Save", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Overall monthly budget section white card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Total monthly budget",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Minimalist integer input with logic to clear categories if budget shrinks too much
                    MinimalistIntegerInput(
                        value = overallBudgetInput,
                        onValueChange = { input ->
                            overallBudgetInput = input
                            val newOverall = input.toFloatOrNull() ?: 0f
                            if (categoryLimits.values.sum() > newOverall) {
                                categoryLimits = emptyMap()
                            }
                        },
                        labelSymbol = "€",
                        maxValue = 1000000L,
                        fontSize = 40.sp,
                        modifier = Modifier.width(200.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Limits header and toggle icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Limits per category",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "(optional)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                IconButton(onClick = { isPercentMode = !isPercentMode }) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Toggle mode",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main list container
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .padding(bottom = 32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {

                    // Reset Button and Remaining Tracker inside the container
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val totalAllocated = categoryLimits.values.sum()
                        val remainingEur = (overallBudget - totalAllocated).coerceAtLeast(0f)

                        val remainingText = if (isPercentMode) {
                            val pct = if (overallBudget > 0f) (remainingEur / overallBudget) * 100f else 0f
                            "Remaining: ${String.format(Locale.US, "%.0f", pct)}%"
                        } else {
                            "Remaining: ${String.format(Locale.US, "%.0f", remainingEur)} €"
                        }

                        Text(
                            text = remainingText,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (remainingEur <= 0f) MaterialTheme.colorScheme.error else Color.Gray,
                            fontWeight = FontWeight.Bold
                        )

                        TextButton(
                            onClick = { categoryLimits = emptyMap() },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Reset", color = MaterialTheme.colorScheme.error)
                        }
                    }

                    Divider(color = Color.LightGray.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp))

                    categories.forEachIndexed { index, categoryData ->

                        // Calculate mathematical maximum allowed for this specific category
                        val currentLimit = categoryLimits[categoryData.name] ?: 0f
                        val otherCategoriesSum = categoryLimits.filterKeys { it != categoryData.name }.values.sum()
                        val maxAllowedEurForCategory = (overallBudget - otherCategoriesSum).coerceAtLeast(0f)

                        CategoryLimitRow(
                            categoryData = categoryData,
                            currentLimitEur = currentLimit,
                            maxAllowedEur = maxAllowedEurForCategory,
                            overallBudget = overallBudget,
                            isPercentMode = isPercentMode,
                            onLimitChange = { newEurValue ->
                                val clampedEurValue = newEurValue.coerceAtMost(maxAllowedEurForCategory)
                                categoryLimits = categoryLimits.toMutableMap().apply { put(categoryData.name, clampedEurValue) }
                            }
                        )

                        // Add a subtle divider between items except for the last one
                        if (index < categories.size - 1) {
                            Divider(color = Color.LightGray.copy(alpha = 0.5f), modifier = Modifier.padding(start = 72.dp, end = 16.dp))
                        }
                    }
                }
            }
        }
    }
}

// Data class to hold category visual info
data class CategoryData(val name: String, val icon: ImageVector, val color: Color)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryLimitRow(
    categoryData: CategoryData,
    currentLimitEur: Float,
    maxAllowedEur: Float,
    overallBudget: Float,
    isPercentMode: Boolean,
    onLimitChange: (Float) -> Unit
) {
    // Calculate display value based on the current mode
    val displayValue = if (isPercentMode && overallBudget > 0f) {
        ((currentLimitEur / overallBudget) * 100f).toInt().toFloat()
    } else {
        currentLimitEur.toInt().toFloat()
    }

    // Format state for text field to show integers smoothly
    var textFieldValue by remember(displayValue, isPercentMode) {
        mutableStateOf(if (displayValue == 0f) "" else displayValue.toInt().toString())
    }

    // Dynamic max value tightly caps input to prevent exceeding 100 percent of total budget
    val currentMax = if (isPercentMode) {
        if (overallBudget > 0f) ((maxAllowedEur / overallBudget) * 100f).toLong() else 0L
    } else {
        maxAllowedEur.toLong()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Colored icon background
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(48.dp)
                .background(categoryData.color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = categoryData.icon,
                contentDescription = categoryData.name,
                tint = Color.Black.copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f).heightIn(min = 64.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = categoryData.name,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Customized slider with vertical line handle
                Slider(
                    value = displayValue,
                    onValueChange = { newValue ->
                        val snappedValue = Math.round(newValue).toFloat()
                        val newEurValue = if (isPercentMode) {
                            (snappedValue / 100f) * overallBudget
                        } else {
                            snappedValue
                        }
                        onLimitChange(newEurValue)
                    },
                    valueRange = if (isPercentMode) 0f..100f else 0f..(if (overallBudget > 0f) overallBudget else 1000000f),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 16.dp),
                    colors = SliderDefaults.colors(
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = Color.LightGray.copy(alpha = 0.5f)
                    ),
                    thumb = {
                        Box(
                            modifier = Modifier
                                .size(width = 6.dp, height = 24.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(3.dp))
                        )
                    }
                )

                // Fixed width container for input prevents layout from shifting
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(90.dp)
                ) {
                    MinimalistIntegerInput(
                        value = textFieldValue,
                        onValueChange = { cleanInput ->
                            textFieldValue = cleanInput
                            val parsedVal = cleanInput.toFloatOrNull() ?: 0f
                            val newEurValue = if (isPercentMode) {
                                (parsedVal / 100f) * overallBudget
                            } else {
                                parsedVal
                            }
                            onLimitChange(newEurValue)
                        },
                        labelSymbol = if (isPercentMode) "%" else "€",
                        maxValue = currentMax,
                        fontSize = 18.sp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Conversion helper text reserves space even when empty
                    val conversionText = if (currentLimitEur > 0f) {
                        if (isPercentMode) {
                            "${currentLimitEur.toInt()} €"
                        } else if (overallBudget > 0f) {
                            "${((currentLimitEur / overallBudget) * 100f).toInt()}%"
                        } else {
                            " "
                        }
                    } else {
                        " "
                    }

                    Text(
                        text = conversionText,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.height(16.dp)
                    )
                }
            }
        }
    }
}

// Fixed width integer input component that completely eliminates horizontal scroll bugs
@Composable
fun MinimalistIntegerInput(
    value: String,
    onValueChange: (String) -> Unit,
    labelSymbol: String,
    maxValue: Long,
    fontSize: TextUnit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        BasicTextField(
            value = value,
            onValueChange = { input ->
                // Strip out any non digits
                var cleanInput = input.filter { it.isDigit() }

                // Enforce dynamic limits perfectly
                val parsedVal = cleanInput.toLongOrNull() ?: 0L
                if (parsedVal > maxValue) {
                    cleanInput = maxValue.toString()
                }

                onValueChange(cleanInput)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = TextStyle(
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            ),
            singleLine = true,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = Modifier.weight(1f),
            decorationBox = { innerTextField ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    innerTextField()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (fontSize.value > 20f) 2.dp else 1.dp)
                            .background(Color.Gray.copy(alpha = 0.5f))
                    )
                }
            }
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = labelSymbol,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = if (fontSize.value > 20f) 4.dp else 2.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BudgetSettingsScreenPreview() {
    MaterialTheme {
        BudgetSettingsScreen(onNavigateBack = {})
    }
}