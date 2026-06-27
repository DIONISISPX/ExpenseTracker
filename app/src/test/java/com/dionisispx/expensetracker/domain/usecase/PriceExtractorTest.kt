package com.dionisispx.expensetracker.domain.usecase

import org.junit.Assert.*
import org.junit.Test

class PriceExtractorTest {

    private val normalizer = GreekTextNormalizer()
    private val priceExtractor = PriceExtractor(normalizer)

    @Test
    fun extractTotal_withCleanGreekTotal_returnsCorrectPrice() {
        val receipt = """
            ΑΒ ΒΑΣΙΛΟΠΟΥΛΟΣ
            ΚΑΦΕΣ ΦΙΛΤΡΟΥ 1.80
            ΨΩΜΙ ΧΩΡΙΑΤΙΚΟ 1.20
            ΣΥΝΟΛΟ 3.00
            ΚΑΡΤΑ 3.00
        """.trimIndent()

        val total = priceExtractor.extractTotal(receipt)
        assertEquals("3.00", total)
    }

    @Test
    fun extractTotal_withLatinHomoglyphsAndSpaces_returnsCorrectPrice() {
        // Test total extraction with Latin homoglyph typos and extra spaces
        val receipt = """
            ΓΡΗΓΟΡΗΣ
            ΤΟΣΤ ΔΙΠΛΟ 2.50
            S Υ N Ο L Ο   2.50 €
            CASH 5.00
        """.trimIndent()

        val total = priceExtractor.extractTotal(receipt)
        assertEquals("2.50", total)
    }

    @Test
    fun extractTotal_withPriceOnNextLine_returnsCorrectPrice() {
        val receipt = """
            ΣΚΛΑΒΕΝΙΤΗΣ
            ΑΦΜ: 012345678
            ΣΥΝΟΛΟ
            14.28
            ΜΕΤΡΗΤΑ 20.00
        """.trimIndent()

        val total = priceExtractor.extractTotal(receipt)
        assertEquals("14.28", total)
    }

    @Test
    fun extractTotal_withMathematicalHeuristic_returnsCorrectPrice() {
        // Test mathematical heuristic: Total + Change = Cash Given
        val receipt = """
            ΚΑΤΑΣΤΗΜΑ ΨΙΛΙΚΩΝ
            12.35
            20.00
            7.65
        """.trimIndent()

        val total = priceExtractor.extractTotal(receipt)
        assertEquals("12.35", total)
    }

    @Test
    fun extractTotal_withVatRatesInFallback_excludesVatRates() {
        // Verify Greek VAT tax rates are excluded from fallback price matching
        val receipt = """
            ΑΡΤΟΠΟΙΕΙΟ
            24.00
            13.00
            5.40
        """.trimIndent()

        val total = priceExtractor.extractTotal(receipt)
        assertEquals("5.40", total)
    }
}
