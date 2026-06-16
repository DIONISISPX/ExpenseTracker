package com.dionisispx.expensetracker.presentation.add_expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dionisispx.expensetracker.domain.repository.ExpenseRepository
import com.dionisispx.expensetracker.domain.repository.VisionRepository
import com.dionisispx.expensetracker.domain.usecase.AnalyzeReceiptUseCase
import com.dionisispx.expensetracker.domain.util.ImageProcessor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    private val _category = MutableStateFlow("Other")
    val category: StateFlow<String> = _category.asStateFlow()

    private val _isConfirmingScan = MutableStateFlow(false)
    val isConfirmingScan: StateFlow<Boolean> = _isConfirmingScan.asStateFlow()

    fun updateStoreName(name: String) {
        _storeName.value = name
    }

    fun updateAmount(amt: String) {
        _amount.value = amt
    }

    fun updateCategory(cat: String) {
        _category.value = cat
    }

    fun setConfirmingScan(isConfirming: Boolean) {
        _isConfirmingScan.value = isConfirming
    }

    fun processImage(uriString: String) {
        viewModelScope.launch {
            _isProcessing.value = true

            // 1. Process image to base64
            val base64Result = imageProcessor.getBase64FromUri(uriString)
            if (base64Result.isSuccess) {
                val base64 = base64Result.getOrThrow()

                // 2. Extract text via Vision API
                val textResult = visionRepository.extractTextFromImage(base64)
                if (textResult.isSuccess) {
                    val extractedText = textResult.getOrThrow()

                    // 3. Analyze text with UseCase
                    val userDictionary = getUserDictionary()
                    val receiptData = analyzeReceiptUseCase(extractedText, userDictionary)

                    // 4. Update UI state
                    _storeName.value = receiptData.storeName
                    _amount.value = receiptData.amount
                    _category.value = receiptData.category
                    _isConfirmingScan.value = true
                }
            }

            _isProcessing.value = false
        }
    }

    private suspend fun getUserDictionary(): Map<String, String> {
        val allExpenses = expenseRepository.getAllExpenses().firstOrNull() ?: emptyList()
        val dict = mutableMapOf<String, String>()
        allExpenses.sortedBy { it.date }.forEach { expense ->
            dict[expense.storeName.uppercase()] = expense.category
        }
        return dict
    }
}
