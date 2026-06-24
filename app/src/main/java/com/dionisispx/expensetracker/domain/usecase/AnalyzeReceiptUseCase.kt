package com.dionisispx.expensetracker.domain.usecase

import com.dionisispx.expensetracker.domain.model.ReceiptData
import javax.inject.Inject


import com.dionisispx.expensetracker.domain.model.ExpenseCategory

// Analyzes receipt text to extract store, amount, and category
class AnalyzeReceiptUseCase @Inject constructor(
    private val storeNameMatcher: StoreNameMatcher,
    private val priceExtractor: PriceExtractor
) {

    // Analyzes raw receipt text and returns extracted data
    operator fun invoke(text: String, userDictionary: Map<String, ExpenseCategory>): ReceiptData {
        // Convert to uppercase for matching
        val upperText = text.uppercase()

        // Match store name and category
        val (finalStore, finalCategory) = storeNameMatcher.matchStoreName(upperText, userDictionary)
        
        // Extract total amount from text
        val finalAmount = priceExtractor.extractTotal(upperText)

        return ReceiptData(finalStore, finalAmount, finalCategory)
    }
}
