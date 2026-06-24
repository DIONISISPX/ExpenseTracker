package com.dionisispx.expensetracker.presentation.util

import java.time.YearMonth
import java.time.ZoneId

object DateUtils {

    fun getStartOfMonthMillis(yearMonth: YearMonth): Long {
        return yearMonth.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun getEndOfMonthMillis(yearMonth: YearMonth): Long {
        return yearMonth.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun getStartOfYearMillis(year: Int): Long {
        return YearMonth.of(year, 1).atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun getEndOfYearMillis(year: Int): Long {
        return YearMonth.of(year, 12).atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}
