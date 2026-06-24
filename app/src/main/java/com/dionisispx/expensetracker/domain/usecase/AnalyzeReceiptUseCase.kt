package com.dionisispx.expensetracker.domain.usecase

import com.dionisispx.expensetracker.domain.model.ReceiptData
import java.util.Locale
import javax.inject.Inject


class AnalyzeReceiptUseCase @Inject constructor(
    private val storeNameMatcher: StoreNameMatcher,
    private val priceExtractor: PriceExtractor
) {

    // Analyzes raw receipt text and returns extracted data
    operator fun invoke(text: String, userDictionary: Map<String, String>): ReceiptData {
        val upperText = text.uppercase()

        val (finalStore, finalCategory) = storeNameMatcher.matchStoreName(upperText, userDictionary)
        
        // Process total amount from text
        val finalAmount = priceExtractor.extractTotal(upperText)

        return ReceiptData(finalStore, finalAmount, finalCategory)
    }
}
