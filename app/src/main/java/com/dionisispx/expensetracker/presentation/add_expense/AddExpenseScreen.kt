package com.dionisispx.expensetracker.presentation.add_expense

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.MediaActionSound
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
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
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import androidx.hilt.navigation.compose.hiltViewModel
import com.dionisispx.expensetracker.R
import com.dionisispx.expensetracker.data.remote.AnnotateImageRequest
import com.dionisispx.expensetracker.data.remote.Feature
import com.dionisispx.expensetracker.data.remote.VisionImage
import com.dionisispx.expensetracker.data.remote.VisionNetwork
import com.dionisispx.expensetracker.data.remote.VisionRequest
import com.dionisispx.expensetracker.domain.model.Expense
import com.dionisispx.expensetracker.presentation.ExpenseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    onNavigateBack: () -> Unit,
    viewModel: ExpenseViewModel = hiltViewModel()
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
    var storeName by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("Other") }
    var isConfirmingScan by rememberSaveable { mutableStateOf(false) }

    // Fetch the smart dictionary from the view model
    val userDictionary by viewModel.userDictionary.collectAsState()

    // Fetch currency preference
    val currencyPreference by viewModel.currencyPreference.collectAsState()

    val tabs = listOf(
        stringResource(R.string.tab_camera),
        stringResource(R.string.tab_manual)
    )

    // Handle navigation back logic based on current state
    val handleBackPress = {
        if (isConfirmingScan) {
            isConfirmingScan = false
            storeName = ""
            amount = ""
            category = "Other"
        } else {
            onNavigateBack()
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
                }
            )
        }
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
                        onStoreNameChange = { storeName = it },
                        amount = amount,
                        onAmountChange = { amount = it },
                        category = category,
                        onCategoryChange = { category = it },
                        currencySymbol = currencyPreference,
                        onNavigateBack = onNavigateBack,
                        viewModel = viewModel
                    )
                } else if (selectedTab == 0) {
                    CameraUI(
                        userDictionary = userDictionary,
                        onScanComplete = { scannedStore, scannedAmount, scannedCategory ->
                            storeName = scannedStore
                            amount = scannedAmount
                            category = scannedCategory
                            isConfirmingScan = true
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
                        currencySymbol = currencyPreference,
                        onNavigateBack = onNavigateBack,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun CameraUI(
    userDictionary: Map<String, String>,
    onScanComplete: (String, String, String) -> Unit
) {
    val context = LocalContext.current
    val imageCapture = remember { ImageCapture.Builder().build() }

    val sound = remember { MediaActionSound().apply { load(MediaActionSound.SHUTTER_CLICK) } }
    var isProcessing by rememberSaveable { mutableStateOf(false) }

    // Changed to standard remember so it resets to false when leaving the camera tab
    var flashEnabled by remember { mutableStateOf(false) }

    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasCameraPermission = isGranted }
    )

    // Launcher for photo picker
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                isProcessing = true
                processImageWithCloudVision(context, uri, userDictionary) { store, amount, category ->
                    isProcessing = false
                    onScanComplete(store, amount, category)
                }
            }
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            CameraPreview(
                imageCapture = imageCapture,
                flashEnabled = flashEnabled
            )
        } else {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.camera_permission_required), color = Color.White)
            }
        }

        if (isProcessing) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(stringResource(R.string.analyzing_receipt), color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(bottom = 32.dp, start = 24.dp, end = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                IconButton(
                    onClick = { flashEnabled = !flashEnabled },
                    enabled = !isProcessing
                ) {
                    Icon(
                        imageVector = if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Toggle Flash",
                        tint = if (isProcessing) Color.Gray else Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .border(4.dp, if (isProcessing) Color.Gray else Color.White, CircleShape)
                    .padding(8.dp)
                    .background(if (isProcessing) Color.Gray.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.5f), CircleShape)
                    .clickable(enabled = !isProcessing) {
                        sound.play(MediaActionSound.SHUTTER_CLICK)
                        isProcessing = true
                        takePhoto(
                            context = context,
                            imageCapture = imageCapture,
                            onPhotoTaken = { uri ->
                                processImageWithCloudVision(context, uri, userDictionary) { store, amount, category ->
                                    isProcessing = false
                                    onScanComplete(store, amount, category)
                                }
                            }
                        )
                    }
            )

            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                IconButton(
                    onClick = {
                        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    enabled = !isProcessing
                ) {
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
    currencySymbol: String,
    onNavigateBack: () -> Unit,
    viewModel: ExpenseViewModel
) {
    var isDropdownExpanded by remember { mutableStateOf(false) }
    val categories = listOf(
        Pair("Groceries", stringResource(R.string.cat_groceries)),
        Pair("Food & Drink", stringResource(R.string.cat_food_drink)),
        Pair("Transport & Fuel", stringResource(R.string.cat_transport)),
        Pair("Shopping", stringResource(R.string.cat_shopping)),
        Pair("Entertainment", stringResource(R.string.cat_entertainment)),
        Pair("Bills & Utilities", stringResource(R.string.cat_bills)),
        Pair("Health & Fitness", stringResource(R.string.cat_health)),
        Pair("Travel", stringResource(R.string.cat_travel)),
        Pair("Home", stringResource(R.string.cat_home)),
        Pair("Education", stringResource(R.string.cat_education)),
        Pair("Personal Care", stringResource(R.string.cat_personal)),
        Pair("Other", stringResource(R.string.cat_other))
    )

    val amountLabel = if (currencySymbol == "$") {
        stringResource(R.string.amount_label_left, currencySymbol)
    } else {
        stringResource(R.string.amount_label_right, currencySymbol)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = storeName,
            // Let the text update naturally to prevent lag and dropped keystrokes
            onValueChange = onStoreNameChange,
            label = { Text(stringResource(R.string.store_name)) },
            placeholder = { Text(stringResource(R.string.store_name_placeholder), color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            // Visually transform the text to uppercase on the screen
            visualTransformation = { text ->
                TransformedText(
                    AnnotatedString(text.text.uppercase()),
                    OffsetMapping.Identity
                )
            }
        )

        OutlinedTextField(
            value = amount, onValueChange = onAmountChange,
            label = { Text(amountLabel) },
            placeholder = { Text(stringResource(R.string.amount_placeholder), color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true
        )

        ExposedDropdownMenuBox(expanded = isDropdownExpanded, onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }) {
            OutlinedTextField(
                value = categories.find { it.first == category }?.second ?: stringResource(R.string.cat_other),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.category)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier
                    .menuAnchor(
                        type = androidx.compose.material3.MenuAnchorType.PrimaryNotEditable,
                        enabled = true
                    )
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = isDropdownExpanded, onDismissRequest = { isDropdownExpanded = false }) {
                categories.forEach { (internalName, displayName) ->
                    DropdownMenuItem(
                        text = { Text(displayName) },
                        onClick = {
                            onCategoryChange(internalName)
                            isDropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            modifier = Modifier.fillMaxWidth().height(56.dp),
            onClick = {
                if (storeName.isNotBlank() && amount.isNotBlank()) {
                    val newExpense = Expense(
                        // 3. Force it to uppercase right before saving to the database
                        storeName = storeName.uppercase(),
                        amount = amount.replace(",", ".").toDoubleOrNull() ?: 0.0,
                        category = category,
                        date = System.currentTimeMillis()
                    )
                    viewModel.addExpense(newExpense)
                    onNavigateBack()
                }
            }
        ) {
            Text(stringResource(R.string.save_expense), fontWeight = FontWeight.Bold)
        }
    }
}

// Helper functions

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

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

// Visual character normalizer for greek OCR mistakes
private fun normalizeForFuzzy(input: String): String {
    return input.uppercase()
        .replace("V", "Ψ")
        .replace("S", "Σ")
        .replace("C", "Σ")
        .replace("E", "Ε")
        .replace("N", "Ν")
        .replace("I", "Ι")
        .replace("O", "Ο")
        .replace("P", "Ρ")
        .replace("A", "Α")
        .replace("T", "Τ")
        .replace("H", "Η")
        .replace("K", "Κ")
        .replace("M", "Μ")
        .replace("X", "Χ")
        .replace("Y", "Υ")
        .replace("Z", "Ζ")
        .replace("B", "Β")
        .replace("U", "Υ")
}

// Google cloud vision API integration
private fun processImageWithCloudVision(
    context: Context,
    uri: Uri,
    userDictionary: Map<String, String>,
    onScanComplete: (String, String, String) -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            var inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap != null) {
                // Correct orientation
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

                // Scale image down
                val maxDimension = 1024f
                val scale = minOf(maxDimension / originalBitmap.width, maxDimension / originalBitmap.height)

                if (scale < 1f) {
                    matrix.postScale(scale, scale)
                }
                val processedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)

                // Convert to base64
                val byteArrayOutputStream = ByteArrayOutputStream()
                processedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
                val imageBytes = byteArrayOutputStream.toByteArray()
                val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

                // Prepare request
                val request = VisionRequest(
                    requests = listOf(
                        AnnotateImageRequest(
                            image = VisionImage(content = base64Image),
                            features = listOf(Feature(type = "DOCUMENT_TEXT_DETECTION"))
                        )
                    )
                )

                val apiKey = com.dionisispx.expensetracker.BuildConfig.VISION_API_KEY

                Log.d("CloudVision", "Uploading image to Google Vision API...")
                val response = VisionNetwork.api.annotateImage(apiKey, request)

                val extractedText = response.responses?.firstOrNull()?.textAnnotations?.firstOrNull()?.description ?: ""

                Log.d("CloudVision", "Found text")
                Log.d("CloudVision", extractedText)

                val (store, parsedAmount, category) = extractDataFromText(extractedText, userDictionary)

                withContext(Dispatchers.Main) {
                    onScanComplete(store, parsedAmount, category)
                }
            }
        } catch (e: Exception) {
            Log.e("CloudVision", "Network or Parsing Error", e)
            withContext(Dispatchers.Main) { onScanComplete("", "", "Other") }
        }
    }
}

// Fuzzy string matching algorithms
private fun similarity(s1: String, s2: String): Double {
    var longer = s1
    var shorter = s2
    if (s1.length < s2.length) {
        longer = s2
        shorter = s1
    }
    val longerLength = longer.length
    if (longerLength == 0) return 1.0
    return (longerLength - levenshteinDistance(longer, shorter)) / longerLength.toDouble()
}

private fun levenshteinDistance(lhs: CharSequence, rhs: CharSequence): Int {
    val len0 = lhs.length + 1
    val len1 = rhs.length + 1
    var cost = IntArray(len0)
    var newcost = IntArray(len0)
    for (i in 0 until len0) cost[i] = i
    for (j in 1 until len1) {
        newcost[0] = j
        for (i in 1 until len0) {
            val match = if (lhs[i - 1] == rhs[j - 1]) 0 else 1
            val costReplace = cost[i - 1] + match
            val costInsert = cost[i] + 1
            val costDelete = newcost[i - 1] + 1
            newcost[i] = minOf(minOf(costInsert, costDelete), costReplace)
        }
        val swap = cost
        cost = newcost
        newcost = swap
    }
    return cost[len0 - 1]
}

// AI parser logic
private fun extractDataFromText(
    text: String,
    userDictionary: Map<String, String>
): Triple<String, String, String> {
    var finalStore = ""
    var finalAmount = ""
    var finalCategory = "Other"

    val upperText = text.uppercase()

    // Strip quotes and brackets to help fuzzy matching find exact words
    val cleanTextForNames = upperText.replace("\"", "").replace("'", "").replace("«", "").replace("»", "")
    val words = cleanTextForNames.split(Regex("\\s+")).filter { it.isNotBlank() }

    // Seed dictionary
    val seedDictionary = mapOf(
        "ΣΚΛΑΒΕΝΙΤΗΣ" to "Groceries",
        "ΓΑΛΑΞΙΑΣ" to "Groceries",
        "ΑΒ ΒΑΣΙΛΟΠΟΥΛΟΣ" to "Groceries",
        "ΜΑΣΟΥΤΗΣ" to "Groceries",
        "ΚΡΗΤΙΚΟΣ" to "Groceries",
        "LIDL" to "Groceries",

        "ZARA" to "Shopping",
        "H&M" to "Shopping",
        "PULL&BEAR" to "Shopping",
        "BERSHKA" to "Shopping",
        "NIKE" to "Shopping",
        "ADIDAS" to "Shopping",
        "COSMOS SPORT" to "Shopping",
        "JD" to "Shopping",
        "PLAISIO" to "Shopping",
        "PUBLIC" to "Shopping",
        "ΚΩΤΣΟΒΟΛΟΣ" to "Shopping",
        "ΓΕΡΜΑΝΟΣ" to "Shopping",

        "VILLAGE" to "Entertainment",
        "OPTIONS" to "Entertainment",

        "OASA" to "Transport & Fuel",
        "ΣΤΑΣΥ" to "Transport & Fuel",
        "EKO" to "Transport & Fuel",
        "BP" to "Transport & Fuel",
        "SHELL" to "Transport & Fuel",
        "ETEKA" to "Transport & Fuel",
        "REVOIL" to "Transport & Fuel",
        "ΕΛΙΝ" to "Transport & Fuel",
        "AVIN" to "Transport & Fuel",

        "ΓΡΗΓΟΡΗΣ" to "Food & Drink",
        "EVEREST" to "Food & Drink",
        "COFFEE ISLAND" to "Food & Drink",
        "IL TOTO" to "Food & Drink",
        "STARBUCKS" to "Food & Drink",
        "COFFEE BERRY" to "Food & Drink",
        "MCDONALD'S" to "Food & Drink",
        "JACKAROO" to "Food & Drink",
        "KFC" to "Food & Drink",
        "PIZZA FAN" to "Food & Drink",
        "DOMINO'S" to "Food & Drink",
        "PIZZA HUT" to "Food & Drink",
        "GOODY'S" to "Food & Drink",
        "BREAD FACTORY" to "Food & Drink",
        "ΣΤΕΡΓΙΟΥ" to "Food & Drink",
        "NANOU" to "Food & Drink",

        "YAVA" to "Health & Fitness",
        "PLANET FITNESS" to "Health & Fitness",
        "ALTERLIFE" to "Health & Fitness",

        "COSMOTE" to "Bills & Utilities",
        "NOVA" to "Bills & Utilities",
        "VODAFONE" to "Bills & Utilities",
        "INALAN" to "Bills & Utilities",
        "ΔΕΗ" to "Bills & Utilities",
        "PROTERGIA" to "Bills & Utilities",
        "ΕΥΔΑΠ" to "Bills & Utilities"

    )

    // Combine dictionaries
    val combinedDictionary = seedDictionary + userDictionary

    // Store extraction using multi-word phrase matching
    var bestMatchScore = 0.0

    val phrases = mutableListOf<String>()
    for (i in words.indices) {
        phrases.add(words[i])
        if (i < words.size - 1) phrases.add("${words[i]} ${words[i+1]}")
        if (i < words.size - 2) phrases.add("${words[i]} ${words[i+1]} ${words[i+2]}")
        if (i < words.size - 3) phrases.add("${words[i]} ${words[i+1]} ${words[i+2]} ${words[i+3]}")
    }

    for (phrase in phrases) {
        if (phrase.length < 3) continue

        for ((store, category) in combinedDictionary) {

            val normalizedPhrase = normalizeForFuzzy(phrase)
            val normalizedStore = normalizeForFuzzy(store)

            // Check if exact match otherwise calculate fuzzy score
            val isExactMatch = normalizedPhrase.replace(" ", "") == normalizedStore.replace(" ", "")
            val score = if (isExactMatch) 1.0 else similarity(normalizedPhrase, normalizedStore)

            // Only update if new score is higher than best score
            if (score > 0.70 && score > bestMatchScore) {
                bestMatchScore = score
                finalStore = store
                finalCategory = category
            }
        }
    }

    // Smart amount extraction
    val lines = upperText.lines().map { it.trim() }.filter { it.isNotEmpty() }

    // Negative lookbehinds and lookaheads ensure we do not match glued digits
    val priceRegex = Regex("(?<!\\d)\\d+[.,]\\d{2}(?!\\d)")

    val totalKeywords = listOf(
        "ΣΥΝΟΛΟ", "ΤΕΛΙΚΟ ΣΥΝΟΛΟ", "ΜΕΡΙΚΟ ΣΥΝΟΛΟ", "ΣΥΝΟΛΟ €", "TOTAL"
    )

    val ignoreKeywords = listOf(
        "ΜΕΤΡΗΤΑ", "ΚΑΡΤΑ", "ΠΙΣΤΩΤΙΚΗ ΚΑΡΤΑ", "ΠΛΗΡΩΜΗ ΜΕ ΚΑΡΤΑ",
        "ΑΜΕΣΗ ΚΑΡΤΑ", "ΚΑΡΤΑ-1", "Π. ΚΑΡΤΑ", "ΠΙΣΤ. ΚΑΡΤΑ",
        "ΡΕΣΤΑ", "CHANGE", "ΤΗΛ", "ΑΦΜ", "ΔΟΥ", "CARD"
    )

    for (i in lines.indices) {
        val line = lines[i]

        val normalizedLine = line.replace(" ", "").replace("0", "Ο").replace("1", "Ι")

        val containsTotal = totalKeywords.any { line.contains(it) || normalizedLine.contains(it.replace(" ", "")) }
        val containsIgnore = ignoreKeywords.any { line.contains(it) || normalizedLine.contains(it.replace(" ", "")) }

        if (containsTotal && !containsIgnore) {
            var matches = priceRegex.findAll(line).toList()

            if (matches.isEmpty() && i + 1 < lines.size) {
                matches = priceRegex.findAll(lines[i + 1]).toList()
            }

            if (matches.isNotEmpty()) {
                finalAmount = matches.last().value.replace(",", ".").replace("-", ".")
                break
            }
        }
    }

    // Safe fallback using mathematical triangulation
    if (finalAmount.isEmpty()) {
        val validNumbers = mutableListOf<Double>()

        for (line in lines) {
            val normalizedLine = line.replace(" ", "").replace("0", "Ο")
            val containsIgnore = ignoreKeywords.any { line.contains(it) || normalizedLine.contains(it.replace(" ", "")) }

            // Exclude lines with explicit percentages or item multipliers
            if (containsIgnore || line.contains("X") || line.contains("Χ") || line.contains("%")) continue

            val matches = priceRegex.findAll(line)
            for (match in matches) {
                val normalizedStr = match.value.replace(",", ".").replace("-", ".")
                val num = normalizedStr.toDoubleOrNull() ?: 0.0

                // Hard exclude greek VAT rates and zero
                if (num != 13.00 && num != 24.00 && num != 6.00 && num != 0.0) {
                    validNumbers.add(num)
                }
            }
        }

        if (validNumbers.isNotEmpty()) {
            // Analyze the last 4 numbers as they usually contain the total block
            val tail = validNumbers.takeLast(4)
            var foundByMath = false

            // Scenario A: Card payment where amount appears twice
            for (i in 0 until tail.size - 1) {
                if (tail[i] == tail[i+1]) {
                    finalAmount = String.format(java.util.Locale.US, "%.2f", tail[i])
                    foundByMath = true
                    break
                }
            }

            // Scenario B: Cash payment where total plus change is cash given
            if (!foundByMath && tail.size >= 3) {
                val a = tail[tail.size - 3]
                val b = tail[tail.size - 2]
                val c = tail[tail.size - 1]

                // Floating point math check to avoid rounding errors
                val sumAC = ((a + c) * 100).roundToInt() / 100.0

                if (sumAC == b) {
                    finalAmount = String.format(java.util.Locale.US, "%.2f", a)
                    foundByMath = true
                }
            }

            // Scenario C: Fallback to max number
            if (!foundByMath) {
                val maxNum = validNumbers.maxOrNull() ?: 0.0
                finalAmount = String.format(java.util.Locale.US, "%.2f", maxNum)
            }
        }
    }

    return Triple(finalStore, finalAmount, finalCategory)
}