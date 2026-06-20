package com.dionisispx.expensetracker.domain.usecase

import com.dionisispx.expensetracker.domain.model.ReceiptData
import java.util.Locale
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.round

class AnalyzeReceiptUseCase @Inject constructor() {

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

    // Analyzes raw receipt text and returns extracted data
    operator fun invoke(text: String, userDictionary: Map<String, String>): ReceiptData {
        var finalStore = ""
        var finalAmount: String
        var finalCategory = "Other"

        val upperText = text.uppercase()

        // Remove punctuation for store name matching
        val cleanTextForNames = upperText
            .replace("\"", "").replace("'", "")
            .replace("«", "").replace("»", "")
            .replace(".", " ").replace(",", " ")

        // Common greek company suffixes to ignore
        val companySuffixes = setOf(
            "ΙΚΕ", "IKE", "ΑΕ", "AE", "ΕΠΕ", "EPE", "ΟΕ", "OE", "ΕΕ", "EE",
            "ΜΙΚΕ", "MIKE", "ΜΕΠΕ", "MEPE", "ΑΕΒΕ", "AEBE"
        )

        // Filter out suffixes from words
        val words = cleanTextForNames.split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .map { it.trim() }
            .filter { word ->
                val normalizedWord = stripGreekAccents(normalizeForFuzzy(word))
                normalizedWord !in companySuffixes
            }

        // Predefined store to category mappings
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

        // Merge seed data with user dictionary
        val combinedDictionary = seedDictionary + userDictionary

        var bestMatchScore = 0.0
        val phrases = mutableListOf<String>()
        
        // Build n-gram combinations up to four words
        for (i in words.indices) {
            phrases.add(words[i])
            if (i < words.size - 1) phrases.add("${words[i]} ${words[i + 1]}")
            if (i < words.size - 2) phrases.add("${words[i]} ${words[i + 1]} ${words[i + 2]}")
            if (i < words.size - 3) phrases.add("${words[i]} ${words[i + 1]} ${words[i + 2]} ${words[i + 3]}")
        }

        // Compare generated phrases against dictionary
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

        // Use top text line as fallback if no store is matched
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

        // Process total amount from text
        finalAmount = extractTotal(upperText)

        return ReceiptData(finalStore, finalAmount, finalCategory)
    }

    // Identifies the final total amount from the text
    private fun extractTotal(upperText: String): String {
        val lines = upperText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) return ""

        val totalPrices = mutableListOf<Double>()
        val subtotalPrices = mutableListOf<Double>()

        // Scan lines for total and subtotal keywords
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
                val stripped = stripGreekAccents(line)
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

    // Calculates text similarity ratio using levenshtein distance
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

    // Computes edit distance between two strings
    private fun levenshteinDistance(lhs: CharSequence, rhs: CharSequence): Int {
        val len0 = lhs.length + 1
        val len1 = rhs.length + 1
        var cost = IntArray(len0)
        var newcost = IntArray(len0)
        
        // Initialize base costs
        for (i in 0 until len0) cost[i] = i
        
        // Calculate matrix
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

    // Removes all accent marks from greek letters
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

    // Simplifies string to base latin characters for fuzzy matching
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
