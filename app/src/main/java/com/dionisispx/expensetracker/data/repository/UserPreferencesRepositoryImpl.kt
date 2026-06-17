package com.dionisispx.expensetracker.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dionisispx.expensetracker.domain.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : UserPreferencesRepository {

    // Keys for saving data safely
    private val totalBudgetKey = intPreferencesKey("total_budget")
    private val categoryLimitsKey = stringPreferencesKey("category_limits")
    private val themePreferenceKey = stringPreferencesKey("theme_preference")
    private val currencyPreferenceKey = stringPreferencesKey("currency_preference")
    private val languagePreferenceKey = stringPreferencesKey("language_preference")

    // Flow to read total budget defaulting to 1000
    override val totalBudget: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[totalBudgetKey] ?: 1000
    }

    // Flow to read category limits from JSON string
    override val categoryLimits: Flow<Map<String, Float>> = context.dataStore.data.map { preferences ->
        val jsonString = preferences[categoryLimitsKey] ?: "{}"
        try {
            val jsonObject = JSONObject(jsonString)
            val map = mutableMapOf<String, Float>()
            jsonObject.keys().forEach { key ->
                map[key] = jsonObject.getDouble(key).toFloat()
            }
            map
        } catch (e: Exception) {
            emptyMap()
        }
    }

    // Flow to read theme preference defaulting to system setting
    override val themePreference: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[themePreferenceKey] ?: "system"
    }

    // Flow to read currency preference defaulting to euro
    override val currencyPreference: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[currencyPreferenceKey] ?: "€"
    }

    // Flow to read language preference defaulting to greek
    override val languagePreference: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[languagePreferenceKey] ?: "el"
    }

    // Write budget to disk
    override suspend fun saveTotalBudget(budget: Int) {
        context.dataStore.edit { preferences ->
            preferences[totalBudgetKey] = budget
        }
    }

    // Write limits to disk
    override suspend fun saveCategoryLimits(limits: Map<String, Float>) {
        context.dataStore.edit { preferences ->
            val jsonObject = JSONObject()
            limits.forEach { (key, value) ->
                jsonObject.put(key, value.toDouble())
            }
            preferences[categoryLimitsKey] = jsonObject.toString()
        }
    }

    // Write theme preference to disk
    override suspend fun saveThemePreference(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[themePreferenceKey] = theme
        }
    }

    // Write currency preference to disk
    override suspend fun saveCurrencyPreference(currency: String) {
        context.dataStore.edit { preferences ->
            preferences[currencyPreferenceKey] = currency
        }
    }

    // Write language preference to disk
    override suspend fun saveLanguagePreference(language: String) {
        context.dataStore.edit { preferences ->
            preferences[languagePreferenceKey] = language
        }
    }
}
