package com.dionisispx.expensetracker.presentation.add_expense

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.media.MediaActionSound
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import com.googlecode.tesseract.android.TessBaseAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    onNavigateBack: () -> Unit,
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var storeName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Other") }

    val tabs = listOf("Κάμερα", "Χειροκίνητη προσθήκη")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LOGO", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
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
                    CameraUI(
                        onScanComplete = { scannedStore, scannedAmount, scannedCategory ->
                            storeName = scannedStore
                            amount = scannedAmount
                            category = scannedCategory
                            selectedTab = 1
                        }
                    )
                } else {
                    ManualExpenseForm(
                        storeName = storeName,
                        onStoreNameChange = { storeName = it },
                        amount = amount,
                        onAmountChange = { amount = it },
                        category = category,
                        onCategoryChange = { category = it },
                        onNavigateBack = onNavigateBack,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun CameraUI(onScanComplete: (String, String, String) -> Unit) {
    val context = LocalContext.current
    val imageCapture = remember { ImageCapture.Builder().build() }

    // NEW: System Sound functionality
    val sound = remember { MediaActionSound().apply { load(MediaActionSound.SHUTTER_CLICK) } }

    // NEW: Processing state for loading UI
    var isProcessing by remember { mutableStateOf(false) }

    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasCameraPermission = isGranted }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            CameraPreview(imageCapture = imageCapture)
        } else {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
                Text("Camera permission is required", color = Color.White)
            }
        }

        // NEW: Loading Overlay (Covers the screen while parsing)
        if (isProcessing) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Ανάλυση απόδειξης...", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(bottom = 32.dp, start = 24.dp, end = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // The Shutter Button
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .border(4.dp, if (isProcessing) Color.Gray else Color.White, CircleShape)
                    .padding(8.dp)
                    .background(if (isProcessing) Color.Gray.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.5f), CircleShape)
                    .clickable(enabled = !isProcessing) { // Disable clicks while processing!
                        sound.play(MediaActionSound.SHUTTER_CLICK) // Play sound!
                        isProcessing = true // Show loading
                        takePhoto(
                            context = context,
                            imageCapture = imageCapture,
                            onPhotoTaken = { uri ->
                                processImageWithTesseract(context, uri) { store, amount, category ->
                                    isProcessing = false // Hide loading
                                    onScanComplete(store, amount, category)
                                }
                            }
                        )
                    }
            )

            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                IconButton(onClick = { /* TODO */ }, enabled = !isProcessing) {
                    Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = "Gallery", tint = if (isProcessing) Color.Gray else Color.White, modifier = Modifier.size(32.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualExpenseForm(
    storeName: String, onStoreNameChange: (String) -> Unit,
    amount: String, onAmountChange: (String) -> Unit,
    category: String, onCategoryChange: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ExpenseViewModel
) {
    var isDropdownExpanded by remember { mutableStateOf(false) }
    val categories = listOf("Groceries", "Food & Drink", "Transport & Fuel", "Shopping", "Entertainment", "Bills & Utilities", "Health & Fitness", "Travel", "Home", "Education", "Personal Care", "Other")

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = storeName, onValueChange = onStoreNameChange,
            label = { Text("Store Name (e.g. Supermarket)") }, modifier = Modifier.fillMaxWidth(), singleLine = true
        )

        OutlinedTextField(
            value = amount, onValueChange = onAmountChange,
            label = { Text("Amount (€)") }, modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true
        )

        ExposedDropdownMenuBox(expanded = isDropdownExpanded, onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }) {
            OutlinedTextField(
                value = category, onValueChange = {}, readOnly = true, label = { Text("Category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(), modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = isDropdownExpanded, onDismissRequest = { isDropdownExpanded = false }) {
                categories.forEach { selectionOption ->
                    DropdownMenuItem(text = { Text(selectionOption) }, onClick = { onCategoryChange(selectionOption); isDropdownExpanded = false })
                }
            }
        }

        Button(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            onClick = {
                if (storeName.isNotBlank() && amount.isNotBlank()) {
                    val newExpense = Expense(storeName = storeName, amount = amount.replace(",", ".").toDoubleOrNull() ?: 0.0, category = category, date = System.currentTimeMillis())
                    viewModel.addExpense(newExpense)
                    onNavigateBack()
                }
            }
        ) {
            Text("Save Expense")
        }
    }
}

// --- HELPER FUNCTIONS ---

private fun takePhoto(context: Context, imageCapture: ImageCapture, onPhotoTaken: (Uri) -> Unit) {
    val photoFile = File(context.cacheDir, "receipt_${System.currentTimeMillis()}.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
    imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(context), object : ImageCapture.OnImageSavedCallback {
        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
            onPhotoTaken(Uri.fromFile(photoFile))
        }
        override fun onError(exc: ImageCaptureException) {
            Log.e("Camera", "Capture failed", exc)
        }
    })
}

