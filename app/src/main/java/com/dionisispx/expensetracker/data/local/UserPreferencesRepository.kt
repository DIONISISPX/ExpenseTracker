package com.dionisispx.expensetracker.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Keys for saving data safely
    private val totalBudgetKey = floatPreferencesKey("total_budget")
    private val categoryLimitsKey = stringPreferencesKey("category_limits")

    // Flow to read total budget defaulting to 1000
    val totalBudget: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[totalBudgetKey] ?: 1000f
    }

    // Flow to read category limits from JSON string
    val categoryLimits: Flow<Map<String, Float>> = context.dataStore.data.map { preferences ->
        val jsonString = preferences[categoryLimitsKey] ?: "{}"
        val jsonObject = JSONObject(jsonString)
        val map = mutableMapOf<String, Float>()
        jsonObject.keys().forEach { key ->
            map[key] = jsonObject.getDouble(key).toFloat()
        }
        map
    }

    // Write budget to disk
    suspend fun saveTotalBudget(budget: Float) {
        context.dataStore.edit { preferences ->
            preferences[totalBudgetKey] = budget
        }
    }

    // Write limits to disk
    suspend fun saveCategoryLimits(limits: Map<String, Float>) {
        context.dataStore.edit { preferences ->
            val jsonObject = JSONObject()
            limits.forEach { (key, value) ->
                jsonObject.put(key, value.toDouble())
            }
            preferences[categoryLimitsKey] = jsonObject.toString()
        }
    }
}