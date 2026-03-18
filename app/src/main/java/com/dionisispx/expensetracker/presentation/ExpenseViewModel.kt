package com.dionisispx.expensetracker.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dionisispx.expensetracker.domain.model.Expense
import com.dionisispx.expensetracker.domain.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    // State to hold the currently selected month and year
    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    // Keeps track of the active database query
    private var currentJob: Job? = null

    init {
        // Whenever the month changes, automatically fetch the new data
        viewModelScope.launch {
            _currentMonth.collect { month ->
                loadExpensesForMonth(month)
            }
        }
    }

    // Fetches expenses for a whole month
    private fun loadExpensesForMonth(yearMonth: YearMonth) {
        currentJob?.cancel() // Cancel previous fetch if user clicks arrows too fast
        currentJob = viewModelScope.launch {
            // Convert the 1st of the month to milliseconds
            val startOfMonth = yearMonth.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            // Convert the last day of the month to milliseconds
            val endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            repository.getExpensesByDateRange(startOfMonth, endOfMonth).collect { expenseList ->
                _expenses.value = expenseList
            }
        }
    }

    // Move to the next month
    fun nextMonth() {
        _currentMonth.value = _currentMonth.value.plusMonths(1)
    }

    // Move to the previous month
    fun previousMonth() {
        _currentMonth.value = _currentMonth.value.minusMonths(1)
    }

    fun addExpense(expense: Expense) {
        viewModelScope.launch { repository.insertExpense(expense) }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch { repository.deleteExpense(expense) }
    }
}