private fun prepareTesseract(context: Context): String {
    val tessDir = File(context.filesDir, "tessdata")
    if (!tessDir.exists()) tessDir.mkdirs()
    val languages = listOf("ell.traineddata", "eng.traineddata")
    for (lang in languages) {
        val trainedDataFile = File(tessDir, lang)
        if (!trainedDataFile.exists()) {
            context.assets.open("tessdata/$lang").use { input -> FileOutputStream(trainedDataFile).use { output -> input.copyTo(output) } }
        }
    }
    return context.filesDir.absolutePath
}

private fun processImageWithTesseract(context: Context, uri: Uri, onScanComplete: (String, String, String) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val dataPath = prepareTesseract(context)
            var inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap != null) {
                // 1. Διορθώνουμε το Orientation
                inputStream = context.contentResolver.openInputStream(uri)
                val exif = ExifInterface(inputStream!!)
                val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                inputStream.close()

                val matrix = Matrix()
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                    ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                }

                // 2. Μικραίνουμε την εικόνα για ταχύτητα
                val maxDimension = 1500f
                val scale = minOf(maxDimension / originalBitmap.width, maxDimension / originalBitmap.height)

                if (scale < 1f) {
                    matrix.postScale(scale, scale)
                }

                val processedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)

                val tessAPI = TessBaseAPI()
                if (tessAPI.init(dataPath, "ell+eng")) {
                    tessAPI.setImage(processedBitmap)
                    val extractedText = tessAPI.utF8Text
                    tessAPI.recycle()

                    // ==========================================
                    // ΕΔΩ ΕΙΝΑΙ ΤΑ LOGS ΠΟΥ ΕΙΧΑΜΕ ΧΑΣΕΙ!
                    // ==========================================
                    Log.d("Tesseract", "====================================")
                    Log.d("Tesseract", "TESSERACT FOUND TEXT:\n$extractedText")
                    Log.d("Tesseract", "====================================")

                    // Parse the text!
                    val (store, parsedAmount, category) = extractDataFromText(extractedText)

                    // Switch back to the Main Thread to update the UI
                    withContext(Dispatchers.Main) {
                        onScanComplete(store, parsedAmount, category)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("Tesseract", "Error", e)
            withContext(Dispatchers.Main) { onScanComplete("", "", "Other") }
        }
    }
}

