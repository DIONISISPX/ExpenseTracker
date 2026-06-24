package com.dionisispx.expensetracker.presentation.util

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

fun getCategoryDetails(category: String): Pair<ImageVector, Color> {
    return when (category) {
        "Groceries" -> Icons.Default.ShoppingCart to Color(0xFF81C784)
        "Food & Drink" -> Icons.Default.Restaurant to Color(0xFFFF8A65)
        "Transport & Fuel" -> Icons.Default.DirectionsCar to Color(0xFF64B5F6)
        "Shopping" -> Icons.Default.LocalMall to Color(0xFFBA68C8)
        "Entertainment" -> Icons.Default.Movie to Color(0xFFFFD54F)
        "Bills & Utilities" -> Icons.Default.Receipt to Color(0xFF4DB6AC)
        "Health & Fitness" -> Icons.Default.Favorite to Color(0xFFE57373)
        "Travel" -> Icons.Default.Flight to Color(0xFF7986CB)
        "Home" -> Icons.Default.Home to Color(0xFFA1887F)
        "Education" -> Icons.Default.School to Color(0xFFFFB74D)
        "Personal Care" -> Icons.Default.Spa to Color(0xFFF06292)
        else -> Icons.Default.MoreHoriz to Color(0xFF90A4AE)
    }
}

@Composable
fun getLocalizedCategoryName(category: String): String {
    val resId = when (category) {
        "Groceries" -> R.string.cat_groceries
        "Food & Drink" -> R.string.cat_food_drink
        "Transport & Fuel" -> R.string.cat_transport
        "Shopping" -> R.string.cat_shopping
        "Entertainment" -> R.string.cat_entertainment
        "Bills & Utilities" -> R.string.cat_bills
        "Health & Fitness" -> R.string.cat_health
        "Travel" -> R.string.cat_travel
        "Home" -> R.string.cat_home
        "Education" -> R.string.cat_education
        "Personal Care" -> R.string.cat_personal
        else -> R.string.cat_other
    }
    return stringResource(resId)
}
