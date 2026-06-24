package com.dionisispx.expensetracker.presentation.home.components.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dionisispx.expensetracker.domain.model.Expense
import com.dionisispx.expensetracker.presentation.expense.ExpenseViewModel
import com.dionisispx.expensetracker.presentation.home.components.charts.DonutChart
import java.time.format.DateTimeFormatter
import java.util.Locale

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
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .height(48.dp)
                .padding(horizontal = 16.dp)
                .align(Alignment.TopCenter),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val monthString = animMonth.format(formatter).replaceFirstChar { it.uppercase() }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = monthString,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

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