// ---------------------------------------------------------------------
// THE AI PARSER: SMART LINE-BY-LINE ALGORITHM (V4 - Tuned Heuristics)
// ---------------------------------------------------------------------
private fun extractDataFromText(text: String): Triple<String, String, String> {
    var finalStore = ""
    var finalAmount = ""
    var finalCategory = "Other"

    val upperText = text.uppercase()

    // 1. SEED DICTIONARY (Εμπλουτισμένο με τα λάθη του Tesseract!)
    val knownStores = mapOf(
        "ΣΚΛΑΒΕΝΙΤΗΣ" to "Groceries",
        "ΓΑΛΑΞΙΑΣ" to "Groceries",
        "ΓΩΛΩ" to "Groceries", // Typo για ΓΑΛΑΞΙΑΣ
        "ΓΩΔΩ" to "Groceries", // Typo για ΓΑΛΑΞΙΑΣ
        "ΒΑΣΙΛΟΠΟΥΛΟΣ" to "Groceries",
        "ΜΑΣΟΥΤΗΣ" to "Groceries",
        "ZARA" to "Shopping",
        "EKO" to "Transport & Fuel",
        "SHELL" to "Transport & Fuel",
        "ΓΡΗΓΟΡΗΣ" to "Food & Drink",
        "EVEREST" to "Food & Drink"
    )

    for ((store, category) in knownStores) {
        if (upperText.contains(store) || upperText.replace(" ", "").contains(store)) {
            // Αν βρει το "ΓΩΛΩ", θέλουμε να τυπώσει το σωστό όνομα "ΓΑΛΑΞΙΑΣ", όχι το λάθος!
            finalStore = if (store == "ΓΩΛΩ" || store == "ΓΩΔΩ") "ΓΑΛΑΞΙΑΣ" else store
            finalCategory = category
            break
        }
    }

    // 2. SMART AMOUNT EXTRACTION
    val lines = upperText.lines().map { it.trim() }.filter { it.isNotEmpty() }
    val priceRegex = Regex("\\d+[.,]\\d{2}")

    // --- ΛΙΣΤΕΣ ΜΕ ΤΑ ΛΑΘΗ ΤΟΥ TESSERACT (Ενημερωμένες από τα Logs σου) ---
    val totalKeywords = listOf(
        "ΣΥΝΟΛΟ", "ΣΥΝΩΛΔΟ", "TOTAL", "ΜΕΡΙΚΟ", "ΣΥΝ.", "EYNOAO", "ZYNOAO",
        "2YNOAO", "XYNOAO", "XY NOAO", "LYNOAO", "ZYN.",
        "XZXYNOAO", "ΣΥΝΩΛΑΟ", "ΞΣΥΝΩΛΟ", "ΣΥΝΩΛΟ", "MEPIKO"
    )

    val ignoreKeywords = listOf(
        "ΜΕΤΡΗΤΑ", "ΡΕΣΤΑ", "CASH", "METPHTA", "PEETA", "METP.", "CHANGE", "KAPTA"
    )

    for (i in lines.indices) {
        val line = lines[i]

        val normalizedLine = line.replace(" ", "").replace("0", "Ο").replace("1", "Ι")

        val containsTotal = totalKeywords.any { line.contains(it) || normalizedLine.contains(it.replace(" ", "")) }
        val containsIgnore = ignoreKeywords.any { line.contains(it) || normalizedLine.contains(it.replace(" ", "")) }

        if (containsTotal && !containsIgnore) {

            var matches = priceRegex.findAll(line).toList()

            // Αν δεν βρούμε στην ίδια γραμμή, κοιτάμε στην επόμενη
            if (matches.isEmpty() && i + 1 < lines.size) {
                matches = priceRegex.findAll(lines[i + 1]).toList()
            }

            if (matches.isNotEmpty()) {
                finalAmount = matches.last().value.replace(",", ".")
                break
            }
        }
    }

    // 3. THE SAFE FALLBACK
    if (finalAmount.isEmpty()) {
        val validNumbers = mutableListOf<Double>()

        for (line in lines) {
            val normalizedLine = line.replace(" ", "").replace("0", "Ο")
            val containsIgnore = ignoreKeywords.any { line.contains(it) || normalizedLine.contains(it.replace(" ", "")) }
            if (containsIgnore) continue

            val matches = priceRegex.findAll(line)
            for (match in matches) {
                val num = match.value.replace(",", ".").toDoubleOrNull() ?: 0.0
                // Αγνοούμε τα ποσοστά ΦΠΑ (13, 24, 6) και τα μηδενικά
                if (num != 13.00 && num != 24.00 && num != 6.00 && num != 0.0) {
                    validNumbers.add(num)
                }
            }
        }

        if (validNumbers.isNotEmpty()) {
            val maxNum = validNumbers.maxOrNull() ?: 0.0
            finalAmount = String.format(java.util.Locale.US, "%.2f", maxNum)
        }
    }

    return Triple(finalStore, finalAmount, finalCategory)
}