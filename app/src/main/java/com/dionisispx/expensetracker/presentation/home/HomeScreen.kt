package com.dionisispx.expensetracker.presentation.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.SwapHoriz
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dionisispx.expensetracker.domain.model.Expense
import com.dionisispx.expensetracker.presentation.ExpenseViewModel
import kotlin.text.format
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    // Observe the list of expenses from the viewmodel
    val expenses by viewModel.expenses.collectAsState()

    // Observe the current month from ViewModel
    val currentMonth by viewModel.currentMonth.collectAsState()

    // Format the month in Greek
    val formatter = remember {
        java.time.format.DateTimeFormatter.ofPattern("LLLL yyyy", java.util.Locale.forLanguageTag("el-GR"))
    }
    val monthString = currentMonth.format(formatter).replaceFirstChar { it.uppercase() }

    // State for the top icon toggle (Spent vs Remaining)
    var showRemaining by remember { mutableStateOf(false) }

    // State for the main tabs (0 = Τώρα, 1 = Ιστορικό)
    var selectedMainTab by remember { mutableIntStateOf(0) }
    val mainTabs = listOf("Τώρα", "Ιστορικό")

    // State for the sub tabs (0 = Συναλλαγές, 1 = Όρια)
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
                // "Τώρα" screen selected

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
                    // "Συναλλαγές" list selected
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(expenses) { expense ->
                            ExpenseItem(expense = expense)
                        }
                    }
                } else {
                    // "Όρια" list selected
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Limits progress bars will go here")
                    }
                }

            } else {
                // "Ιστορικό" screen selected
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Yearly Bar Chart will go here")
                }
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

            // If there are expenses, draw category arcs on top
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
                text = "€${String.format(java.util.Locale.US, "%.2f", amount)}", // Format to US double
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ExpenseItem(expense: Expense) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                text = "- €${String.format(java.util.Locale.US, "%.2f", expense.amount)}", // Format to US double
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
        }
    }
}