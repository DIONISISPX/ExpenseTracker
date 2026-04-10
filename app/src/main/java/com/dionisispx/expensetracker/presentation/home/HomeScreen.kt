package com.dionisispx.expensetracker.presentation.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dionisispx.expensetracker.domain.model.Expense
import com.dionisispx.expensetracker.presentation.ExpenseViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    // Observe the list of expenses from the viewmodel
    val expenses by viewModel.expenses.collectAsState()
    val yearlyExpenses by viewModel.yearlyExpenses.collectAsState()

    // Observe the current month and year from viewmodel
    val currentMonth by viewModel.currentMonth.collectAsState()
    val currentYear by viewModel.currentYear.collectAsState()

    // Format the month in greek
    val formatter = remember {
        DateTimeFormatter.ofPattern("LLLL yyyy", Locale.forLanguageTag("el-GR"))
    }
    val monthString = currentMonth.format(formatter).replaceFirstChar { it.uppercase() }

    // State for the top icon toggle
    var showRemaining by remember { mutableStateOf(false) }

    // State for the main tabs
    var selectedMainTab by remember { mutableIntStateOf(0) }
    val mainTabs = listOf("Τώρα", "Ιστορικό")

    // State for the sub tabs
    var selectedSubTab by remember { mutableIntStateOf(0) }
    val subTabs = listOf("Συναλλαγές", "Όρια")

    // Observe live budget and limits from data store
    val totalBudget by viewModel.totalBudget.collectAsState()
    val categoryLimits by viewModel.categoryLimits.collectAsState()

    // Observe user currency preference
    val currencyPreference by viewModel.currencyPreference.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "LOGO", fontWeight = FontWeight.Bold)
                },
                actions = {
                    // Swap icon button to toggle view
                    IconButton(onClick = { showRemaining = !showRemaining }) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Toggle budget view",
                            tint = if (showRemaining) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            TabRow(selectedTabIndex = selectedMainTab) {
                mainTabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedMainTab == index,
                        onClick = { selectedMainTab = index },
                        text = { Text(title, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            if (selectedMainTab == 0) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.previousMonth() }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Prev")
                    }
                    Text(
                        text = monthString,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    IconButton(onClick = { viewModel.nextMonth() }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next")
                    }
                }

                // Dynamic donut chart updates based on the toggle state
                DonutChart(
                    expenses = expenses,
                    showRemaining = showRemaining,
                    totalBudget = totalBudget,
                    currencySymbol = currencyPreference
                )

                Spacer(modifier = Modifier.height(16.dp))

                TabRow(selectedTabIndex = selectedSubTab) {
                    subTabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedSubTab == index,
                            onClick = { selectedSubTab = index },
                            text = { Text(title) }
                        )
                    }
                }

                if (selectedSubTab == 0) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(expenses) { expense ->
                            ExpenseItem(
                                expense = expense,
                                currencySymbol = currencyPreference,
                                onDeleteClick = { viewModel.deleteExpense(it) }
                            )
                        }
                    }
                } else {
                    // Limits progress bars tab
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            MasterProgressCard(expenses = expenses, totalBudget = totalBudget, currencySymbol = currencyPreference)
                        }

                        items(categoryLimits.entries.toList()) { limitEntry ->
                            val category = limitEntry.key
                            val limitAmount = limitEntry.value
                            val spentInCategory = expenses.filter { it.category == category }.sumOf { it.amount }.toFloat()

                            CategoryProgressRow(
                                categoryName = category,
                                spentAmount = spentInCategory,
                                limitAmount = limitAmount,
                                currencySymbol = currencyPreference
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }

            } else {

                val monthlyTotals = FloatArray(12) { 0f }
                yearlyExpenses.forEach { expense ->
                    val date = Instant.ofEpochMilli(expense.date).atZone(ZoneId.systemDefault()).toLocalDate()
                    val monthIndex = date.monthValue - 1
                    monthlyTotals[monthIndex] += expense.amount.toFloat()
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.previousYear() }) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Prev Year")
                        }
                        Text(
                            text = currentYear.toString(),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                        IconButton(onClick = { viewModel.nextYear() }) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Year")
                        }
                    }

                    YearlyBarChart(monthlyTotals = monthlyTotals)

                    val yearlyTotal = yearlyExpenses.sumOf { it.amount }
                    val formattedYearlyTotal = String.format(Locale.US, "%.2f", yearlyTotal)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Σύνολο Έτους:", fontWeight = FontWeight.Bold)
                            Text(
                                text = if (currencyPreference == "$") "$$formattedYearlyTotal" else "$formattedYearlyTotal $currencyPreference",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Text(
                        text = "Ανάλυση ανά μήνα:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .padding(bottom = 32.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            val fullMonths = listOf("Ιανουάριος", "Φεβρουάριος", "Μάρτιος", "Απρίλιος", "Μάιος", "Ιούνιος", "Ιούλιος", "Αύγουστος", "Σεπτέμβριος", "Οκτώβριος", "Νοέμβριος", "Δεκέμβριος")

                            monthlyTotals.forEachIndexed { index, total ->
                                val formattedTotal = String.format(Locale.US, "%.2f", total)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(fullMonths[index])
                                    Text(
                                        text = if (currencyPreference == "$") "$$formattedTotal" else "$formattedTotal $currencyPreference",
                                        fontWeight = if (total > 0f) FontWeight.Bold else FontWeight.Normal,
                                        color = if (total > 0f) MaterialTheme.colorScheme.primary else Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Visuals for individual progress bar rows
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = categoryName,
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

// Master summary progress bar card
@Composable
fun MasterProgressCard(expenses: List<Expense>, totalBudget: Float, currencySymbol: String) {
    val totalSpent = expenses.sumOf { it.amount }.toFloat()
    val progress = if (totalBudget > 0f) (totalSpent / totalBudget).coerceIn(0f, 1f) else 0f
    val isOverBudget = totalSpent > totalBudget
    val remaining = (totalBudget - totalSpent).coerceAtLeast(0f)

    val formattedSpent = String.format(Locale.US, "%.2f", totalSpent)
    val formattedRemaining = String.format(Locale.US, "%.2f", remaining)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Total Spent",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = if (currencySymbol == "$") "$$formattedSpent" else "$formattedSpent $currencySymbol",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Remaining",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = if (currencySymbol == "$") "$$formattedRemaining" else "$formattedRemaining $currencySymbol",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White.copy(alpha = 0.4f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .background(if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

// Updated donut chart that supports remaining logic
@Composable
fun DonutChart(expenses: List<Expense>, showRemaining: Boolean, totalBudget: Float, currencySymbol: String) {
    val totalSpent = expenses.sumOf { it.amount }.toFloat()

    val categoryColors = listOf(
        Color(0xFF6200EA), Color(0xFF03DAC5), Color(0xFFFF0266),
        Color(0xFFFFDE03), Color(0xFF00C853), Color(0xFFFF8F00)
    )

    Box(
        modifier = Modifier
            .size(220.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {

            drawArc(
                color = Color.LightGray.copy(alpha = 0.3f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 50f, cap = StrokeCap.Round)
            )

            if (expenses.isNotEmpty() && totalSpent > 0f) {
                var currentStartAngle = -90f
                val groupedExpenses = expenses.groupBy { it.category }

                // Dynamic scale based on mode
                val scaleBase = if (showRemaining && totalBudget > 0f) totalBudget else totalSpent

                if (showRemaining && totalSpent > totalBudget) {
                    // Turn entire circle red if over budget
                    drawArc(
                        color = Color.Red,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 50f, cap = StrokeCap.Round)
                    )
                } else {
                    groupedExpenses.entries.forEachIndexed { index, entry ->
                        val categoryTotal = entry.value.sumOf { it.amount }.toFloat()
                        // Calculate sweep angle relative to base
                        val sweepAngle = (categoryTotal / scaleBase) * 360f
                        val color = categoryColors[index % categoryColors.size]

                        drawArc(
                            color = color,
                            startAngle = currentStartAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = 50f, cap = StrokeCap.Round)
                        )
                        currentStartAngle += sweepAngle
                    }
                }
            }
        }

        // Central text
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.animateContentSize()) {
            if (showRemaining) {
                val remaining = (totalBudget - totalSpent).coerceAtLeast(0f)
                val isOver = totalSpent > totalBudget

                val centerValue = if (isOver) totalSpent - totalBudget else remaining
                val formattedValue = String.format(Locale.US, "%.2f", centerValue)

                Text(
                    text = if (isOver) "Over Budget" else "Remaining",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isOver) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (currencySymbol == "$") "$$formattedValue" else "$formattedValue $currencySymbol",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isOver) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
            } else {
                val formattedSpent = String.format(Locale.US, "%.2f", totalSpent)
                Text(
                    text = "Spent",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (currencySymbol == "$") "$$formattedSpent" else "$formattedSpent $currencySymbol",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Included history bar chart unchanged from previous setup
@Composable
fun YearlyBarChart(monthlyTotals: FloatArray) {
    val maxAmount = monthlyTotals.maxOrNull() ?: 1f
    val safeMax = if (maxAmount == 0f) 1f else maxAmount

    val monthNames = listOf("Ιαν", "Φεβ", "Μαρ", "Απρ", "Μαι", "Ιουν", "Ιουλ", "Αυγ", "Σεπ", "Οκτ", "Νοε", "Δεκ")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        monthlyTotals.forEachIndexed { index, amount ->
            val heightFraction = amount / safeMax

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .fillMaxHeight(heightFraction)
                            .background(
                                color = if (amount > 0f) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                            )
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = monthNames[index],
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Visible
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExpenseItem(
    expense: Expense,
    currencySymbol: String,
    onDeleteClick: (Expense) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    val formattedAmount = String.format(Locale.US, "%.2f", expense.amount)
    val expenseText = if (currencySymbol == "$") "- $$formattedAmount" else "- $formattedAmount $currencySymbol"

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
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = expense.storeName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = expense.category,
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
            title = { Text("Διαγραφή εξόδου") },
            text = { Text("Είστε σίγουροι ότι θέλετε να διαγράψετε το έξοδο ${expense.storeName};") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick(expense)
                        showDeleteDialog = false
                    }
                ) {
                    Text("Διαγραφή", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Ακύρωση")
                }
            }
        )
    }
}