package com.dionisispx.expensetracker.presentation.util

import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId

// Utility object for date and time conversions
object DateUtils {

    // Gets the instant for the start of the given month
    fun getStartOfMonthInstant(yearMonth: YearMonth): Instant {
        return yearMonth.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
    }

    // Gets the instant for the end of the given month
    fun getEndOfMonthInstant(yearMonth: YearMonth): Instant {
        return yearMonth.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant()
    }

    // Gets the instant for the start of the given year
    fun getStartOfYearInstant(year: Int): Instant {
        return YearMonth.of(year, 1).atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
    }

    // Gets the instant for the end of the given year
    fun getEndOfYearInstant(year: Int): Instant {
        return YearMonth.of(year, 12).atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant()
    }
}
