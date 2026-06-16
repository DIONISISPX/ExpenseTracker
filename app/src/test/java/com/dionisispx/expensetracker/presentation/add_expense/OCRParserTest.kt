package com.dionisispx.expensetracker.presentation.add_expense

import org.junit.Assert.assertEquals
import org.junit.Test

class OCRParserTest {

    @Test
    fun testReceiptWithQuantityAndTotal() {
        val receiptText = """
            ΕΙΔΙΚΟ ΦΟΡΟΛΟΓΙΚΟ ΔΕΛΤΙΟ - ΕΝΑΡΞΗ
            JACKAROO
            RED MEAT
            ΑΦΜ: 802065769 - ΔΟΥ: ΚΕΦΟΔΕ ΑΤΤΙΚΗΣ
            Απόδειξη Λιανικής Πώλησης
            Αρ. Παρ. : 417420
            Ημερομηνία - Ώρα : 14/4/2026-22:40:20
            Double Cheese 1,000 X 3,90 2,98 13%
            Hot Honey sauce 1,000 X 0,90 0,69 13%
            Σύν. Ποσότητας: 88,00
            Σύνολο: 35,11
            Κάρτα: 35,10
            Ρέστα: 0,00
        """.trimIndent()

        val (store, amount, category) = extractDataFromText(receiptText, emptyMap())

        assertEquals("JACKAROO", store)
        assertEquals("35.11", amount)
        assertEquals("Food & Drink", category)
    }

    @Test
    fun testIKEAtTopDoesNotMatchNike() {
        val receiptText = """
            JACKAROO Ι.Κ.Ε.
            RED MEAT
            Σύνολο: 12,50
        """.trimIndent()

        val (store, amount, category) = extractDataFromText(receiptText, emptyMap())

        assertEquals("JACKAROO", store)
        assertEquals("12.50", amount)
        assertEquals("Food & Drink", category)
    }

    @Test
    fun testLatinIKEAtTopDoesNotMatchNike() {
        val receiptText = """
            JACKAROO IKE
            RED MEAT
            Σύνολο: 12,50
        """.trimIndent()

        val (store, amount, category) = extractDataFromText(receiptText, emptyMap())

        assertEquals("JACKAROO", store)
        assertEquals("12.50", amount)
        assertEquals("Food & Drink", category)
    }

    @Test
    fun testOnlyIkeAtTopDoesNotMatchNike() {
        val receiptText = """
            ΙΚΕ
            Σύνολο: 5,00
        """.trimIndent()

        val (store, amount, category) = extractDataFromText(receiptText, emptyMap())

        // Since "ΙΚΕ" is filtered out, no store is identified
        assertEquals("", store)
        assertEquals("5.00", amount)
    }
}
