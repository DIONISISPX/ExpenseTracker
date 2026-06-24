package com.dionisispx.expensetracker.data.local

import androidx.room.TypeConverter
import com.dionisispx.expensetracker.domain.model.ExpenseCategory
import java.time.Instant

class Converters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? {
        return value?.let { Instant.ofEpochMilli(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Instant?): Long? {
        return date?.toEpochMilli()
    }

    @TypeConverter
    fun fromExpenseCategory(value: String?): ExpenseCategory? {
        return value?.let { ExpenseCategory.fromDisplayName(it) }
    }

    @TypeConverter
    fun expenseCategoryToString(category: ExpenseCategory?): String? {
        return category?.displayName
    }
}
