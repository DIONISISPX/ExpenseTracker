package com.dionisispx.expensetracker.domain.usecase

import java.util.Locale
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.round

class PriceExtractor @Inject constructor(
    private val normalizer: GreekTextNormalizer
) {

    // Matches bare price patterns
    private val priceRegex = Regex("(?<!\\d)\\d+\\s*[.,]\\s*\\d{2}(?!\\d)")

    // Matches prices with euro symbol
    private val euroPriceRegex = Regex("€\\s*(\\d+\\s*[.,]\\s*\\d{2})(?!\\d)")

    // Matches greek total keyword
    private val greekTotalRegex = Regex("[ΣS][ΥYU][ΝN][ΟO0][ΛL][ΟO0]")

    // Matches greek subtotal keyword
    private val greekSubtotalRegex = Regex("[ΜM][ΕE][ΡRP][ΙI][ΚK][ΟO0]")

    // Matches english total keyword
    private val totalLatinRegex = Regex("\\bTOTAL\\b")

    // Matches english subtotal keyword
    private val subtotalLatinRegex = Regex("\\b(SUBTOTAL|SUB\\s*TOTAL)\\b")

    // Matches greek payable keyword
    private val payableRegex = Regex("ΠΛΗΡΩΤ[ΕE][ΟO0]")

    // Words indicating payment methods
    private val paymentKeywords = listOf(
        "ΜΕΤΡΗΤΑ", "ΚΑΡΤΑ", "ΠΙΣΤΩΤΙΚΗ", "ΠΛΗΡΩΜΗ",
        "ΡΕΣΤΑ", "CHANGE", "CARD", "CASH", "VISA", "MASTERCARD"
    )

    // Words indicating metadata lines
    private val metadataKeywords = listOf(
        "ΤΗΛ", "ΑΦΜ", "ΔΟΥ", "ΠΟΣΟΤΗΤΑ", "QTY", "QUANTITY"
    )

    // Patterns for dates and identifiers
    private val noisePatterns = listOf(
        Regex("#\\d{3,}"),
        Regex("\\d{2}[/.]\\d{2}[/.]\\d{2,4}"),
        Regex("\\d{10,}"),
        Regex("\\d{2}:\\d{2}")
    )

    fun extractTotal(upperText: String): String {
        val lines = upperText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) return ""

        val totalPrices = mutableListOf<Double>()
        val subtotalPrices = mutableListOf<Double>()

        // Scan lines for total and subtotal keywords
        for (i in lines.indices) {
            val stripped = normalizer.stripGreekAccents(lines[i])
            if (paymentKeywords.any { stripped.contains(it) }) continue

            val hasTotalWord = greekTotalRegex.containsMatchIn(stripped)
                    || totalLatinRegex.containsMatchIn(stripped)
                    || payableRegex.containsMatchIn(stripped)
            val hasSubtotalPrefix = greekSubtotalRegex.containsMatchIn(stripped)
                    || subtotalLatinRegex.containsMatchIn(stripped)

            if (hasTotalWord && !hasSubtotalPrefix) {
                totalPrices.addAll(findPricesNear(lines, i))
            } else if (hasTotalWord) {
                subtotalPrices.addAll(findPricesNear(lines, i))
            }
        }

        // Return highest total price found
        if (totalPrices.isNotEmpty()) {
            return formatPrice(totalPrices.maxOrNull()!!)
        }
        
        // Return highest subtotal price found
        if (subtotalPrices.isNotEmpty()) {
            return formatPrice(subtotalPrices.maxOrNull()!!)
        }

        return structuralFallback(lines)
    }

    // Extracts numeric prices surrounding a specific line index
    private fun findPricesNear(lines: List<String>, targetIndex: Int): List<Double> {
        val found = mutableListOf<Double>()
        val offsets = listOf(0, 1, 2)
        
        // Check current and next two lines
        for (offset in offsets) {
            val idx = targetIndex + offset
            if (idx !in lines.indices) continue
            val line = lines[idx]

            if (offset != 0) {
                val stripped = normalizer.stripGreekAccents(line)
                if (paymentKeywords.any { stripped.contains(it) }) continue
                if (metadataKeywords.any { stripped.contains(it) }) continue
            }

            // Extract values using regex patterns
            euroPriceRegex.findAll(line).forEach {
                it.groupValues[1].replace(" ", "").replace(",", ".").toDoubleOrNull()?.let { num -> found.add(num) }
            }
            priceRegex.findAll(line).forEach {
                it.value.replace(" ", "").replace(",", ".").toDoubleOrNull()?.let { num -> found.add(num) }
            }
        }
        return found
    }

    // Calculates total without keywords using structural heuristics
    private fun structuralFallback(lines: List<String>): String {
        data class PriceEntry(val amount: Double, val lineIndex: Int)

        val prices = mutableListOf<PriceEntry>()

        // Collect all valid prices
        for (i in lines.indices) {
            val line = lines[i]
            val stripped = normalizer.stripGreekAccents(line)
            if (paymentKeywords.any { stripped.contains(it) }) continue
            if (metadataKeywords.any { stripped.contains(it) }) continue
            if (noisePatterns.any { it.containsMatchIn(line) }) continue

            for (match in priceRegex.findAll(line)) {
                val num = match.value.replace(" ", "").replace(",", ".").toDoubleOrNull() ?: continue
                if (num <= 0.0 || isVatRate(num)) continue
                prices.add(PriceEntry(num, i))
            }
        }

        if (prices.isEmpty()) return ""

        val bottomHalfStart = lines.size / 2
        val bottomPrices = prices.filter { it.lineIndex >= bottomHalfStart }

        // Find frequently repeated amounts in bottom half
        val duplicateGroup = bottomPrices
            .groupBy { roundCents(it.amount) }
            .filter { it.value.size >= 2 }
            .maxByOrNull { it.key }

        if (duplicateGroup != null) {
            return formatPrice(duplicateGroup.key)
        }

        // Check for cash tendered pattern
        for (j in 0..prices.size - 3) {
            val a = prices[j].amount
            val b = prices[j+1].amount
            val c = prices[j+2].amount
            if (doublesEqual(a + c, b) && b >= a) {
                return formatPrice(a)
            }
        }

        // Fallback to the highest price in bottom half
        val best = (bottomPrices.ifEmpty { prices }).maxByOrNull { it.amount }
        return if (best != null) formatPrice(best.amount) else ""
    }

    // Checks if number is a standard greek tax rate
    private fun isVatRate(num: Double): Boolean {
        return doublesEqual(num, 6.0) || doublesEqual(num, 13.0) || doublesEqual(num, 24.0)
    }

    // Validates equality for doubles using a threshold
    private fun doublesEqual(a: Double, b: Double): Boolean {
        return abs(a - b) < 0.005
    }

    // Rounds value to exactly two decimal places
    private fun roundCents(value: Double): Double {
        return round(value * 100) / 100.0
    }

    // Formats double as standard price string
    private fun formatPrice(value: Double): String {
        return String.format(Locale.US, "%.2f", value)
    }
}
