package com.dionisispx.expensetracker.presentation.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dionisispx.expensetracker.R
import com.dionisispx.expensetracker.domain.model.ExpenseCategory
import com.dionisispx.expensetracker.presentation.util.CurrencyUtils
import com.dionisispx.expensetracker.presentation.preferences.PreferencesViewModel
import com.dionisispx.expensetracker.presentation.util.getCategoryDetails
import com.dionisispx.expensetracker.presentation.util.getLocalizedCategoryName
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetSettingsScreen(
    onNavigateBack: () -> Unit,
    budgetViewModel: BudgetViewModel = hiltViewModel(),
    prefsViewModel: PreferencesViewModel = hiltViewModel()
) {
    // Observe database values
    val savedTotalBudget by budgetViewModel.totalBudget.collectAsState()
    val savedCategoryLimits by budgetViewModel.categoryLimits.collectAsState()
    val currencySymbol by prefsViewModel.currencyPreference.collectAsState()

    // Local state variables
    var overallBudgetInput by remember { mutableStateOf("") }
    var categoryLimits by remember { mutableStateOf<Map<ExpenseCategory, Float>>(emptyMap()) }
    var isInitialized by remember { mutableStateOf(false) }

    // Load data once
    LaunchedEffect(savedTotalBudget, savedCategoryLimits) {
        if (!isInitialized && savedTotalBudget > 0f) {
            overallBudgetInput = savedTotalBudget.toString()
            categoryLimits = savedCategoryLimits
            isInitialized = true
        }
    }

    val overallBudget = overallBudgetInput.toFloatOrNull() ?: 0f

    // Toggle mode
    var isPercentMode by remember { mutableStateOf(false) }

    // Card background color
    val cardColor = MaterialTheme.colorScheme.surfaceVariant

    // Define categories
    val categories = ExpenseCategory.entries.map { categoryName ->
        val localizedName = getLocalizedCategoryName(categoryName)
        val details = getCategoryDetails(categoryName)
        CategoryData(categoryName, localizedName, details.first, details.second)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.set_budget_limits), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            budgetViewModel.saveBudgetAndLimits(overallBudget.toInt(), categoryLimits)
                            onNavigateBack()
                        },
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.save),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 16.sp
                        )
                    }
                },
                windowInsets = WindowInsets(0.dp)
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

            // Overall monthly budget card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.total_monthly_budget),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Integer input
                    MinimalistIntegerInput(
                        value = overallBudgetInput,
                        onValueChange = { input ->
                            overallBudgetInput = input
                            val newOverall = input.toFloatOrNull() ?: 0f
                            if (categoryLimits.values.sum() > newOverall) {
                                categoryLimits = emptyMap()
                            }
                        },
                        labelSymbol = currencySymbol,
                        maxValue = 1000000L,
                        fontSize = 40.sp,
                        isSymbolOnLeft = (currencySymbol == "$"),
                        modifier = Modifier.width(200.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Limits header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.limits_per_category),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.optional),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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

            // Main list card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .padding(bottom = 32.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {

                    // Reset button and tracker
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
                            stringResource(R.string.remaining) + ": ${String.format(Locale.US, "%.0f", pct)}%"
                        } else {
                            stringResource(R.string.remaining) + ": " + CurrencyUtils.formatCurrencyNoDecimals(remainingEur, currencySymbol)
                        }

                        Text(
                            text = remainingText,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (remainingEur <= 0f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )

                        TextButton(
                            onClick = { categoryLimits = emptyMap() },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(stringResource(R.string.reset), color = MaterialTheme.colorScheme.error)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))

                    categories.forEachIndexed { index, categoryData ->

                        // Calculate maximum allowed limit
                        val currentLimit = categoryLimits[categoryData.internalName] ?: 0f
                        val otherCategoriesSum = categoryLimits.filterKeys { it != categoryData.internalName }.values.sum()
                        val maxAllowedEurForCategory = (overallBudget - otherCategoriesSum).coerceAtLeast(0f)

                        CategoryLimitRow(
                            categoryData = categoryData,
                            currentLimitEur = currentLimit,
                            maxAllowedEur = maxAllowedEurForCategory,
                            overallBudget = overallBudget,
                            isPercentMode = isPercentMode,
                            currencySymbol = currencySymbol,
                            onLimitChange = { newEurValue ->
                                val clampedEurValue = newEurValue.coerceAtMost(maxAllowedEurForCategory)
                                categoryLimits = categoryLimits.toMutableMap().apply { put(categoryData.internalName, clampedEurValue) }
                            }
                        )

                        // Add dividers
                        if (index < categories.size - 1) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f), modifier = Modifier.padding(start = 72.dp, end = 16.dp))
                        }
                    }
                }
            }
        }
    }
}

// Category visual info
data class CategoryData(val internalName: ExpenseCategory, val displayName: String, val icon: ImageVector, val color: Color)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryLimitRow(
    categoryData: CategoryData,
    currentLimitEur: Float,
    maxAllowedEur: Float,
    overallBudget: Float,
    isPercentMode: Boolean,
    currencySymbol: String,
    onLimitChange: (Float) -> Unit
) {
    // Calculate display value
    val displayValue = if (isPercentMode && overallBudget > 0f) {
        ((currentLimitEur / overallBudget) * 100f).toInt().toFloat()
    } else {
        currentLimitEur.toInt().toFloat()
    }

    // Format text field state
    var textFieldValue by remember(displayValue, isPercentMode) {
        mutableStateOf(if (displayValue == 0f) "" else displayValue.toInt().toString())
    }

    // Cap input to prevent exceeding budget
    val currentMax = if (isPercentMode) {
        if (overallBudget > 0f) ((maxAllowedEur / overallBudget) * 100f).toLong() else 0L
    } else {
        maxAllowedEur.toLong()
    }

    val isAppDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    // Set icon tint based on theme
    val iconTint = if (isAppDark) MaterialTheme.colorScheme.surfaceVariant else Color.White

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Box for explicit tint control
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(48.dp)
                .background(categoryData.color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = categoryData.icon,
                contentDescription = categoryData.displayName,
                tint = iconTint
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f).heightIn(min = 64.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = categoryData.displayName,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Slider
                Slider(
                    value = displayValue,
                    onValueChange = { newValue ->
                        val snappedValue = newValue.roundToInt().toFloat()
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
                        inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    ),
                    thumb = {
                        Box(
                            modifier = Modifier
                                .size(width = 6.dp, height = 24.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(3.dp))
                        )
                    }
                )

                // Fixed width input container
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
                        labelSymbol = if (isPercentMode) "%" else currencySymbol,
                        maxValue = currentMax,
                        fontSize = 18.sp,
                        isSymbolOnLeft = false,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Conversion text
                    val conversionText = if (currentLimitEur > 0f) {
                        if (isPercentMode) {
                            if (currencySymbol == "$") {
                                "$${currentLimitEur.toInt()}"
                            } else {
                                "${currentLimitEur.toInt()} $currencySymbol"
                            }
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.height(16.dp)
                    )
                }
            }
        }
    }
}

// Custom integer input component
@Composable
fun MinimalistIntegerInput(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    labelSymbol: String,
    maxValue: Long,
    fontSize: TextUnit,
    isSymbolOnLeft: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        if (isSymbolOnLeft) {
            Text(
                text = labelSymbol,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = if (fontSize.value > 20f) 4.dp else 2.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }

        BasicTextField(
            value = value,
            onValueChange = { input ->
                // Strip non-digits
                var cleanInput = input.filter { it.isDigit() }

                // Enforce dynamic limits
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
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                    )
                }
            }
        )

        if (!isSymbolOnLeft) {
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
}