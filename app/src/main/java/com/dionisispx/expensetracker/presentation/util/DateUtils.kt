package com.dionisispx.expensetracker.presentation.util

import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId

object DateUtils {

    fun getStartOfMonthInstant(yearMonth: YearMonth): Instant {
        return yearMonth.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
    }

    fun getEndOfMonthInstant(yearMonth: YearMonth): Instant {
        return yearMonth.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant()
    }

    fun getStartOfYearInstant(year: Int): Instant {
        return YearMonth.of(year, 1).atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
    }

    fun getEndOfYearInstant(year: Int): Instant {
        return YearMonth.of(year, 12).atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant()
    }
}
