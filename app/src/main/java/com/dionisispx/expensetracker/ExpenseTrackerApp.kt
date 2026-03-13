package com.dionisispx.expensetracker

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// Trigger hilt code generation
@HiltAndroidApp
class ExpenseTrackerApp : Application()