package com.dionisispx.expensetracker.presentation.add_expense

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.dionisispx.expensetracker.domain.model.Expense
import com.dionisispx.expensetracker.presentation.ExpenseViewModel
import android.Manifest
import androidx.compose.material.icons.filled.Close
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.compose.foundation.clickable
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    onNavigateBack: () -> Unit,
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Κάμερα", "Χειροκίνητη προσθήκη")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LOGO", fontWeight = FontWeight.Bold) },
                actions = {
                    // Close 'X' button
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                if (selectedTab == 0) {
                    CameraUI()
                } else {
                    ManualExpenseForm(onNavigateBack = onNavigateBack, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun CameraUI() {
    val context = LocalContext.current

    // Create the ImageCapture use case to control taking photos
    val imageCapture = remember { ImageCapture.Builder().build() }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasCameraPermission = isGranted }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            // Pass the imageCapture to our preview
            CameraPreview(imageCapture = imageCapture)
        } else {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Text("Camera permission is required", color = Color.White)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp, start = 24.dp, end = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // The Shutter Button
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .border(4.dp, Color.White, CircleShape)
                    .padding(8.dp)
                    .background(Color.White.copy(alpha = 0.5f), CircleShape)
                    // Make it clickable to take the photo
                    .clickable {
                        takePhoto(
                            context = context,
                            imageCapture = imageCapture,
                            onPhotoTaken = { uri ->
                                // TODO: Pass this URI to ML Kit for text recognition!
                                Log.d("ExpenseTracker", "Picture taken! Uri: $uri")
                            }
                        )
                    }
            )

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = { /* TODO: Open device gallery */ }) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Upload from gallery",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualExpenseForm(
    onNavigateBack: () -> Unit,
    viewModel: ExpenseViewModel
) {
    // Form states
    var storeName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Other") }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    val categories = listOf(
        "Groceries", "Food & Drink", "Transport & Fuel", "Shopping",
        "Entertainment", "Bills & Utilities", "Health & Fitness",
        "Travel", "Home", "Education", "Personal Care", "Other"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = storeName,
            onValueChange = { storeName = it },
            label = { Text("Store Name (e.g. Supermarket)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount (€)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )

        ExposedDropdownMenuBox(
            expanded = isDropdownExpanded,
            onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
        ) {
            OutlinedTextField(
                value = category,
                onValueChange = {},
                readOnly = true,
                label = { Text("Category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = isDropdownExpanded,
                onDismissRequest = { isDropdownExpanded = false }
            ) {
                categories.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            category = selectionOption
                            isDropdownExpanded = false
                        }
                    )
                }
            }
        }

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            onClick = {
                if (storeName.isNotBlank() && amount.isNotBlank()) {
                    val newExpense = Expense(
                        storeName = storeName,
                        amount = amount.toDoubleOrNull() ?: 0.0,
                        category = category,
                        date = System.currentTimeMillis()
                    )
                    viewModel.addExpense(newExpense)
                    onNavigateBack()
                }
            }
        ) {
            Text("Save Expense")
        }
    }
}

private fun takePhoto(
    context: Context,
    imageCapture: ImageCapture,
    onPhotoTaken: (Uri) -> Unit
) {
    // Create a temporary file in the app's cache directory
    val photoFile = File(
        context.cacheDir,
        "receipt_${System.currentTimeMillis()}.jpg"
    )

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val savedUri = Uri.fromFile(photoFile)
                Log.d("Camera", "Photo saved successfully: $savedUri")
                onPhotoTaken(savedUri)
            }

            override fun onError(exc: ImageCaptureException) {
                Log.e("Camera", "Photo capture failed: ${exc.message}", exc)
            }
        }
    )
}