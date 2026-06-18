package com.dionisispx.expensetracker.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val isFirstRun: Flow<Boolean>
    val totalBudget: Flow<Int>
    val categoryLimits: Flow<Map<String, Float>>
    val themePreference: Flow<String>
    val currencyPreference: Flow<String>
    val languagePreference: Flow<String>

    suspend fun saveTotalBudget(budget: Int)
    suspend fun setFirstRunCompleted()
    suspend fun saveCategoryLimits(limits: Map<String, Float>)
    suspend fun saveThemePreference(theme: String)
    suspend fun saveCurrencyPreference(currency: String)
    suspend fun saveLanguagePreference(language: String)
}
