package com.dionisispx.expensetracker.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dionisispx.expensetracker.domain.repository.UserPreferencesRepository
import com.dionisispx.expensetracker.domain.model.Expense
import com.dionisispx.expensetracker.domain.repository.ExpenseRepository
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
class SharedViewModel @Inject constructor(
    private val repository: ExpenseRepository,
    private val prefsRepository: UserPreferencesRepository
) : ViewModel() {

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    // Tracks the currently selected month and year
    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    // Tracks the selected year for history view
    private val _currentYear = MutableStateFlow(YearMonth.now().year)
    val currentYear: StateFlow<Int> = _currentYear.asStateFlow()

    // Tracks all expenses within the selected year
    private val _yearlyExpenses = MutableStateFlow<List<Expense>>(emptyList())
    val yearlyExpenses: StateFlow<List<Expense>> = _yearlyExpenses.asStateFlow()

    // Toggles between spent and remaining budget views
    private val _showRemaining = MutableStateFlow(false)
    val showRemaining: StateFlow<Boolean> = _showRemaining.asStateFlow()

    // Calculates aggregated monthly expense totals
    val monthlyTotals: StateFlow<FloatArray> = _yearlyExpenses.map { expenses ->
        val totals = FloatArray(12)
        expenses.forEach { expense ->
            val date = java.time.Instant.ofEpochMilli(expense.date).atZone(ZoneId.systemDefault()).toLocalDate()
            val monthIndex = date.monthValue - 1
            totals[monthIndex] += expense.amount.toFloat()
        }
        totals
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FloatArray(12))

    // Observes total budget limit
    val totalBudget: StateFlow<Int> = prefsRepository.totalBudget.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 1000
    )

    // Observes initial launch state
    val isFirstRun: StateFlow<Boolean?> = prefsRepository.isFirstRun.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )

    // Observes category-specific budget limits
    val categoryLimits: StateFlow<Map<String, Float>> = prefsRepository.categoryLimits.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap()
    )

    // Observes UI theme preference
    val themePreference: StateFlow<String> = prefsRepository.themePreference.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), "system"
    )

    // Observes display currency preference
    val currencyPreference: StateFlow<String> = prefsRepository.currencyPreference.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), "€"
    )

    // Observes display language preference
    val languagePreference: StateFlow<String> = prefsRepository.languagePreference.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), "el"
    )

    // Stores active database query jobs to allow cancellation
    private var currentMonthJob: Job? = null
    private var currentYearJob: Job? = null

    init {
        // Subscribes to month changes to trigger data refresh
        viewModelScope.launch {
            _currentMonth.collect { month ->
                loadExpensesForMonth(month)
            }
        }

        // Subscribes to year changes to trigger history refresh
        viewModelScope.launch {
            _currentYear.collect { year ->
                loadExpensesForYear(year)
            }
        }
    }

    // Loads all expenses for a specific month
    private fun loadExpensesForMonth(yearMonth: YearMonth) {
        // Cancels ongoing queries to prevent race conditions
        currentMonthJob?.cancel()
        currentMonthJob = viewModelScope.launch {
            // Calculates start timestamp for the selected month
            val startOfMonth = yearMonth.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            // Calculates end timestamp for the selected month
            val endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            repository.getExpensesByDateRange(startOfMonth, endOfMonth).collect { expenseList ->
                _expenses.value = expenseList
            }
        }
    }

    // Loads all expenses for a specific year
    private fun loadExpensesForYear(year: Int) {
        // Cancels ongoing queries to prevent race conditions
        currentYearJob?.cancel()
        currentYearJob = viewModelScope.launch {
            // Calculates start timestamp for the selected year
            val startOfYear = YearMonth.of(year, 1).atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            // Calculates end timestamp for the selected year
            val endOfYear = YearMonth.of(year, 12).atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            repository.getExpensesByDateRange(startOfYear, endOfYear).collect { expenseList ->
                _yearlyExpenses.value = expenseList
            }
        }
    }

    // Advances current month by one
    fun nextMonth() {
        _currentMonth.value = _currentMonth.value.plusMonths(1)
    }

    // Rewinds current month by one
    fun previousMonth() {
        _currentMonth.value = _currentMonth.value.minusMonths(1)
    }

    // Advances current year by one
    fun nextYear() {
        _currentYear.value += 1
    }

    // Rewinds current year by one
    fun previousYear() {
        _currentYear.value -= 1
    }

    // Inserts a new expense record
    fun addExpense(expense: Expense) {
        viewModelScope.launch { repository.insertExpense(expense) }
    }

    // Removes an existing expense record
    fun deleteExpense(expense: Expense) {
        viewModelScope.launch { repository.deleteExpense(expense) }
    }

    // Updates global budget and category limits
    fun saveBudgetAndLimits(budget: Int, limits: Map<String, Float>) {
        viewModelScope.launch {
            prefsRepository.saveTotalBudget(budget)
            prefsRepository.saveCategoryLimits(limits)
        }
    }

    // Marks initial onboarding as completed
    fun setFirstRunCompleted() {
        viewModelScope.launch {
            prefsRepository.setFirstRunCompleted()
        }
    }

    // Updates UI theme preference
    fun saveThemePreference(theme: String) {
        viewModelScope.launch {
            prefsRepository.saveThemePreference(theme)
        }
    }

    // Updates display currency preference
    fun saveCurrencyPreference(currency: String) {
        viewModelScope.launch {
            prefsRepository.saveCurrencyPreference(currency)
        }
    }

    // Clears all expense data and resets limits
    fun deleteAllData() {
        viewModelScope.launch {
            // Deletes all database records
            repository.deleteAllExpenses()
            // Reverts preferences to factory defaults
            prefsRepository.saveTotalBudget(1000)
            prefsRepository.saveCategoryLimits(emptyMap())
        }
    }

    // Fetches a complete snapshot of all expenses
    suspend fun getAllExpensesSnapshot(): List<Expense> {
        return repository.getAllExpenses().firstOrNull() ?: emptyList()
    }

    // Updates display language preference
    fun saveLanguagePreference(language: String) {
        viewModelScope.launch {
            prefsRepository.saveLanguagePreference(language)
        }
    }

    // Toggles budget display mode
    fun toggleShowRemaining() {
        _showRemaining.value = !_showRemaining.value
    }
}