package com.dionisispx.expensetracker.presentation.preferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dionisispx.expensetracker.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PreferencesViewModel @Inject constructor(
    private val prefsRepository: UserPreferencesRepository
) : ViewModel() {

    val isFirstRun: StateFlow<Boolean?> = prefsRepository.isFirstRun.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )

    val themePreference: StateFlow<String> = prefsRepository.themePreference.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), "system"
    )

    val currencyPreference: StateFlow<String> = prefsRepository.currencyPreference.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), "€"
    )

    val languagePreference: StateFlow<String> = prefsRepository.languagePreference.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), "el"
    )

    fun setFirstRunCompleted() {
        viewModelScope.launch {
            prefsRepository.setFirstRunCompleted()
        }
    }

    fun saveThemePreference(theme: String) {
        viewModelScope.launch {
            prefsRepository.saveThemePreference(theme)
        }
    }

    fun saveCurrencyPreference(currency: String) {
        viewModelScope.launch {
            prefsRepository.saveCurrencyPreference(currency)
        }
    }

    fun saveLanguagePreference(language: String) {
        viewModelScope.launch {
            prefsRepository.saveLanguagePreference(language)
        }
    }
}
