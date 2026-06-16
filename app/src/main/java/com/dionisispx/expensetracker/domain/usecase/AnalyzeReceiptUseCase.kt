package com.dionisispx.expensetracker.domain.usecase

import com.dionisispx.expensetracker.domain.model.ReceiptData
import javax.inject.Inject
import kotlin.math.roundToInt

class AnalyzeReceiptUseCase @Inject constructor() {

    operator fun invoke(text: String, userDictionary: Map<String, String>): ReceiptData {
        var finalStore = ""
        var finalAmount = ""
        var finalCategory = "Other"

        val upperText = text.uppercase()

        // Strip quotes, brackets, and replace dots/commas with spaces for clean word boundary checks
        val cleanTextForNames = upperText
            .replace("\"", "")
            .replace("'", "")
            .replace("«", "")
            .replace("»", "")
            .replace(".", " ")
            .replace(",", " ")

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

        // Seed dictionary
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
            "ΣΤΑΣΥ" to "Transport & Fuel",
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

        // Combine dictionaries
        val combinedDictionary = seedDictionary + userDictionary

        // Store extraction using multi-word phrase matching
        var bestMatchScore = 0.0

        val phrases = mutableListOf<String>()
        for (i in words.indices) {
            phrases.add(words[i])
            if (i < words.size - 1) phrases.add("${words[i]} ${words[i+1]}")
            if (i < words.size - 2) phrases.add("${words[i]} ${words[i+1]} ${words[i+2]}")
            if (i < words.size - 3) phrases.add("${words[i]} ${words[i+1]} ${words[i+2]} ${words[i+3]}")
        }

        for (phrase in phrases) {
            if (phrase.length < 3) continue

            for ((store, category) in combinedDictionary) {

                val normalizedPhrase = normalizeForFuzzy(phrase)
                val normalizedStore = normalizeForFuzzy(store)

                // Check if exact match otherwise calculate fuzzy score
                val isExactMatch = normalizedPhrase.replace(" ", "") == normalizedStore.replace(" ", "")
                val score = if (isExactMatch) 1.0 else similarity(normalizedPhrase, normalizedStore)

                // Only update if new score is higher than best score
                if (score > 0.70 && score > bestMatchScore) {
                    bestMatchScore = score
                    finalStore = store
                    finalCategory = category
                }
            }
        }

        // Smart amount extraction
        val lines = upperText.lines().map { it.trim() }.filter { it.isNotEmpty() }

        // Negative lookbehinds and lookaheads ensure we do not match glued digits
        val priceRegex = Regex("(?<!\\d)\\d+[.,]\\d{2}(?!\\d)")

        val totalKeywords = listOf(
            "ΣΥΝΟΛΟ", "ΤΕΛΙΚΟ ΣΥΝΟΛΟ", "ΜΕΡΙΚΟ ΣΥΝΟΛΟ", "ΣΥΝΟΛΟ €", "TOTAL"
        )

        val ignoreKeywords = listOf(
            "ΜΕΤΡΗΤΑ", "ΚΑΡΤΑ", "ΠΙΣΤΩΤΙΚΗ ΚΑΡΤΑ", "ΠΛΗΡΩΜΗ ΜΕ ΚΑΡΤΑ",
            "ΑΜΕΣΗ ΚΑΡΤΑ", "ΚΑΡΤΑ-1", "Π. ΚΑΡΤΑ", "ΠΙΣΤ. ΚΑΡΤΑ",
            "ΡΕΣΤΑ", "CHANGE", "ΤΗΛ", "ΑΦΜ", "ΔΟΥ", "CARD",
            "ΠΟΣΟΤΗΤΑ", "ΠΟΣΟΤΗΤΑΣ", "ΠΟΣΟΤ.", "QTY", "QUANTITY"
        )

        for (i in lines.indices) {
            val line = lines[i]

            val cleanLine = stripGreekAccents(line)
            val normalizedLine = cleanLine.replace(" ", "").replace("0", "Ο").replace("1", "Ι")

            val containsTotal = totalKeywords.any { cleanLine.contains(it) || normalizedLine.contains(it.replace(" ", "")) }
            // Ignore keywords only if they appear WITHOUT the word "TOTAL" (fix for "TOTAL CARD")
            val containsIgnore = ignoreKeywords.any {
                (cleanLine.contains(it) || normalizedLine.contains(it.replace(" ", ""))) && !containsTotal
            }

            if (containsTotal && !containsIgnore) {
                var matches = priceRegex.findAll(line).toList()

                if (matches.isEmpty() && i + 1 < lines.size) {
                    matches = priceRegex.findAll(lines[i + 1]).toList()
                }

                if (matches.isNotEmpty()) {
                    finalAmount = matches.last().value.replace(",", ".").replace("-", ".")
                    break
                }
            }
        }

        // Safe fallback using mathematical triangulation
        if (finalAmount.isEmpty()) {
            val validNumbers = mutableListOf<Double>()

            for (line in lines) {
                val cleanLine = stripGreekAccents(line)
                val normalizedLine = cleanLine.replace(" ", "").replace("0", "Ο")
                val containsIgnore = ignoreKeywords.any { cleanLine.contains(it) || normalizedLine.contains(it.replace(" ", "")) }

                // Exclude lines with explicit percentages or item multipliers
                if (containsIgnore || cleanLine.contains("X") || cleanLine.contains("Χ") || cleanLine.contains("%")) continue

                val matches = priceRegex.findAll(line)
                for (match in matches) {
                    val normalizedStr = match.value.replace(",", ".").replace("-", ".")
                    val num = normalizedStr.toDoubleOrNull() ?: 0.0

                    // Hard exclude greek VAT rates and zero
                    if (num != 13.00 && num != 24.00 && num != 6.00 && num != 0.0) {
                        validNumbers.add(num)
                    }
                }
            }

            if (validNumbers.isNotEmpty()) {
                // Analyze the last 4 numbers as they usually contain the total block
                val tail = validNumbers.takeLast(4)
                var foundByMath = false

                // Scenario A: Card payment where amount appears twice
                for (i in 0 until tail.size - 1) {
                    if (tail[i] == tail[i+1]) {
                        finalAmount = String.format(java.util.Locale.US, "%.2f", tail[i])
                        foundByMath = true
                        break
                    }
                }

                // Scenario B: Cash payment where total plus change is cash given
                if (!foundByMath && tail.size >= 3) {
                    val a = tail[tail.size - 3]
                    val b = tail[tail.size - 2]
                    val c = tail[tail.size - 1]

                    // Floating point math check to avoid rounding errors
                    val sumAC = ((a + c) * 100).roundToInt() / 100.0

                    if (sumAC == b) {
                        finalAmount = String.format(java.util.Locale.US, "%.2f", a)
                        foundByMath = true
                    }
                }

                // Scenario C: Fallback to max number
                if (!foundByMath) {
                    val maxNum = validNumbers.maxOrNull() ?: 0.0
                    finalAmount = String.format(java.util.Locale.US, "%.2f", maxNum)
                }
            }
        }

        return ReceiptData(finalStore, finalAmount, finalCategory)
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
            .replace("Ά", "Α")
            .replace("Έ", "Ε")
            .replace("Ή", "Η")
            .replace("Ί", "Ι")
            .replace("Ό", "Ο")
            .replace("Ύ", "Υ")
            .replace("Ώ", "Ω")
            .replace("Ϊ", "Ι")
            .replace("Ϋ", "Υ")
    }

    private fun normalizeForFuzzy(input: String): String {
        return stripGreekAccents(input.uppercase())
            .replace("V", "Ψ")
            .replace("S", "Σ")
            .replace("C", "Σ")
            .replace("E", "Ε")
            .replace("N", "Ν")
            .replace("I", "Ι")
            .replace("O", "Ο")
            .replace("P", "Ρ")
            .replace("A", "Α")
            .replace("T", "Τ")
            .replace("H", "Η")
            .replace("K", "Κ")
            .replace("M", "Μ")
            .replace("X", "Χ")
            .replace("Y", "Υ")
            .replace("Z", "Ζ")
            .replace("B", "Β")
            .replace("U", "Υ")
    }
}
