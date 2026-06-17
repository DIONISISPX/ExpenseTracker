package com.dionisispx.expensetracker.domain.usecase

import com.dionisispx.expensetracker.domain.model.ReceiptData
import java.util.Locale
import javax.inject.Inject
import kotlin.math.abs

class AnalyzeReceiptUseCase @Inject constructor() {

    // Bare price pattern bounded by non-digit context
    private val priceRegex = Regex("(?<!\\d)\\d+\\s*[.,]\\s*\\d{2}(?!\\d)")

    // Euro-prefixed price for higher confidence matches
    private val euroPriceRegex = Regex("€\\s*(\\d+\\s*[.,]\\s*\\d{2})(?!\\d)")

    // OCR-tolerant pattern for Greek total allowing common letter swaps
    private val greekTotalRegex = Regex("[ΣS][ΥYU][ΝN][ΟO0][ΛL][ΟO0]")

    // OCR-tolerant pattern for Greek subtotal prefix
    private val greekSubtotalRegex = Regex("[ΜM][ΕE][ΡRP][ΙI][ΚK][ΟO0]")

    // Latin total keyword bounded by word edges
    private val totalLatinRegex = Regex("\\bTOTAL\\b")

    // Latin subtotal variants
    private val subtotalLatinRegex = Regex("\\b(SUBTOTAL|SUB\\s*TOTAL)\\b")

    // Greek payable amount keyword
    private val payableRegex = Regex("ΠΛΗΡΩΤ[ΕE][ΟO0]")

    // Payment method lines should never be treated as totals
    private val paymentKeywords = listOf(
        "ΜΕΤΡΗΤΑ", "ΚΑΡΤΑ", "ΠΙΣΤΩΤΙΚΗ", "ΠΛΗΡΩΜΗ",
        "ΡΕΣΤΑ", "CHANGE", "CARD", "CASH", "VISA", "MASTERCARD"
    )

    // Metadata lines that carry non-price numbers
    private val metadataKeywords = listOf(
        "ΤΗΛ", "ΑΦΜ", "ΔΟΥ", "ΠΟΣΟΤΗΤΑ", "QTY", "QUANTITY"
    )

    // Noise patterns: dates, order numbers, phone numbers, timestamps
    private val noisePatterns = listOf(
        Regex("#\\d{3,}"),
        Regex("\\d{2}[/.]\\d{2}[/.]\\d{2,4}"),
        Regex("\\d{10,}"),
        Regex("\\d{2}:\\d{2}")
    )

    operator fun invoke(text: String, userDictionary: Map<String, String>): ReceiptData {
        var finalStore = ""
        var finalAmount: String
        var finalCategory = "Other"

        val upperText = text.uppercase()

        // Clean text for store name matching by removing punctuation
        val cleanTextForNames = upperText
            .replace("\"", "").replace("'", "")
            .replace("«", "").replace("»", "")
            .replace(".", " ").replace(",", " ")

        val companySuffixes = setOf(
            "ΙΚΕ", "IKE", "ΑΕ", "AE", "ΕΠΕ", "EPE", "ΟΕ", "OE", "ΕΕ", "EE",
            "ΜΙΚΕ", "MIKE", "ΜΕΠΕ", "MEPE", "ΑΕΒΕ", "AEBE"
        )

        val words = cleanTextForNames.split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .map { it.trim() }
            .filter { word ->
                val normalizedWord = stripGreekAccents(normalizeForFuzzy(word))
                normalizedWord !in companySuffixes
            }

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

            "Ο.Α.Σ.Α." to "Transport & Fuel",
            "ΣΤΑ.ΣΥ." to "Transport & Fuel",
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
            "EFOOD" to "Food & Drink",
            "WOLT" to "Food & Drink",
            "BOX" to "Food & Drink",

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

        val combinedDictionary = seedDictionary + userDictionary

        // Build n-gram phrases for fuzzy store name matching
        var bestMatchScore = 0.0
        val phrases = mutableListOf<String>()
        for (i in words.indices) {
            phrases.add(words[i])
            if (i < words.size - 1) phrases.add("${words[i]} ${words[i + 1]}")
            if (i < words.size - 2) phrases.add("${words[i]} ${words[i + 1]} ${words[i + 2]}")
            if (i < words.size - 3) phrases.add("${words[i]} ${words[i + 1]} ${words[i + 2]} ${words[i + 3]}")
        }

        for (phrase in phrases) {
            if (phrase.length < 3) continue
            for ((store, category) in combinedDictionary) {
                val normalizedPhrase = normalizeForFuzzy(phrase)
                val normalizedStore = normalizeForFuzzy(store)
                val isExactMatch = normalizedPhrase.replace(" ", "") == normalizedStore.replace(" ", "")
                val score = if (isExactMatch) 1.0 else similarity(normalizedPhrase, normalizedStore)
                if (score > 0.70 && score > bestMatchScore) {
                    bestMatchScore = score
                    finalStore = store
                    finalCategory = category
                }
            }
        }

        if (finalStore.isEmpty()) {
            val ignoreTokens = listOf("ΑΠΟΔΕΙΞΗ", "ΕΙΔΙΚΟ", "ΦΟΡΟΛΟΓΙΚΟ", "ΔΕΛΤΙΟ", "ΕΝΑΡΞΗ", "ΛΗΞΗ", "ΝΟΜΙΜΗ", "ΠΩΛΗΣΗΣ", "ΛΙΑΝΙΚΗΣ", "ΕΚΔΟΣΗΣ")
            for (line in upperText.lines().map { it.trim() }) {
                if (line.length >= 3 && line.any { it.isLetter() }) {
                    val stripped = stripGreekAccents(line)
                    if (ignoreTokens.none { stripped.contains(it) }) {
                        finalStore = line
                        break
                    }
                }
            }
        }

        finalAmount = extractTotal(upperText)

        return ReceiptData(finalStore, finalAmount, finalCategory)
    }

