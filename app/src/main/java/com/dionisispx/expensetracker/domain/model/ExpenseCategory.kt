package com.dionisispx.expensetracker.domain.model

enum class ExpenseCategory(val displayName: String) {
    GROCERIES("Groceries"),
    FOOD_DRINK("Food & Drink"),
    TRANSPORT_FUEL("Transport & Fuel"),
    SHOPPING("Shopping"),
    ENTERTAINMENT("Entertainment"),
    BILLS_UTILITIES("Bills & Utilities"),
    HEALTH_FITNESS("Health & Fitness"),
    TRAVEL("Travel"),
    HOME("Home"),
    EDUCATION("Education"),
    PERSONAL_CARE("Personal Care"),
    OTHER("Other");

    companion object {
        fun fromDisplayName(name: String): ExpenseCategory {
            return entries.find { it.displayName == name } ?: OTHER
        }
    }
}
