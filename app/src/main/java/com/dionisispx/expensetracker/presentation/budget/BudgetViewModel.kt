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

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val prefsRepository: UserPreferencesRepository
) : ViewModel() {

    private val _showRemaining = MutableStateFlow(false)
    val showRemaining: StateFlow<Boolean> = _showRemaining.asStateFlow()

    val totalBudget: StateFlow<Int> = prefsRepository.totalBudget.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 1000
    )

    val categoryLimits: StateFlow<Map<ExpenseCategory, Float>> = prefsRepository.categoryLimits.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap()
    )

    fun saveBudgetAndLimits(budget: Int, limits: Map<ExpenseCategory, Float>) {
        viewModelScope.launch {
            prefsRepository.saveTotalBudget(budget)
            prefsRepository.saveCategoryLimits(limits)
        }
    }

    fun toggleShowRemaining() {
        _showRemaining.value = !_showRemaining.value
    }

    fun resetBudget() {
        viewModelScope.launch {
            prefsRepository.saveTotalBudget(1000)
            prefsRepository.saveCategoryLimits(emptyMap())
        }
    }
}
