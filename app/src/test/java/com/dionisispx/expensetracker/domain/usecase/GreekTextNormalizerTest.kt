package com.dionisispx.expensetracker.domain.usecase

import org.junit.Assert.*
import org.junit.Test

class GreekTextNormalizerTest {

    private val normalizer = GreekTextNormalizer()

    @Test
    fun stripGreekAccents_removesAccentsCorrectly() {
        val input = "Άρτος, Έλαιο, Ήλιος, Φεγγάρι, Όριο, Νερό, Ώρα"
        val expected = "Αρτος, Ελαιο, Ηλιος, Φεγγαρι, Οριο, Νερο, Ωρα"
        assertEquals(expected, normalizer.stripGreekAccents(input))
    }

    @Test
    fun normalizeForFuzzy_convertsHomoglyphsToGreek() {
        // Test Latin to Greek homoglyph conversion
        val ocrTotalWithLatin = "SENO" 
        val expected = "ΣΕΝΟ"
        
        assertEquals(expected, normalizer.normalizeForFuzzy(ocrTotalWithLatin))
    }

    @Test
    fun normalizeForFuzzy_cleansPunctuationAndConvertsCase() {
        val input = "Σ.Κ.Λ.Α.Β.Ε.Ν.Ι.Τ.Η.Σ."
        val expected = "ΣΚΛΑΒΕΝΙΤΗΣ"
        assertEquals(expected, normalizer.normalizeForFuzzy(input))
    }

    @Test
    fun normalizeForFuzzy_convertsToUppercase() {
        val input = "Σκλαβενιτης"
        val expected = "ΣΚΛΑΒΕΝΙΤΗΣ"
        assertEquals(expected, normalizer.normalizeForFuzzy(input))
    }

    @Test
    fun normalizeForFuzzy_handlesCombinedNormalization() {
        // Test normalization with accents and hyphens
        val input = "Άλφα-Βήτα"
        val expected = "ΑΛΦΑΒΗΤΑ"
        assertEquals(expected, normalizer.normalizeForFuzzy(input))
    }

    @Test
    fun similarity_calculatesCorrectEditDistanceRatio() {
        // Mismatched letter test
        val s1 = "ΣΚΛΑΒΕΝΙΤΗΣ"
        val s2 = "ΣΚΛΑΒΗΝΙΤΗΣ"
        
        val score = normalizer.similarity(s1, s2)
        assertTrue(score > 0.90)
        
        val s3 = "ΑΒ ΒΑΣΙΛΟΠΟΥΛΟΣ"
        val scoreDifferent = normalizer.similarity(s1, s3)
        assertTrue(scoreDifferent < 0.30)
    }

    @Test
    fun similarity_returnsOneForExactMatch() {
        val s1 = "ΣΚΛΑΒΕΝΙΤΗΣ"
        val score = normalizer.similarity(s1, s1)
        assertEquals(1.0, score, 0.001)
    }
}
