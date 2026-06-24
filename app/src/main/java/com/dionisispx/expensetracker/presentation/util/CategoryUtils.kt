package com.dionisispx.expensetracker.presentation.util

import com.dionisispx.expensetracker.domain.model.ExpenseCategory

import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dionisispx.expensetracker.R
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Spa
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

// Get icon and color pair for a given expense category
fun getCategoryDetails(category: ExpenseCategory): Pair<ImageVector, Color> {
    return when (category) {
        ExpenseCategory.GROCERIES -> Icons.Default.ShoppingCart to Color(0xFF81C784)
        ExpenseCategory.FOOD_DRINK -> Icons.Default.Restaurant to Color(0xFFFF8A65)
        ExpenseCategory.TRANSPORT_FUEL -> Icons.Default.DirectionsCar to Color(0xFF64B5F6)
        ExpenseCategory.SHOPPING -> Icons.Default.LocalMall to Color(0xFFBA68C8)
        ExpenseCategory.ENTERTAINMENT -> Icons.Default.Movie to Color(0xFFFFD54F)
        ExpenseCategory.BILLS_UTILITIES -> Icons.Default.Receipt to Color(0xFF4DB6AC)
        ExpenseCategory.HEALTH_FITNESS -> Icons.Default.Favorite to Color(0xFFE57373)
        ExpenseCategory.TRAVEL -> Icons.Default.Flight to Color(0xFF7986CB)
        ExpenseCategory.HOME -> Icons.Default.Home to Color(0xFFA1887F)
        ExpenseCategory.EDUCATION -> Icons.Default.School to Color(0xFFFFB74D)
        ExpenseCategory.PERSONAL_CARE -> Icons.Default.Spa to Color(0xFFF06292)
        ExpenseCategory.OTHER -> Icons.Default.MoreHoriz to Color(0xFF90A4AE)
    }
}

@Composable
// Get localized string representation of an expense category
fun getLocalizedCategoryName(category: ExpenseCategory): String {
    val resId = when (category) {
        ExpenseCategory.GROCERIES -> R.string.cat_groceries
        ExpenseCategory.FOOD_DRINK -> R.string.cat_food_drink
        ExpenseCategory.TRANSPORT_FUEL -> R.string.cat_transport
        ExpenseCategory.SHOPPING -> R.string.cat_shopping
        ExpenseCategory.ENTERTAINMENT -> R.string.cat_entertainment
        ExpenseCategory.BILLS_UTILITIES -> R.string.cat_bills
        ExpenseCategory.HEALTH_FITNESS -> R.string.cat_health
        ExpenseCategory.TRAVEL -> R.string.cat_travel
        ExpenseCategory.HOME -> R.string.cat_home
        ExpenseCategory.EDUCATION -> R.string.cat_education
        ExpenseCategory.PERSONAL_CARE -> R.string.cat_personal
        ExpenseCategory.OTHER -> R.string.cat_other
    }
    return stringResource(resId)
}
