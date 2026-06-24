package com.dionisispx.expensetracker.presentation.expense

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
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

// ViewModel managing expense data and state
@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    // Holds the list of expenses for the current month
    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    // Represents the currently selected month
    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    // Represents the currently selected year
    private val _currentYear = MutableStateFlow(YearMonth.now().year)
    val currentYear: StateFlow<Int> = _currentYear.asStateFlow()

    // Holds the list of expenses for the current year
    private val _yearlyExpenses = MutableStateFlow<List<Expense>>(emptyList())
    val yearlyExpenses: StateFlow<List<Expense>> = _yearlyExpenses.asStateFlow()

    // Calculates total expenses for each month of the year
    val monthlyTotals: StateFlow<FloatArray> = _yearlyExpenses.map { expenses ->
        val totals = FloatArray(12)
        expenses.forEach { expense ->
            val date = expense.date.atZone(ZoneId.systemDefault()).toLocalDate()
            val monthIndex = date.monthValue - 1
            totals[monthIndex] += expense.amount.toFloat()
        }
        totals
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FloatArray(12))

    // Jobs to manage ongoing coroutines for fetching data
    private var currentMonthJob: Job? = null
    private var currentYearJob: Job? = null

    init {
        // Collects selected month updates and loads corresponding expenses
        viewModelScope.launch {
            _currentMonth.collect { month ->
                loadExpensesForMonth(month)
            }
        }
        // Collects selected year updates and loads corresponding expenses
        viewModelScope.launch {
            _currentYear.collect { year ->
                loadExpensesForYear(year)
            }
        }
    }

    // Loads expenses for the specified month from the repository
    private fun loadExpensesForMonth(yearMonth: YearMonth) {
        currentMonthJob?.cancel()
        currentMonthJob = viewModelScope.launch {
            val startOfMonth = DateUtils.getStartOfMonthInstant(yearMonth)
            val endOfMonth = DateUtils.getEndOfMonthInstant(yearMonth)

            repository.getExpensesByDateRange(startOfMonth, endOfMonth).collect { expenseList ->
                _expenses.value = expenseList
            }
        }
    }

    // Loads expenses for the specified year from the repository
    private fun loadExpensesForYear(year: Int) {
        currentYearJob?.cancel()
        currentYearJob = viewModelScope.launch {
            val startOfYear = DateUtils.getStartOfYearInstant(year)
            val endOfYear = DateUtils.getEndOfYearInstant(year)

            repository.getExpensesByDateRange(startOfYear, endOfYear).collect { expenseList ->
                _yearlyExpenses.value = expenseList
            }
        }
    }

    // Advances to the next month
    fun nextMonth() {
        _currentMonth.value = _currentMonth.value.plusMonths(1)
    }

    // Goes back to the previous month
    fun previousMonth() {
        _currentMonth.value = _currentMonth.value.minusMonths(1)
    }

    // Advances to the next year
    fun nextYear() {
        _currentYear.value += 1
    }

    // Goes back to the previous year
    fun previousYear() {
        _currentYear.value -= 1
    }

    // Event flow to communicate errors to the UI
    private val _errorEvent = kotlinx.coroutines.flow.MutableSharedFlow<String>()
    val errorEvent = _errorEvent.asSharedFlow()

    // Adds a new expense to the repository
    fun addExpense(expense: Expense) {
        viewModelScope.launch { 
            val result = repository.insertExpense(expense)
            if (result.isFailure) {
                _errorEvent.emit("Failed to add expense: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    // Deletes an existing expense from the repository
    fun deleteExpense(expense: Expense) {
        viewModelScope.launch { 
            val result = repository.deleteExpense(expense)
            if (result.isFailure) {
                _errorEvent.emit("Failed to delete expense: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    // Deletes all expense data from the repository
    fun deleteAllData() {
        viewModelScope.launch {
            val result = repository.deleteAllExpenses()
            if (result.isFailure) {
                _errorEvent.emit("Failed to delete all data: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    // Retrieves a one-time snapshot of all expenses
    suspend fun getAllExpensesSnapshot(): List<Expense> {
        return repository.getAllExpenses().firstOrNull() ?: emptyList()
    }
}
