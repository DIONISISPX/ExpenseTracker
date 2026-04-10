package com.dionisispx.expensetracker.presentation.home

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dionisispx.expensetracker.R
import com.dionisispx.expensetracker.domain.model.Expense
import com.dionisispx.expensetracker.presentation.ExpenseViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    // Observe database and view model states
    val expenses by viewModel.expenses.collectAsState()
    val yearlyExpenses by viewModel.yearlyExpenses.collectAsState()
    val currentMonth by viewModel.currentMonth.collectAsState()
    val currentYear by viewModel.currentYear.collectAsState()
    val languagePreference by viewModel.languagePreference.collectAsState()
    val totalBudget by viewModel.totalBudget.collectAsState()
    val categoryLimits by viewModel.categoryLimits.collectAsState()
    val currencyPreference by viewModel.currencyPreference.collectAsState()

    // Setup interactive states
    var showRemaining by remember { mutableStateOf(false) }
    var selectedMainTab by remember { mutableIntStateOf(0) }
    var selectedSubTab by remember { mutableIntStateOf(0) }

    val pagerState = rememberPagerState(pageCount = { 2 })
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val mainTabs = listOf(
        stringResource(R.string.tab_now),
        stringResource(R.string.tab_history)
    )
    val subTabs = listOf(
        stringResource(R.string.tab_transactions),
        stringResource(R.string.tab_limits)
    )

    // Sync sub tab clicks with pager swipes
    LaunchedEffect(selectedSubTab) {
        pagerState.animateScrollToPage(selectedSubTab)
    }
    LaunchedEffect(pagerState.currentPage) {
        selectedSubTab = pagerState.currentPage
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.app_name), fontWeight = FontWeight.Bold) },
                actions = {
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
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

            // Main tabs remain full width regardless of orientation
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
                if (isLandscape) {
                    // Split layout for Now tab in landscape
                    Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState())) {
                            MonthSelectorAndChart(currentMonth, expenses, showRemaining, totalBudget, currencyPreference, languagePreference, viewModel)
                        }
                        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            TabRow(selectedTabIndex = selectedSubTab) {
                                subTabs.forEachIndexed { index, title ->
                                    Tab(
                                        selected = selectedSubTab == index,
                                        onClick = { selectedSubTab = index },
                                        text = { Text(title) }
                                    )
                                }
                            }
                            SubTabPager(
                                pagerState = pagerState, expenses = expenses, categoryLimits = categoryLimits,
                                totalBudget = totalBudget, currencyPreference = currencyPreference, viewModel = viewModel
                            )
                        }
                    }
                } else {
                    // Standard portrait layout utilizing remaining height properly
                    Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        MonthSelectorAndChart(currentMonth, expenses, showRemaining, totalBudget, currencyPreference, languagePreference, viewModel)

                        TabRow(selectedTabIndex = selectedSubTab) {
                            subTabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedSubTab == index,
                                    onClick = { selectedSubTab = index },
                                    text = { Text(title) }
                                )
                            }
                        }

                        SubTabPager(
                            pagerState = pagerState, expenses = expenses, categoryLimits = categoryLimits,
                            totalBudget = totalBudget, currencyPreference = currencyPreference, viewModel = viewModel
                        )
                    }
                }
            } else {
                // History tab securely fills remaining weight
                HistoryBreakdown(
                    yearlyExpenses = yearlyExpenses,
                    currentYear = currentYear,
                    currencyPreference = currencyPreference,
                    isLandscape = isLandscape,
                    viewModel = viewModel,
                    modifier = Modifier.weight(1f).fillMaxWidth()
                )
            }
        }
    }
}

// Reusable component for the top section with sliding animation and gesture detection
@Composable
fun MonthSelectorAndChart(
    currentMonth: java.time.YearMonth,
    expenses: List<Expense>,
    showRemaining: Boolean,
    totalBudget: Float,
    currencyPreference: String,
    languagePreference: String,
    viewModel: ExpenseViewModel
) {
    var swipeOffset by remember { mutableFloatStateOf(0f) }
    val formatter = remember(languagePreference) { DateTimeFormatter.ofPattern("LLLL yyyy", Locale(languagePreference)) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (swipeOffset > 40) viewModel.previousMonth()
                        else if (swipeOffset < -40) viewModel.nextMonth()
                        swipeOffset = 0f
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        swipeOffset += dragAmount
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Static arrows layer
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.previousMonth() }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Prev")
            }
            IconButton(onClick = { viewModel.nextMonth() }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next")
            }
        }

        // Animated Content Layer
        AnimatedContent(
            targetState = Pair(currentMonth, expenses),
            transitionSpec = {
                if (targetState.first > initialState.first) {
                    (slideInHorizontally(animationSpec = tween(300)) { width -> width } + fadeIn()) togetherWith
                            (slideOutHorizontally(animationSpec = tween(300)) { width -> -width } + fadeOut())
                } else {
                    (slideInHorizontally(animationSpec = tween(300)) { width -> -width } + fadeIn()) togetherWith
                            (slideOutHorizontally(animationSpec = tween(300)) { width -> width } + fadeOut())
                }
            },
            label = "MonthChartAnimation"
        ) { (animMonth, animExpenses) ->
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val monthString = animMonth.format(formatter).replaceFirstChar { it.uppercase() }

                Text(
                    text = monthString,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                DonutChart(
                    expenses = animExpenses,
                    showRemaining = showRemaining,
                    totalBudget = totalBudget,
                    currencySymbol = currencyPreference
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// Reusable component for the interactive swipeable pager
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SubTabPager(
    pagerState: androidx.compose.foundation.pager.PagerState,
    expenses: List<Expense>,
    categoryLimits: Map<String, Float>,
    totalBudget: Float,
    currencyPreference: String,
    viewModel: ExpenseViewModel
) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        if (page == 0) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
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
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
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
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

// Reusable components for history layout to keep it clean
@Composable
fun HistoryBreakdown(
    yearlyExpenses: List<Expense>,
    currentYear: Int,
    currencyPreference: String,
    isLandscape: Boolean,
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier
) {
    val monthlyTotals = FloatArray(12) { 0f }
    yearlyExpenses.forEach { expense ->
        val date = Instant.ofEpochMilli(expense.date).atZone(ZoneId.systemDefault()).toLocalDate()
        val monthIndex = date.monthValue - 1
        monthlyTotals[monthIndex] += expense.amount.toFloat()
    }

    if (isLandscape) {
        Column(modifier = modifier) {
            HistoryHeader(currentYear, viewModel)
            Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    YearlyBarChart(monthlyTotals = monthlyTotals)
                    YearlyTotalCard(yearlyExpenses, currencyPreference)
                }
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState())
                ) {
                    MonthlyBreakdownList(monthlyTotals, currencyPreference)
                }
            }
        }
    } else {
        Column(modifier = modifier.verticalScroll(rememberScrollState())) {
            HistoryHeader(currentYear, viewModel)
            YearlyBarChart(monthlyTotals = monthlyTotals)
            YearlyTotalCard(yearlyExpenses, currencyPreference)
            MonthlyBreakdownList(monthlyTotals, currencyPreference)
        }
    }
}

