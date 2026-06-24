package com.dionisispx.expensetracker.presentation.add_expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dionisispx.expensetracker.domain.model.ExpenseCategory
import com.dionisispx.expensetracker.domain.repository.ExpenseRepository
import com.dionisispx.expensetracker.domain.repository.VisionRepository
import com.dionisispx.expensetracker.domain.usecase.AnalyzeReceiptUseCase
import com.dionisispx.expensetracker.domain.util.ImageProcessor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    private val imageProcessor: ImageProcessor,
    private val visionRepository: VisionRepository,
    private val analyzeReceiptUseCase: AnalyzeReceiptUseCase,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _storeName = MutableStateFlow("")
    val storeName: StateFlow<String> = _storeName.asStateFlow()

    private val _amount = MutableStateFlow("")
    val amount: StateFlow<String> = _amount.asStateFlow()

    private val _category = MutableStateFlow(ExpenseCategory.OTHER)
    val category: StateFlow<ExpenseCategory> = _category.asStateFlow()

    private val _isConfirmingScan = MutableStateFlow(false)
    val isConfirmingScan: StateFlow<Boolean> = _isConfirmingScan.asStateFlow()

    fun updateStoreName(name: String) {
        _storeName.value = name
    }

    fun updateAmount(amt: String) {
        _amount.value = amt
    }

    fun updateCategory(cat: ExpenseCategory) {
        _category.value = cat
    }

    fun setConfirmingScan(isConfirming: Boolean) {
        _isConfirmingScan.value = isConfirming
    }

    private val _errorEvent = kotlinx.coroutines.flow.MutableSharedFlow<String>()
    val errorEvent = _errorEvent.asSharedFlow()

    fun processImage(uriString: String) {
        viewModelScope.launch {
            _isProcessing.value = true
            android.util.Log.d("AddExpenseViewModel", "Starting image processing for uri: $uriString")

            // 1. Process image to base64
            val base64Result = imageProcessor.getBase64FromUri(uriString)
            if (base64Result.isSuccess) {
                val base64 = base64Result.getOrThrow()
                android.util.Log.d("AddExpenseViewModel", "Successfully converted image to base64")

                // 2. Extract text via Vision API
                val textResult = visionRepository.extractTextFromImage(base64)
                if (textResult.isSuccess) {
                    val extractedText = textResult.getOrThrow()
                    android.util.Log.d("AddExpenseViewModel", "Extracted text: $extractedText")

                    // 3. Analyze text with UseCase
                    val userDictionary = getUserDictionary()
                    val receiptData = analyzeReceiptUseCase(extractedText, userDictionary)
                    android.util.Log.d("AddExpenseViewModel", "Analyzed receipt data: $receiptData")

                    // 4. Update UI state
                    _storeName.value = receiptData.storeName
                    _amount.value = receiptData.amount
                    _category.value = receiptData.category
                    _isConfirmingScan.value = true
                } else {
                    android.util.Log.e("AddExpenseViewModel", "Vision API failed", textResult.exceptionOrNull())
                    _errorEvent.emit("Vision API failed: ${textResult.exceptionOrNull()?.message ?: "Unknown error"}")
                }
            } else {
                android.util.Log.e("AddExpenseViewModel", "Base64 conversion failed", base64Result.exceptionOrNull())
                _errorEvent.emit("Image processing failed: ${base64Result.exceptionOrNull()?.message ?: "Unknown error"}")
            }

            _isProcessing.value = false
        }
    }

    private suspend fun getUserDictionary(): Map<String, ExpenseCategory> {
        val allExpenses = expenseRepository.getAllExpenses().firstOrNull() ?: emptyList()
        val dict = mutableMapOf<String, ExpenseCategory>()
        allExpenses.sortedBy { it.date }.forEach { expense ->
            dict[expense.storeName.uppercase()] = expense.category
        }
        return dict
    }
}
