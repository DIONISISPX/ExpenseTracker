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

    // State to hold the currently selected month and year
    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    // State to hold the currently selected year for the history tab
    private val _currentYear = MutableStateFlow(YearMonth.now().year)
    val currentYear: StateFlow<Int> = _currentYear.asStateFlow()

    // State to hold all expenses for the selected year
    private val _yearlyExpenses = MutableStateFlow<List<Expense>>(emptyList())
    val yearlyExpenses: StateFlow<List<Expense>> = _yearlyExpenses.asStateFlow()

    val monthlyTotals: StateFlow<FloatArray> = _yearlyExpenses.map { expenses ->
        val totals = FloatArray(12)
        expenses.forEach { expense ->
            val date = java.time.Instant.ofEpochMilli(expense.date).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
            val monthIndex = date.monthValue - 1
            totals[monthIndex] += expense.amount.toFloat()
        }
        totals
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FloatArray(12) { 0f })

    // Read real budget limits from data store
    val totalBudget: StateFlow<Float> = prefsRepository.totalBudget.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 1000f
    )

    // Read category limits from data store
    val categoryLimits: StateFlow<Map<String, Float>> = prefsRepository.categoryLimits.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap()
    )

    // Read theme preference from data store
    val themePreference: StateFlow<String> = prefsRepository.themePreference.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), "system"
    )

    // Read currency preference from data store
    val currencyPreference: StateFlow<String> = prefsRepository.currencyPreference.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), "€"
    )

    val languagePreference: StateFlow<String> = prefsRepository.languagePreference.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), "el"
    )

    // Auto learning dictionary builds a map from all database expenses
    val userDictionary: StateFlow<Map<String, String>> = repository.getAllExpenses()
        .map { allExpenses ->
            val dict = mutableMapOf<String, String>()
            // Sort by date (oldest first) to ensure newest category overwrites the old one
            allExpenses.sortedBy { it.date }.forEach { expense ->
                // Uppercase to match the OCR output format perfectly
                dict[expense.storeName.uppercase()] = expense.category
            }
            dict
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Keeps track of the active database queries
    private var currentMonthJob: Job? = null
    private var currentYearJob: Job? = null

    init {
        // Whenever the month changes automatically fetch the new data
        viewModelScope.launch {
            _currentMonth.collect { month ->
                loadExpensesForMonth(month)
            }
        }

        // Listen for year changes and fetch yearly data
        viewModelScope.launch {
            _currentYear.collect { year ->
                loadExpensesForYear(year)
            }
        }
    }

    // Fetches expenses for a whole month
    private fun loadExpensesForMonth(yearMonth: YearMonth) {
        // Cancel previous fetch if user clicks arrows too fast
        currentMonthJob?.cancel()
        currentMonthJob = viewModelScope.launch {
            // Convert the first day of the month to milliseconds
            val startOfMonth = yearMonth.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            // Convert the last day of the month to milliseconds
            val endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            repository.getExpensesByDateRange(startOfMonth, endOfMonth).collect { expenseList ->
                _expenses.value = expenseList
            }
        }
    }

    // Fetches expenses for an entire year
    private fun loadExpensesForYear(year: Int) {
        // Cancel previous fetch if user clicks arrows too fast
        currentYearJob?.cancel()
        currentYearJob = viewModelScope.launch {
            // Convert the first day of the year to milliseconds
            val startOfYear = YearMonth.of(year, 1).atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            // Convert the last day of the year to milliseconds
            val endOfYear = YearMonth.of(year, 12).atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            repository.getExpensesByDateRange(startOfYear, endOfYear).collect { expenseList ->
                _yearlyExpenses.value = expenseList
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

    // Move to the next year
    fun nextYear() {
        _currentYear.value += 1
    }

    // Move to the previous year
    fun previousYear() {
        _currentYear.value -= 1
    }

    // Add new expense to database
    fun addExpense(expense: Expense) {
        viewModelScope.launch { repository.insertExpense(expense) }
    }

    // Delete expense from database
    fun deleteExpense(expense: Expense) {
        viewModelScope.launch { repository.deleteExpense(expense) }
    }

    // Save new limits to data store
    fun saveBudgetAndLimits(budget: Float, limits: Map<String, Float>) {
        viewModelScope.launch {
            prefsRepository.saveTotalBudget(budget)
            prefsRepository.saveCategoryLimits(limits)
        }
    }

    // Save theme preference to data store
    fun saveThemePreference(theme: String) {
        viewModelScope.launch {
            prefsRepository.saveThemePreference(theme)
        }
    }

    // Save currency preference to data store
    fun saveCurrencyPreference(currency: String) {
        viewModelScope.launch {
            prefsRepository.saveCurrencyPreference(currency)
        }
    }

    // Delete all data from database and reset preferences
    fun deleteAllData() {
        viewModelScope.launch {
            // Wipes the database
            repository.deleteAllExpenses()
            // Resets the budget and limits back to default
            prefsRepository.saveTotalBudget(1000f)
            prefsRepository.saveCategoryLimits(emptyMap())
        }
    }

    // Fetch a single snapshot of all expenses for exporting
    suspend fun getAllExpensesSnapshot(): List<Expense> {
        return repository.getAllExpenses().firstOrNull() ?: emptyList()
    }

    fun saveLanguagePreference(language: String) {
        viewModelScope.launch {
            prefsRepository.saveLanguagePreference(language)
        }
    }
}