@Composable
fun HistoryHeader(currentYear: Int, viewModel: ExpenseViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
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
}

@Composable
fun YearlyTotalCard(yearlyExpenses: List<Expense>, currencyPreference: String) {
    val yearlyTotal = yearlyExpenses.sumOf { it.amount }
    val formattedYearlyTotal = String.format(Locale.US, "%.2f", yearlyTotal)

    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(stringResource(R.string.yearly_total), fontWeight = FontWeight.Bold)
            Text(
                text = if (currencyPreference == "$") "$$formattedYearlyTotal" else "$formattedYearlyTotal $currencyPreference",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun MonthlyBreakdownList(monthlyTotals: FloatArray, currencyPreference: String) {
    Text(
        text = stringResource(R.string.monthly_breakdown),
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)
    )

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).padding(bottom = 32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val fullMonths = stringArrayResource(R.array.months_full)

            monthlyTotals.forEachIndexed { index, total ->
                val formattedTotal = String.format(Locale.US, "%.2f", total)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
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

// Helper to provide universal colors and icons for categories
fun getCategoryDetails(category: String): Pair<ImageVector, Color> {
    return when (category) {
        "Groceries" -> Icons.Default.ShoppingCart to Color(0xFF81C784)
        "Food & Drink" -> Icons.Default.Restaurant to Color(0xFFFF8A65)
        "Transport & Fuel" -> Icons.Default.DirectionsCar to Color(0xFF64B5F6)
        "Shopping" -> Icons.Default.LocalMall to Color(0xFFBA68C8)
        "Entertainment" -> Icons.Default.Movie to Color(0xFFFFD54F)
        "Bills & Utilities" -> Icons.Default.Receipt to Color(0xFF4DB6AC)
        "Health & Fitness" -> Icons.Default.Favorite to Color(0xFFE57373)
        "Travel" -> Icons.Default.Flight to Color(0xFF7986CB)
        "Home" -> Icons.Default.Home to Color(0xFFA1887F)
        "Education" -> Icons.Default.School to Color(0xFFFFB74D)
        "Personal Care" -> Icons.Default.Spa to Color(0xFFF06292)
        else -> Icons.Default.MoreHoriz to Color(0xFF90A4AE)
    }
}

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
                        text = stringResource(R.string.total_spent),
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
                        text = stringResource(R.string.remaining),
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

@Composable
fun DonutChart(expenses: List<Expense>, showRemaining: Boolean, totalBudget: Float, currencySymbol: String) {
    val totalSpent = expenses.sumOf { it.amount }.toFloat()

    Box(
        modifier = Modifier
            .size(220.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Enforcing explicit canvas size and aspect ratio so it cannot stretch into an oval
        Canvas(modifier = Modifier.size(180.dp).aspectRatio(1f)) {
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
                val scaleBase = if (showRemaining && totalBudget > 0f) totalBudget else totalSpent

                if (showRemaining && totalSpent > totalBudget) {
                    drawArc(
                        color = Color.Red,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 50f, cap = StrokeCap.Round)
                    )
                } else {
                    groupedExpenses.entries.forEach { entry ->
                        val categoryTotal = entry.value.sumOf { it.amount }.toFloat()
                        val sweepAngle = (categoryTotal / scaleBase) * 360f
                        val color = getCategoryDetails(entry.key).second

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

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.animateContentSize()) {
            if (showRemaining) {
                val remaining = (totalBudget - totalSpent).coerceAtLeast(0f)
                val isOver = totalSpent > totalBudget
                val centerValue = if (isOver) totalSpent - totalBudget else remaining
                val formattedValue = String.format(Locale.US, "%.2f", centerValue)

                Text(
                    text = if (isOver) stringResource(R.string.over_budget) else stringResource(R.string.remaining),
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
                    text = stringResource(R.string.spent),
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

@Composable
fun YearlyBarChart(monthlyTotals: FloatArray) {
    val maxAmount = monthlyTotals.maxOrNull() ?: 1f
    val safeMax = if (maxAmount == 0f) 1f else maxAmount
    val monthNames = stringArrayResource(R.array.months_short)

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
                modifier = Modifier.weight(1f).fillMaxHeight()
            ) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
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

                val localizedCategory = when (expense.category) {
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
                    else -> expense.category
                }

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