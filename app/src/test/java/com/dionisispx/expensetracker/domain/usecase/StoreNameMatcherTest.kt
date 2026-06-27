package com.dionisispx.expensetracker.domain.usecase

import com.dionisispx.expensetracker.domain.model.ExpenseCategory
import org.junit.Assert.*
import org.junit.Test

class StoreNameMatcherTest {

    private val normalizer = GreekTextNormalizer()
    private val matcher = StoreNameMatcher(normalizer)

    @Test
    fun matchStoreName_withExactMatchAndSuffix_removesSuffixAndReturnsCorrectCategory() {
        // Suffix should be ignored
        val ocrText = "ΣΚΛΑΒΕΝΙΤΗΣ ΙΚΕ\nΑΦΜ: 999999999\nΣΥΝΟΛΟ 10.00"
        
        val result = matcher.matchStoreName(ocrText, emptyMap())
        
        assertEquals("ΣΚΛΑΒΕΝΙΤΗΣ", result.first)
        assertEquals(ExpenseCategory.GROCERIES, result.second)
    }

    @Test
    fun matchStoreName_withFuzzyMatch_matchesCorrectStore() {
        // OCR read typo instead of correct store name
        val ocrText = "ΣΚΛΑΒΗΝΙΤΗΣ\nΛ. ΚΗΦΙΣΙΑΣ 12"
        
        val result = matcher.matchStoreName(ocrText, emptyMap())
        
        assertEquals("ΣΚΛΑΒΕΝΙΤΗΣ", result.first)
        assertEquals(ExpenseCategory.GROCERIES, result.second)
    }

    @Test
    fun matchStoreName_withCustomUserDictionary_matchesUserStore() {
        val ocrText = "MY SPECIAL LOCAL BAKERY\n1.50"
        val userDict = mapOf("MY SPECIAL LOCAL BAKERY" to ExpenseCategory.FOOD_DRINK)
        
        val result = matcher.matchStoreName(ocrText, userDict)
        
        assertEquals("MY SPECIAL LOCAL BAKERY", result.first)
        assertEquals(ExpenseCategory.FOOD_DRINK, result.second)
    }

    @Test
    fun matchStorelName_withNoMatch_fallsBackToFirstValidLine() {
        // Skip ignored header tokens and fallback to first valid line
        val ocrText = """
            ΦΟΡΟΛΟΓΙΚΗ ΑΠΟΔΕΙΞΗ ΛΙΑΝΙΚΗΣ
            ΚΑΦΕ ΟΛΥΜΠΙΑ
            ΤΗΛ: 2101234567
        """.trimIndent()
        
        val result = matcher.matchStoreName(ocrText, emptyMap())
        
        assertEquals("ΚΑΦΕ ΟΛΥΜΠΙΑ", result.first)
        assertEquals(ExpenseCategory.OTHER, result.second)
    }
}