    // Runs keyword scan then falls back to structural heuristics
    private fun extractTotal(upperText: String): String {
        val lines = upperText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) return ""

        val totalPrices = mutableListOf<Double>()
        val subtotalPrices = mutableListOf<Double>()

        for (i in lines.indices) {
            val stripped = stripGreekAccents(lines[i])
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

        if (totalPrices.isNotEmpty()) {
            return formatPrice(totalPrices.maxOrNull()!!)
        }
        if (subtotalPrices.isNotEmpty()) {
            return formatPrice(subtotalPrices.maxOrNull()!!)
        }

        return structuralFallback(lines)
    }

    // Extracts all prices near a given line index
    private fun findPricesNear(lines: List<String>, targetIndex: Int): List<Double> {
        val found = mutableListOf<Double>()
        // Labels usually precede their values or are on the same line.
        // We only check the current line and the next two lines.
        val offsets = listOf(0, 1, 2)
        for (offset in offsets) {
            val idx = targetIndex + offset
            if (idx !in lines.indices) continue
            val line = lines[idx]

            // Only validate adjacent lines against payment and metadata keywords
            if (offset != 0) {
                val stripped = stripGreekAccents(line)
                if (paymentKeywords.any { stripped.contains(it) }) continue
                if (metadataKeywords.any { stripped.contains(it) }) continue
            }

            // Extract all possible prices
            euroPriceRegex.findAll(line).forEach {
                it.groupValues[1].replace(" ", "").replace(",", ".").toDoubleOrNull()?.let { num -> found.add(num) }
            }
            priceRegex.findAll(line).forEach {
                it.value.replace(" ", "").replace(",", ".").toDoubleOrNull()?.let { num -> found.add(num) }
            }
        }
        return found
    }

    // Fallback when no keywords are found, using position and duplicate heuristics
    private fun structuralFallback(lines: List<String>): String {
        data class PriceEntry(val amount: Double, val lineIndex: Int)

        val prices = mutableListOf<PriceEntry>()

        for (i in lines.indices) {
            val line = lines[i]
            val stripped = stripGreekAccents(line)
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

        // Duplicate in bottom half usually means subtotal echoed as total
        val duplicateGroup = bottomPrices
            .groupBy { roundCents(it.amount) }
            .filter { it.value.size >= 2 }
            .maxByOrNull { it.key }

        if (duplicateGroup != null) {
            return formatPrice(duplicateGroup.key)
        }

        // Cash payment pattern: total + change = cash tendered
        for (j in 0..prices.size - 3) {
            val a = prices[j].amount
            val b = prices[j+1].amount
            val c = prices[j+2].amount
            if (doublesEqual(a + c, b) && b >= a) {
                return formatPrice(a)
            }
        }

        // Largest value in the bottom half, or overall largest as last resort
        val best = (bottomPrices.ifEmpty { prices }).maxByOrNull { it.amount }
        return if (best != null) formatPrice(best.amount) else ""
    }

    // Returns true for common Greek VAT rate values
    private fun isVatRate(num: Double): Boolean {
        return doublesEqual(num, 6.0) || doublesEqual(num, 13.0) || doublesEqual(num, 24.0)
    }

    // Compares doubles with cent-level tolerance to avoid floating point drift
    private fun doublesEqual(a: Double, b: Double): Boolean {
        return abs(a - b) < 0.005
    }

    private fun roundCents(value: Double): Double {
        return Math.round(value * 100) / 100.0
    }

    private fun formatPrice(value: Double): String {
        return String.format(Locale.US, "%.2f", value)
    }

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

    private fun stripGreekAccents(input: String): String {
        return input
            .replace("Ά", "Α").replace("Έ", "Ε").replace("Ή", "Η")
            .replace("Ί", "Ι").replace("Ό", "Ο").replace("Ύ", "Υ")
            .replace("Ώ", "Ω").replace("Ϊ", "Ι").replace("Ϋ", "Υ")
            .replace("ά", "α").replace("έ", "ε").replace("ή", "η")
            .replace("ί", "ι").replace("ό", "ο").replace("ύ", "υ")
            .replace("ώ", "ω").replace("ϊ", "ι").replace("ϋ", "υ")
            .replace("ΐ", "ι").replace("ΰ", "υ")
    }

    private fun normalizeForFuzzy(input: String): String {
        return stripGreekAccents(input.uppercase())
            .replace(".", "").replace(",", "").replace("-", "")
            .replace("V", "Ψ").replace("S", "Σ").replace("C", "Σ")
            .replace("E", "Ε").replace("N", "Ν").replace("I", "Ι")
            .replace("O", "Ο").replace("P", "Ρ").replace("A", "Α")
            .replace("T", "Τ").replace("H", "Η").replace("K", "Κ")
            .replace("M", "Μ").replace("X", "Χ").replace("Y", "Υ")
            .replace("Z", "Ζ").replace("B", "Β").replace("U", "Υ")
    }
}

