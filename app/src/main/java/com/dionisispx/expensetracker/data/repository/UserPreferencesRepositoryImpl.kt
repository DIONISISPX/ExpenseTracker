package com.dionisispx.expensetracker.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dionisispx.expensetracker.domain.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import com.dionisispx.expensetracker.domain.model.ExpenseCategory
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : UserPreferencesRepository {

    // Defines datastore keys
    private val isFirstRunKey = booleanPreferencesKey("is_first_run")
    private val totalBudgetKey = intPreferencesKey("total_budget")
    private val categoryLimitsKey = stringPreferencesKey("category_limits")
    private val themePreferenceKey = stringPreferencesKey("theme_preference")
    private val currencyPreferenceKey = stringPreferencesKey("currency_preference")
    private val languagePreferenceKey = stringPreferencesKey("language_preference")

    // Observes initial launch state
    override val isFirstRun: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[isFirstRunKey] ?: true
    }

    // Observes total budget
    override val totalBudget: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[totalBudgetKey] ?: 1000
    }

    // Observes category budgets
    override val categoryLimits: Flow<Map<ExpenseCategory, Float>> = context.dataStore.data.map { preferences ->
        val jsonString = preferences[categoryLimitsKey] ?: "{}"
        try {
            val jsonObject = JSONObject(jsonString)
            val map = mutableMapOf<ExpenseCategory, Float>()
            jsonObject.keys().forEach { key ->
                map[ExpenseCategory.fromDisplayName(key)] = jsonObject.getDouble(key).toFloat()
            }
            map
        } catch (e: Exception) {
            android.util.Log.e("UserPrefsRepo", "Error deserializing category limits", e)
            emptyMap()
        }
    }

    // Observes UI theme
    override val themePreference: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[themePreferenceKey] ?: "system"
    }

    // Observes display currency
    override val currencyPreference: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[currencyPreferenceKey] ?: "€"
    }

    // Observes display language
    override val languagePreference: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[languagePreferenceKey] ?: "el"
    }

    // Saves total budget
    override suspend fun saveTotalBudget(budget: Int) {
        context.dataStore.edit { preferences ->
            preferences[totalBudgetKey] = budget
        }
    }

    // Marks first run complete
    override suspend fun setFirstRunCompleted() {
        context.dataStore.edit { preferences ->
            preferences[isFirstRunKey] = false
        }
    }

    // Saves category budgets
    override suspend fun saveCategoryLimits(limits: Map<ExpenseCategory, Float>) {
        context.dataStore.edit { preferences ->
            val jsonObject = JSONObject()
            limits.forEach { (key, value) ->
                jsonObject.put(key.displayName, value.toDouble())
            }
            preferences[categoryLimitsKey] = jsonObject.toString()
        }
    }

    // Saves UI theme
    override suspend fun saveThemePreference(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[themePreferenceKey] = theme
        }
    }

    // Saves display currency
    override suspend fun saveCurrencyPreference(currency: String) {
        context.dataStore.edit { preferences ->
            preferences[currencyPreferenceKey] = currency
        }
    }

    // Saves display language
    override suspend fun saveLanguagePreference(language: String) {
        context.dataStore.edit { preferences ->
            preferences[languagePreferenceKey] = language
        }
    }
}
