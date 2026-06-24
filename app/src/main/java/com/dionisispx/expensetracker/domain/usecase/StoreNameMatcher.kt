package com.dionisispx.expensetracker.domain.usecase

import com.dionisispx.expensetracker.domain.model.ExpenseCategory
import javax.inject.Inject

class StoreNameMatcher @Inject constructor(
    private val normalizer: GreekTextNormalizer
) {

    // Predefined store to category mappings
    private val seedDictionary = mapOf(
        "ΣΚΛΑΒΕΝΙΤΗΣ" to ExpenseCategory.GROCERIES,
        "ΓΑΛΑΞΙΑΣ" to ExpenseCategory.GROCERIES,
        "ΑΒ ΒΑΣΙΛΟΠΟΥΛΟΣ" to ExpenseCategory.GROCERIES,
        "ΜΑΣΟΥΤΗΣ" to ExpenseCategory.GROCERIES,
        "ΚΡΗΤΙΚΟΣ" to ExpenseCategory.GROCERIES,
        "LIDL" to ExpenseCategory.GROCERIES,

        "ZARA" to ExpenseCategory.SHOPPING,
        "H&M" to ExpenseCategory.SHOPPING,
        "PULL&BEAR" to ExpenseCategory.SHOPPING,
        "BERSHKA" to ExpenseCategory.SHOPPING,
        "NIKE" to ExpenseCategory.SHOPPING,
        "ADIDAS" to ExpenseCategory.SHOPPING,
        "COSMOS SPORT" to ExpenseCategory.SHOPPING,
        "JD" to ExpenseCategory.SHOPPING,
        "PLAISIO" to ExpenseCategory.SHOPPING,
        "PUBLIC" to ExpenseCategory.SHOPPING,
        "ΚΩΤΣΟΒΟΛΟΣ" to ExpenseCategory.SHOPPING,
        "ΓΕΡΜΑΝΟΣ" to ExpenseCategory.SHOPPING,

        "VILLAGE" to ExpenseCategory.ENTERTAINMENT,
        "OPTIONS" to ExpenseCategory.ENTERTAINMENT,

        "Ο.Α.Σ.Α." to ExpenseCategory.TRANSPORT_FUEL,
        "ΣΤΑ.ΣΥ." to ExpenseCategory.TRANSPORT_FUEL,
        "EKO" to ExpenseCategory.TRANSPORT_FUEL,
        "BP" to ExpenseCategory.TRANSPORT_FUEL,
        "SHELL" to ExpenseCategory.TRANSPORT_FUEL,
        "ETEKA" to ExpenseCategory.TRANSPORT_FUEL,
        "REVOIL" to ExpenseCategory.TRANSPORT_FUEL,
        "ΕΛΙΝ" to ExpenseCategory.TRANSPORT_FUEL,
        "AVIN" to ExpenseCategory.TRANSPORT_FUEL,

        "ΓΡΗΓΟΡΗΣ" to ExpenseCategory.FOOD_DRINK,
        "EVEREST" to ExpenseCategory.FOOD_DRINK,
        "COFFEE ISLAND" to ExpenseCategory.FOOD_DRINK,
        "IL TOTO" to ExpenseCategory.FOOD_DRINK,
        "STARBUCKS" to ExpenseCategory.FOOD_DRINK,
        "COFFEE BERRY" to ExpenseCategory.FOOD_DRINK,
        "MCDONALD'S" to ExpenseCategory.FOOD_DRINK,
        "JACKAROO" to ExpenseCategory.FOOD_DRINK,
        "KFC" to ExpenseCategory.FOOD_DRINK,
        "PIZZA FAN" to ExpenseCategory.FOOD_DRINK,
        "DOMINO'S" to ExpenseCategory.FOOD_DRINK,
        "PIZZA HUT" to ExpenseCategory.FOOD_DRINK,
        "GOODY'S" to ExpenseCategory.FOOD_DRINK,
        "BREAD FACTORY" to ExpenseCategory.FOOD_DRINK,
        "ΣΤΕΡΓΙΟΥ" to ExpenseCategory.FOOD_DRINK,
        "NANOU" to ExpenseCategory.FOOD_DRINK,
        "EFOOD" to ExpenseCategory.FOOD_DRINK,
        "WOLT" to ExpenseCategory.FOOD_DRINK,
        "BOX" to ExpenseCategory.FOOD_DRINK,

        "YAVA" to ExpenseCategory.HEALTH_FITNESS,
        "PLANET FITNESS" to ExpenseCategory.HEALTH_FITNESS,
        "ALTERLIFE" to ExpenseCategory.HEALTH_FITNESS,

        "COSMOTE" to ExpenseCategory.BILLS_UTILITIES,
        "NOVA" to ExpenseCategory.BILLS_UTILITIES,
        "VODAFONE" to ExpenseCategory.BILLS_UTILITIES,
        "INALAN" to ExpenseCategory.BILLS_UTILITIES,
        "ΔΕΗ" to ExpenseCategory.BILLS_UTILITIES,
        "PROTERGIA" to ExpenseCategory.BILLS_UTILITIES,
        "ΕΥΔΑΠ" to ExpenseCategory.BILLS_UTILITIES
    )

    // Common Greek company suffixes to ignore
    private val companySuffixes = setOf(
        "ΙΚΕ", "IKE", "ΑΕ", "AE", "ΕΠΕ", "EPE", "ΟΕ", "OE", "ΕΕ", "EE",
        "ΜΙΚΕ", "MIKE", "ΜΕΠΕ", "MEPE", "ΑΕΒΕ", "AEBE"
    )

    fun matchStoreName(upperText: String, userDictionary: Map<String, ExpenseCategory>): Pair<String, ExpenseCategory> {
        var finalStore = ""
        var finalCategory = ExpenseCategory.OTHER

        // 1. Remove punctuation for store name matching
        val cleanTextForNames = upperText
            .replace("\"", "").replace("'", "")
            .replace("«", "").replace("»", "")
            .replace(".", " ").replace(",", " ")

        // 2. Filter out suffixes from words
        val words = cleanTextForNames.split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .map { it.trim() }
            .filter { word ->
                val normalizedWord = normalizer.stripGreekAccents(normalizer.normalizeForFuzzy(word))
                normalizedWord !in companySuffixes
            }

        // 3. Merge seed data with user dictionary
        val combinedDictionary = seedDictionary + userDictionary

        var bestMatchScore = 0.0
        val phrases = mutableListOf<String>()
        
        // 4. Build n-gram combinations up to four words
        for (i in words.indices) {
            phrases.add(words[i])
            if (i < words.size - 1) phrases.add("${words[i]} ${words[i + 1]}")
            if (i < words.size - 2) phrases.add("${words[i]} ${words[i + 1]} ${words[i + 2]}")
            if (i < words.size - 3) phrases.add("${words[i]} ${words[i + 1]} ${words[i + 2]} ${words[i + 3]}")
        }

        // 5. Compare generated phrases against dictionary
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

        // 6. Use top text line as fallback if no store is matched
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
