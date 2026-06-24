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

// ViewModel for managing user preferences
@HiltViewModel
class PreferencesViewModel @Inject constructor(
    private val prefsRepository: UserPreferencesRepository
) : ViewModel() {

    // Flow representing if the app is run for the first time
    val isFirstRun: StateFlow<Boolean?> = prefsRepository.isFirstRun.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )

    // Flow representing the selected UI theme
    val themePreference: StateFlow<String> = prefsRepository.themePreference.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), "system"
    )

    // Flow representing the selected currency symbol
    val currencyPreference: StateFlow<String> = prefsRepository.currencyPreference.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), "€"
    )

    // Flow representing the selected app language
    val languagePreference: StateFlow<String> = prefsRepository.languagePreference.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), "el"
    )

    // Marks the first run as completed in preferences
    fun setFirstRunCompleted() {
        viewModelScope.launch {
            prefsRepository.setFirstRunCompleted()
        }
    }

    // Saves the selected theme preference
    fun saveThemePreference(theme: String) {
        viewModelScope.launch {
            prefsRepository.saveThemePreference(theme)
        }
    }

    // Saves the selected currency preference
    fun saveCurrencyPreference(currency: String) {
        viewModelScope.launch {
            prefsRepository.saveCurrencyPreference(currency)
        }
    }

    // Saves the selected language preference
    fun saveLanguagePreference(language: String) {
        viewModelScope.launch {
            prefsRepository.saveLanguagePreference(language)
        }
    }
}
