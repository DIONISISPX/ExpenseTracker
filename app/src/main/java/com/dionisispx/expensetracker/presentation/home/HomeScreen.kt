package com.dionisispx.expensetracker.presentation.home

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

    // We use a scaffold here to easily place the top app bar
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
                            contentDescription = "Toggle budget view"
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

            // Top main tab row
            TabRow(selectedTabIndex = selectedMainTab) {
                mainTabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedMainTab == index,
                        onClick = { selectedMainTab = index },
                        text = { Text(title, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            // Show content based on selected main tab
            if (selectedMainTab == 0) {

                // Month selector row
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

                DonutChart(expenses = expenses)

                Spacer(modifier = Modifier.height(16.dp))

                // Sub tab row for transactions and limits
                TabRow(selectedTabIndex = selectedSubTab) {
                    subTabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedSubTab == index,
                            onClick = { selectedSubTab = index },
                            text = { Text(title) }
                        )
                    }
                }

                // Show content based on selected sub tab
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
                                onDeleteClick = { viewModel.deleteExpense(it) }
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Limits progress bars will go here")
                    }
                }

            } else {

                // Calculate monthly totals here so both the chart and the list can use it
                val monthlyTotals = FloatArray(12) { 0f }
                yearlyExpenses.forEach { expense ->
                    val date = Instant.ofEpochMilli(expense.date).atZone(ZoneId.systemDefault()).toLocalDate()
                    val monthIndex = date.monthValue - 1
                    monthlyTotals[monthIndex] += expense.amount.toFloat()
                }

                // History screen selected scrollable view
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {

                    // Year selector row
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

                    // Display the yearly bar chart
                    YearlyBarChart(monthlyTotals = monthlyTotals)

                    // Display a total summary for the year
                    val yearlyTotal = yearlyExpenses.sumOf { it.amount }
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
                                text = "€${String.format(Locale.US, "%.2f", yearlyTotal)}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Display monthly breakdown list
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
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(fullMonths[index])
                                    Text(
                                        text = "€${String.format(Locale.US, "%.2f", total)}",
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

@Composable
fun YearlyBarChart(monthlyTotals: FloatArray) {
    // Find the highest spending month to scale the bars properly
    val maxAmount = monthlyTotals.maxOrNull() ?: 1f
    val safeMax = if (maxAmount == 0f) 1f else maxAmount

    // Three letter greek initials for months
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
            // Calculate how tall the bar should be relative to the highest month
            val heightFraction = amount / safeMax

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                // Chart drawing area ensures bars always grow from the bottom up and never overlap text
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
                // The month label
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

@Composable
fun DonutChart(expenses: List<Expense>) {
    // Calculate total amount spent
    val amount = expenses.sumOf { it.amount }

    // Fallback colors for categories
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
            // Always draw a constant background arc for the donut hole
            drawArc(
                color = Color.LightGray.copy(alpha = 0.3f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 50f, cap = StrokeCap.Round)
            )

            // If there are expenses draw category arcs on top
            if (expenses.isNotEmpty()) {
                var currentStartAngle = -90f
                val groupedExpenses = expenses.groupBy { it.category }

                groupedExpenses.entries.forEachIndexed { index, entry ->
                    val categoryTotal = entry.value.sumOf { it.amount }
                    val sweepAngle = (categoryTotal.toFloat() / amount.toFloat()) * 360f
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

        // Text inside the donut chart
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Spent",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "€${String.format(Locale.US, "%.2f", amount)}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExpenseItem(
    expense: Expense,
    onDeleteClick: (Expense) -> Unit
) {
    // State to control the delete confirmation dialog
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { /* Handle normal click to edit later */ },
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
                text = "- €${String.format(Locale.US, "%.2f", expense.amount)}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
        }
    }

    // Show popup dialog when user long presses
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