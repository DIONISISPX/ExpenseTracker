package com.dionisispx.expensetracker.presentation.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dionisispx.expensetracker.domain.model.ExpenseCategory
import com.dionisispx.expensetracker.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// Manages budget-related UI state and user preferences
@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val prefsRepository: UserPreferencesRepository
) : ViewModel() {

    // Indicates whether to show the remaining budget or the spent amount
    private val _showRemaining = MutableStateFlow(false)
    val showRemaining: StateFlow<Boolean> = _showRemaining.asStateFlow()

    // Holds the total budget amount
    val totalBudget: StateFlow<Int> = prefsRepository.totalBudget.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 1000
    )

    // Holds the budget limits per category
    val categoryLimits: StateFlow<Map<ExpenseCategory, Float>> = prefsRepository.categoryLimits.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap()
    )

    // Saves the total budget and category limits to user preferences
    fun saveBudgetAndLimits(budget: Int, limits: Map<ExpenseCategory, Float>) {
        viewModelScope.launch {
            prefsRepository.saveTotalBudget(budget)
            prefsRepository.saveCategoryLimits(limits)
        }
    }

    // Toggles the visibility state between showing remaining and spent budget
    fun toggleShowRemaining() {
        _showRemaining.value = !_showRemaining.value
    }

    // Resets the total budget and category limits to their default values
    fun resetBudget() {
        viewModelScope.launch {
            prefsRepository.saveTotalBudget(1000)
            prefsRepository.saveCategoryLimits(emptyMap())
        }
    }
}
