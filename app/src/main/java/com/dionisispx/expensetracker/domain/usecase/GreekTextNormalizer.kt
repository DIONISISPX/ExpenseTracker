package com.dionisispx.expensetracker.domain.usecase

import javax.inject.Inject

class GreekTextNormalizer @Inject constructor() {

    // Removes all accent marks from greek letters
    fun stripGreekAccents(input: String): String {
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
    fun normalizeForFuzzy(input: String): String {
        return stripGreekAccents(input.uppercase())
            .replace(".", "").replace(",", "").replace("-", "")
            .replace("V", "Ψ").replace("S", "Σ").replace("C", "Σ")
            .replace("E", "Ε").replace("N", "Ν").replace("I", "Ι")
            .replace("O", "Ο").replace("P", "Ρ").replace("A", "Α")
            .replace("T", "Τ").replace("H", "Η").replace("K", "Κ")
            .replace("M", "Μ").replace("X", "Χ").replace("Y", "Υ")
            .replace("Z", "Ζ").replace("B", "Β").replace("U", "Υ")
    }

    // Calculates text similarity ratio using levenshtein distance
    fun similarity(s1: String, s2: String): Double {
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
}
