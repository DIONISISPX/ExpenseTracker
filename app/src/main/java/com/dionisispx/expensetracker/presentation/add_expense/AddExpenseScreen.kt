package com.dionisispx.expensetracker.presentation.add_expense

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dionisispx.expensetracker.R
import com.dionisispx.expensetracker.domain.model.ExpenseCategory
import com.dionisispx.expensetracker.presentation.expense.ExpenseViewModel
import com.dionisispx.expensetracker.presentation.preferences.PreferencesViewModel
import com.dionisispx.expensetracker.presentation.add_expense.components.CameraUI
import com.dionisispx.expensetracker.presentation.add_expense.components.ManualExpenseForm
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    onNavigateBack: () -> Unit,
    addExpenseViewModel: AddExpenseViewModel = hiltViewModel(),
    expenseViewModel: ExpenseViewModel = hiltViewModel(),
    prefsViewModel: PreferencesViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // Lock screen orientation to portrait to prevent camera aspect ratio squishing and UI disappearing
    DisposableEffect(Unit) {
        val activity = context.findActivity()
        val originalOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        onDispose {
            activity?.requestedOrientation = originalOrientation
        }
    }

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    val storeName by addExpenseViewModel.storeName.collectAsState()
    val amount by addExpenseViewModel.amount.collectAsState()
    val category by addExpenseViewModel.category.collectAsState()
    val isConfirmingScan by addExpenseViewModel.isConfirmingScan.collectAsState()
    val isProcessing by addExpenseViewModel.isProcessing.collectAsState()

    // Fetch currency preference
    val currencyPreference by prefsViewModel.currencyPreference.collectAsState()

    val tabs = listOf(
        stringResource(R.string.tab_camera),
        stringResource(R.string.tab_manual)
    )

    val handleBackPress = {
        if (isConfirmingScan) {
            addExpenseViewModel.setConfirmingScan(false)
            addExpenseViewModel.updateStoreName("")
            addExpenseViewModel.updateAmount("")
            addExpenseViewModel.updateCategory(ExpenseCategory.OTHER)
        } else {
            onNavigateBack()
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        launch {
            addExpenseViewModel.errorEvent.collect { message ->
                snackbarHostState.showSnackbar(message)
            }
        }
        launch {
            expenseViewModel.errorEvent.collect { message ->
                snackbarHostState.showSnackbar(message)
            }
        }
    }

    BackHandler(onBack = handleBackPress)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_expense), fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = handleBackPress) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                },
                windowInsets = WindowInsets(0.dp)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

            // Hide tabs if the user is confirming a scan
            if (!isConfirmingScan) {
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontWeight = FontWeight.Bold) }
                        )
                    }
                }
            }

            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                if (isConfirmingScan) {
                    ManualExpenseForm(
                        storeName = storeName,
                        onStoreNameChange = { addExpenseViewModel.updateStoreName(it) },
                        amount = amount,
                        onAmountChange = { addExpenseViewModel.updateAmount(it) },
                        category = category,
                        onCategoryChange = { addExpenseViewModel.updateCategory(it) },
                        currencySymbol = currencyPreference,
                        onNavigateBack = onNavigateBack,
                        onSaveExpense = { expenseViewModel.addExpense(it) }
                    )
                } else if (selectedTab == 0) {
                    CameraUI(
                        isProcessing = isProcessing,
                        onImageSelected = { uriString ->
                            addExpenseViewModel.processImage(uriString)
                        }
                    )
                } else {
                    ManualExpenseForm(
                        storeName = storeName,
                        onStoreNameChange = { addExpenseViewModel.updateStoreName(it) },
                        amount = amount,
                        onAmountChange = { addExpenseViewModel.updateAmount(it) },
                        category = category,
                        onCategoryChange = { addExpenseViewModel.updateCategory(it) },
                        currencySymbol = currencyPreference,
                        onNavigateBack = onNavigateBack,
                        onSaveExpense = { expenseViewModel.addExpense(it) }
                    )
                }
            }
        }
    }
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}