package com.dionisispx.expensetracker.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dionisispx.expensetracker.domain.model.Expense
import com.dionisispx.expensetracker.domain.repository.ExpenseRepository
import com.dionisispx.expensetracker.presentation.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    private val _currentYear = MutableStateFlow(YearMonth.now().year)
    val currentYear: StateFlow<Int> = _currentYear.asStateFlow()

    private val _yearlyExpenses = MutableStateFlow<List<Expense>>(emptyList())
    val yearlyExpenses: StateFlow<List<Expense>> = _yearlyExpenses.asStateFlow()

    val monthlyTotals: StateFlow<FloatArray> = _yearlyExpenses.map { expenses ->
        val totals = FloatArray(12)
        expenses.forEach { expense ->
            val date = java.time.Instant.ofEpochMilli(expense.date).atZone(ZoneId.systemDefault()).toLocalDate()
            val monthIndex = date.monthValue - 1
            totals[monthIndex] += expense.amount.toFloat()
        }
        totals
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FloatArray(12))

    private var currentMonthJob: Job? = null
    private var currentYearJob: Job? = null

    init {
        viewModelScope.launch {
            _currentMonth.collect { month ->
                loadExpensesForMonth(month)
            }
        }
        viewModelScope.launch {
            _currentYear.collect { year ->
                loadExpensesForYear(year)
            }
        }
    }

    private fun loadExpensesForMonth(yearMonth: YearMonth) {
        currentMonthJob?.cancel()
        currentMonthJob = viewModelScope.launch {
            val startOfMonth = DateUtils.getStartOfMonthMillis(yearMonth)
            val endOfMonth = DateUtils.getEndOfMonthMillis(yearMonth)

            repository.getExpensesByDateRange(startOfMonth, endOfMonth).collect { expenseList ->
                _expenses.value = expenseList
            }
        }
    }

    private fun loadExpensesForYear(year: Int) {
        currentYearJob?.cancel()
        currentYearJob = viewModelScope.launch {
            val startOfYear = DateUtils.getStartOfYearMillis(year)
            val endOfYear = DateUtils.getEndOfYearMillis(year)

            repository.getExpensesByDateRange(startOfYear, endOfYear).collect { expenseList ->
                _yearlyExpenses.value = expenseList
            }
        }
    }

    fun nextMonth() {
        _currentMonth.value = _currentMonth.value.plusMonths(1)
    }

    fun previousMonth() {
        _currentMonth.value = _currentMonth.value.minusMonths(1)
    }

    fun nextYear() {
        _currentYear.value += 1
    }

    fun previousYear() {
        _currentYear.value -= 1
    }

    fun addExpense(expense: Expense) {
        viewModelScope.launch { repository.insertExpense(expense) }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch { repository.deleteExpense(expense) }
    }

    fun deleteAllData() {
        viewModelScope.launch {
            repository.deleteAllExpenses()
        }
    }

    suspend fun getAllExpensesSnapshot(): List<Expense> {
        return repository.getAllExpenses().firstOrNull() ?: emptyList()
    }
}
