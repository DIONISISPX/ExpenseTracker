package com.dionisispx.expensetracker.domain.usecase

import javax.inject.Inject

class StoreNameMatcher @Inject constructor(
    private val normalizer: GreekTextNormalizer
) {

    // Predefined store to category mappings
    private val seedDictionary = mapOf(
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

    // Common greek company suffixes to ignore
    private val companySuffixes = setOf(
        "ΙΚΕ", "IKE", "ΑΕ", "AE", "ΕΠΕ", "EPE", "ΟΕ", "OE", "ΕΕ", "EE",
        "ΜΙΚΕ", "MIKE", "ΜΕΠΕ", "MEPE", "ΑΕΒΕ", "AEBE"
    )

    fun matchStoreName(upperText: String, userDictionary: Map<String, String>): Pair<String, String> {
        var finalStore = ""
        var finalCategory = "Other"

        // Remove punctuation for store name matching
        val cleanTextForNames = upperText
            .replace("\"", "").replace("'", "")
            .replace("«", "").replace("»", "")
            .replace(".", " ").replace(",", " ")

        // Filter out suffixes from words
        val words = cleanTextForNames.split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .map { it.trim() }
            .filter { word ->
                val normalizedWord = normalizer.stripGreekAccents(normalizer.normalizeForFuzzy(word))
                normalizedWord !in companySuffixes
            }

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
                val normalizedPhrase = normalizer.normalizeForFuzzy(phrase)
                val normalizedStore = normalizer.normalizeForFuzzy(store)
                val isExactMatch = normalizedPhrase.replace(" ", "") == normalizedStore.replace(" ", "")
                val score = if (isExactMatch) 1.0 else normalizer.similarity(normalizedPhrase, normalizedStore)
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
                    val stripped = normalizer.stripGreekAccents(line)
                    if (ignoreTokens.none { stripped.contains(it) }) {
                        finalStore = line
                        break
                    }
                }
            }
        }

        return Pair(finalStore, finalCategory)
    }
}
