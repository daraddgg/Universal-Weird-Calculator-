package com.example.calculator

import java.util.regex.Pattern

data class ParsedNluQuery(
    val value: Double,
    val fromUnitKey: String,
    val category: String,
    val fromUnitDisplayName: String,
    val targetObject: ComparisonObject?,
    val isAmbiguous: Boolean,
    val errorOrPrompt: String? = null
)

object WeirdCalculatorNluParser {

    // Synonym maps for Units
    private val unitSynonyms = mapOf(
        // Length
        "mm" to ("mm" to "Length"),
        "milimeter" to ("mm" to "Length"),
        "milimeters" to ("mm" to "Length"),
        "millimeter" to ("mm" to "Length"),
        "millimeters" to ("mm" to "Length"),
        "cm" to ("cm" to "Length"),
        "centimeter" to ("cm" to "Length"),
        "centimeters" to ("cm" to "Length"),
        "m" to ("m" to "Length"),
        "meter" to ("m" to "Length"),
        "meters" to ("m" to "Length"),
        "metre" to ("m" to "Length"),
        "metres" to ("m" to "Length"),
        "km" to ("km" to "Length"),
        "kilometer" to ("km" to "Length"),
        "kilometers" to ("km" to "Length"),
        "kilometre" to ("km" to "Length"),
        "kilometres" to ("km" to "Length"),
        "in" to ("in" to "Length"),
        "inch" to ("in" to "Length"),
        "inches" to ("in" to "Length"),
        "ft" to ("ft" to "Length"),
        "foot" to ("ft" to "Length"),
        "feet" to ("ft" to "Length"),
        "yd" to ("yd" to "Length"),
        "yard" to ("yd" to "Length"),
        "yards" to ("yd" to "Length"),
        "mi" to ("mi" to "Length"),
        "mile" to ("mi" to "Length"),
        "miles" to ("mi" to "Length"),
        "nmi" to ("nmi" to "Length"),
        "nautical mile" to ("nmi" to "Length"),
        "nautical miles" to ("nmi" to "Length"),
        "au" to ("au" to "Length"),
        "astronomical unit" to ("au" to "Length"),
        "astronomical units" to ("au" to "Length"),
        "ly" to ("ly" to "Length"),
        "light year" to ("ly" to "Length"),
        "light years" to ("ly" to "Length"),

        // Weight / Mass
        "mg" to ("mg" to "Weight / Mass"),
        "milligram" to ("mg" to "Weight / Mass"),
        "milligrams" to ("mg" to "Weight / Mass"),
        "g" to ("g" to "Weight / Mass"),
        "gram" to ("g" to "Weight / Mass"),
        "grams" to ("g" to "Weight / Mass"),
        "kg" to ("kg" to "Weight / Mass"),
        "kilogram" to ("kg" to "Weight / Mass"),
        "kilograms" to ("kg" to "Weight / Mass"),
        "t" to ("t" to "Weight / Mass"),
        "ton" to ("t" to "Weight / Mass"),
        "tons" to ("t" to "Weight / Mass"),
        "tonne" to ("t" to "Weight / Mass"),
        "tonnes" to ("t" to "Weight / Mass"),
        "lb" to ("lb" to "Weight / Mass"),
        "lbs" to ("lb" to "Weight / Mass"),
        "pound" to ("lb" to "Weight / Mass"),
        "pounds" to ("lb" to "Weight / Mass"),
        "oz" to ("oz" to "Weight / Mass"),
        "ounce" to ("oz" to "Weight / Mass"),
        "ounces" to ("oz" to "Weight / Mass"),

        // Time
        "s" to ("s" to "Time"),
        "sec" to ("s" to "Time"),
        "secs" to ("s" to "Time"),
        "second" to ("s" to "Time"),
        "seconds" to ("s" to "Time"),
        "min" to ("min" to "Time"),
        "mins" to ("min" to "Time"),
        "minute" to ("min" to "Time"),
        "minutes" to ("min" to "Time"),
        "h" to ("h" to "Time"),
        "hr" to ("h" to "Time"),
        "hrs" to ("h" to "Time"),
        "hour" to ("h" to "Time"),
        "hours" to ("h" to "Time"),
        "d" to ("d" to "Time"),
        "day" to ("d" to "Time"),
        "days" to ("d" to "Time"),
        "w" to ("w" to "Time"),
        "week" to ("w" to "Time"),
        "weeks" to ("w" to "Time"),
        "mo" to ("mo" to "Time"),
        "month" to ("mo" to "Time"),
        "months" to ("mo" to "Time"),
        "yr" to ("yr" to "Time"),
        "yrs" to ("yr" to "Time"),
        "year" to ("yr" to "Time"),
        "years" to ("yr" to "Time"),

        // Speed
        "m/s" to ("m_s" to "Speed"),
        "meters per second" to ("m_s" to "Speed"),
        "km/h" to ("km_h" to "Speed"),
        "kmh" to ("km_h" to "Speed"),
        "kilometers per hour" to ("km_h" to "Speed"),
        "mph" to ("mph" to "Speed"),
        "miles per hour" to ("mph" to "Speed"),
        "knots" to ("knots" to "Speed"),

        // Energy
        "j" to ("j" to "Energy"),
        "joule" to ("j" to "Energy"),
        "joules" to ("j" to "Energy"),
        "kj" to ("kj" to "Energy"),
        "kilojoule" to ("kj" to "Energy"),
        "kilojoules" to ("kj" to "Energy"),
        "cal" to ("cal" to "Energy"),
        "calorie" to ("cal" to "Energy"),
        "calories" to ("cal" to "Energy"),
        "kcal" to ("cal" to "Energy"),
        "kwh" to ("kwh" to "Energy"),
        "kilowatt-hour" to ("kwh" to "Energy"),
        "kilowatt-hours" to ("kwh" to "Energy"),
        "kilowatthour" to ("kwh" to "Energy"),
        "kilowatthours" to ("kwh" to "Energy"),

        // Digital Storage
        "bit" to ("bit" to "Digital Storage"),
        "bits" to ("bit" to "Digital Storage"),
        "b" to ("b" to "Digital Storage"),
        "byte" to ("b" to "Digital Storage"),
        "bytes" to ("b" to "Digital Storage"),
        "kb" to ("kb" to "Digital Storage"),
        "kilobyte" to ("kb" to "Digital Storage"),
        "kilobytes" to ("kb" to "Digital Storage"),
        "mb" to ("mb" to "Digital Storage"),
        "megabyte" to ("mb" to "Digital Storage"),
        "megabytes" to ("mb" to "Digital Storage"),
        "gb" to ("gb" to "Digital Storage"),
        "gigabyte" to ("gb" to "Digital Storage"),
        "gigabytes" to ("gb" to "Digital Storage"),
        "tb" to ("tb" to "Digital Storage"),
        "terabyte" to ("tb" to "Digital Storage"),
        "terabytes" to ("tb" to "Digital Storage"),
        "pb" to ("pb" to "Digital Storage"),
        "petabyte" to ("pb" to "Digital Storage"),
        "petabytes" to ("pb" to "Digital Storage"),

        // Money / Currency
        "usd" to ("usd" to "Money"),
        "dollar" to ("usd" to "Money"),
        "dollars" to ("usd" to "Money"),
        "$" to ("usd" to "Money"),
        "eur" to ("eur" to "Money"),
        "euro" to ("eur" to "Money"),
        "euros" to ("eur" to "Money"),
        "€" to ("eur" to "Money"),
        "gbp" to ("gbp" to "Money"),
        "pound sterling" to ("gbp" to "Money"),
        "pounds sterling" to ("gbp" to "Money"),
        "£" to ("gbp" to "Money"),
        "jpy" to ("jpy" to "Money"),
        "yen" to ("jpy" to "Money"),
        "¥" to ("jpy" to "Money"),
        "cad" to ("cad" to "Money"),
        "aud" to ("aud" to "Money")
    )

    // Maps target synonym words back to precise DB ComparisonObject names
    private val targetSynonyms = mapOf(
        "cat" to "Domestic Cat",
        "cats" to "Domestic Cat",
        "kitten" to "Domestic Cat",
        "kittens" to "Domestic Cat",
        "golden retriever" to "Golden Retriever",
        "golden retrievers" to "Golden Retriever",
        "dog" to "Golden Retriever",
        "dogs" to "Golden Retriever",
        "elephant" to "African Elephant",
        "elephants" to "African Elephant",
        "whale" to "Blue Whale",
        "whales" to "Blue Whale",
        "blue whale" to "Blue Whale",
        "blue whales" to "Blue Whale",
        "cheetah" to "Cheetah Peak speed",
        "cheetahs" to "Cheetah Peak speed",
        "pencil" to "Standard HB Pencil",
        "pencils" to "Standard HB Pencil",
        "human" to "Average Human",
        "humans" to "Average Human",
        "person" to "Average Human",
        "people" to "Average Human",
        "school bus" to "Standard School Bus",
        "school buses" to "Standard School Bus",
        "bus" to "Standard School Bus",
        "buses" to "Standard School Bus",
        "football field" to "American Football Field",
        "football fields" to "American Football Field",
        "eiffel tower" to "Eiffel Tower",
        "eiffel towers" to "Eiffel Tower",
        "everest" to "Mount Everest",
        "mount everest" to "Mount Everest",
        "earth" to "Earth Circumference",
        "earth circumference" to "Earth Circumference",
        "photo" to "Standard Digital Photo",
        "photos" to "Standard Digital Photo",
        "digital photo" to "Standard Digital Photo",
        "digital photos" to "Standard Digital Photo",
        "song" to "Streaming Song",
        "songs" to "Streaming Song",
        "music" to "Streaming Song",
        "book" to "Standard PDF Book",
        "books" to "Standard PDF Book",
        "movie" to "HD Movie Stream",
        "movies" to "HD Movie Stream",
        "stream" to "HD Movie Stream",
        "streams" to "HD Movie Stream",
        "4k movie" to "4K Movie DL",
        "4k movies" to "4K Movie DL",
        "game" to "Modern AAA Video Game",
        "games" to "Modern AAA Video Game",
        "phone charge" to "Smartphone Charge",
        "phone charges" to "Smartphone Charge",
        "charge" to "Smartphone Charge",
        "charges" to "Smartphone Charge",
        "laptop" to "Laptop Screen Hour",
        "laptops" to "Laptop Screen Hour",
        "bulb" to "LED Bulb Hour",
        "bulbs" to "LED Bulb Hour",
        "light bulb" to "LED Bulb Hour",
        "light bulbs" to "LED Bulb Hour",
        "home" to "Average Home Daily Use",
        "household" to "Average Home Daily Use",
        "pizza" to "Slice of Pizza",
        "pizzas" to "Slice of Pizza",
        "slice" to "Slice of Pizza",
        "slices" to "Slice of Pizza",
        "coffee" to "Premium Cafe Latte",
        "coffees" to "Premium Cafe Latte",
        "latte" to "Premium Cafe Latte",
        "lattes" to "Premium Cafe Latte",
        "smartphone" to "Premium Smartphone",
        "smartphones" to "Premium Smartphone",
        "console" to "Next-Gen Console",
        "consoles" to "Next-Gen Console"
    )

    /**
     * Parses queries such as:
     * - "1000 meters"
     * - "500 kg in elephants"
     * - "1 TB in photos"
     * - "2 hours in heartbeats"
     * - "100 dollars in pizzas"
     * - "how many football fields is 3 km"
     * - "energy of 1 kWh in phone charges"
     */
    fun parseQuery(rawQuery: String): ParsedNluQuery {
        val queryClean = rawQuery.trim().lowercase()

        if (queryClean.isBlank()) {
            return ParsedNluQuery(
                value = 1.0,
                fromUnitKey = "m",
                category = "Length",
                fromUnitDisplayName = "Meter (m)",
                targetObject = null,
                isAmbiguous = true,
                errorOrPrompt = "Please enter some metrics to query. E.g., '1000 meters' or '1 TB in photos'."
            )
        }

        // 1. Numeric Extraction (e.g., 1000, 1.5, 3e5)
        val valuePattern = Pattern.compile("(?i)\\b(\\d+(?:\\.\\d+)?(?:e-?\\d+)?)\\b")
        val matcher = valuePattern.matcher(queryClean)
        var value = 1.0
        var valueIndexStart = -1
        var valueIndexEnd = -1

        if (matcher.find()) {
            value = matcher.group(1)?.toDoubleOrNull() ?: 1.0
            valueIndexStart = matcher.start()
            valueIndexEnd = matcher.end()
        }

        // 2. Unit extraction from synonyms with proximity/preposition awareness
        var foundUnitKey: String? = null
        var foundCategory: String? = null
        var foundUnitSynonym: String? = null

        data class UnitMatch(val key: String, val category: String, val synonym: String, val startIdx: Int)
        val allUnitMatches = mutableListOf<UnitMatch>()

        for (synonym in unitSynonyms.keys) {
            val synPattern = Pattern.compile("\\b${Pattern.quote(synonym)}\\b")
            val synMatcher = synPattern.matcher(queryClean)
            while (synMatcher.find()) {
                val pair = unitSynonyms[synonym]
                if (pair != null) {
                    allUnitMatches.add(UnitMatch(pair.first, pair.second, synonym, synMatcher.start()))
                }
            }
        }

        if (allUnitMatches.isNotEmpty()) {
            // Sort primary suggestions from left to right
            // We discard extremely ambiguous synonyms like "in" or "b" if they are far from the numeric value and we have other matches.
            val filteredMatches = allUnitMatches.filter { match ->
                if (match.synonym == "in" || match.synonym == "b") {
                    // It must be close to the number (within 4 characters of valueIndexEnd), or there are no other unit matches in the string
                    val isCloseToNumber = valueIndexEnd != -1 && (match.startIdx - valueIndexEnd) <= 4
                    val hasAlternativeUnits = allUnitMatches.any { it.synonym != "in" && it.synonym != "b" }
                    isCloseToNumber || !hasAlternativeUnits
                } else {
                    true
                }
            }.sortedWith(compareBy<UnitMatch> { it.startIdx }.thenByDescending { it.synonym.length })

            if (filteredMatches.isNotEmpty()) {
                val bestUnit = filteredMatches[0]
                foundUnitKey = bestUnit.key
                foundCategory = bestUnit.category
                foundUnitSynonym = bestUnit.synonym
            }
        }

        // If no unit matches, let's look for currency symbol directly ($ or € or £ or ¥)
        if (foundUnitKey == null) {
            when {
                queryClean.contains("$") -> {
                    foundUnitKey = "usd"
                    foundCategory = "Money"
                }
                queryClean.contains("€") -> {
                    foundUnitKey = "eur"
                    foundCategory = "Money"
                }
                queryClean.contains("£") -> {
                    foundUnitKey = "gbp"
                    foundCategory = "Money"
                }
                queryClean.contains("¥") -> {
                    foundUnitKey = "jpy"
                    foundCategory = "Money"
                }
            }
        }

        // 3. Match Target Object from synonyms
        var foundTargetRef: ComparisonObject? = null
        val sortedTargetSynonyms = targetSynonyms.keys.sortedByDescending { it.length }
        for (targetSyn in sortedTargetSynonyms) {
            val targetPattern = Pattern.compile("\\b${Pattern.quote(targetSyn)}\\b")
            val targetMatcher = targetPattern.matcher(queryClean)
            if (targetMatcher.find()) {
                val dbName = targetSynonyms[targetSyn]
                foundTargetRef = WeirdCalculatorEngine.dbObjects.find { it.name.equals(dbName, ignoreCase = true) }
                if (foundTargetRef != null) {
                    break
                }
            }
        }

        // Context Inference:
        // If we found a target reference (e.g. "elephants" -> "African Elephant" (belongs to Animals, mass based))
        // but no fromUnit was specified, can we infer the unit?
        // E.g., "500 elephants" or "500 in elephants" -> if no fromUnit, we can assume the base unit of that category!
        // Weight/Mass base unit is "kg", Length is "m", Energy is "j", Digital/Technology is "b" / "gb", Money is "usd" etc.
        if (foundUnitKey == null && foundTargetRef != null) {
            val targetCategory = foundTargetRef.category
            when (targetCategory) {
                "Animals" -> { // weight scale
                    foundUnitKey = "kg"
                    foundCategory = "Weight / Mass"
                }
                "Landmarks" -> { // Length scale
                    foundUnitKey = "m"
                    foundCategory = "Length"
                }
                "Technology" -> { // Storage scale
                    foundUnitKey = "gb"
                    foundCategory = "Digital Storage"
                }
                "Energy" -> { // Energy scale
                    foundUnitKey = "kwh"
                    foundCategory = "Energy"
                }
                "Money" -> { // Cost scale
                    foundUnitKey = "usd"
                    foundCategory = "Money"
                }
                else -> {
                    foundUnitKey = "m"
                    foundCategory = "Length"
                }
            }
        }

        // If we still can't find a unit, but we have a unit-like suffix anywhere
        if (foundUnitKey == null) {
            // Check word lists
            val tokens = queryClean.split(Pattern.compile("[\\s,;?!.]+"))
            for (token in tokens) {
                val cleanToken = token.trim()
                if (cleanToken.isNotEmpty() && unitSynonyms.containsKey(cleanToken)) {
                    val pair = unitSynonyms[cleanToken]
                    if (pair != null) {
                        foundUnitKey = pair.first
                        foundCategory = pair.second
                        break
                    }
                }
            }
        }

        // Fallback or Clarification Check
        if (foundUnitKey == null || foundCategory == null) {
            return ParsedNluQuery(
                value = value,
                fromUnitKey = "m",
                category = "Length",
                fromUnitDisplayName = "Meter (m)",
                targetObject = null,
                isAmbiguous = true,
                errorOrPrompt = "We couldn't detect the physical metric/unit in your inquiry \"$rawQuery\". Try using common units like meters, kilograms, TB, hours, or dollars."
            )
        }

        // Get the specific display name from WeirdCalculatorEngine unitsMap
        val activeUnits = WeirdCalculatorEngine.unitsMap[foundCategory] ?: emptyList()
        val matchDef = activeUnits.find { it.key == foundUnitKey }
        val unitDisp = matchDef?.displayName ?: foundUnitKey

        // If target was found, but belongs to a mismatching category of physical dimensions:
        // E.g. "1000 meters in elephants" (Weight elephant vs length meter)
        // Let's verify compatibility!
        if (foundTargetRef != null) {
            val isCompatible = when (foundCategory) {
                "Length", "Astronomy" -> foundTargetRef.category == "Landmarks"
                "Weight / Mass" -> foundTargetRef.category == "Animals"
                "Digital Storage" -> foundTargetRef.category == "Technology"
                "Energy" -> foundTargetRef.category == "Energy"
                "Money" -> foundTargetRef.category == "Money"
                else -> false
            }
            if (!isCompatible) {
                // If incompatible: return with notice or don't enforce strictly but fallback cleanly
                // "Insufficient data for this comparison" as requested.
                return ParsedNluQuery(
                    value = value,
                    fromUnitKey = foundUnitKey,
                    category = foundCategory,
                    fromUnitDisplayName = unitDisp,
                    targetObject = null,
                    isAmbiguous = false,
                    errorOrPrompt = "Insufficient data for this comparison: Cannot compare physical dimension of $foundCategory with standard target of ${foundTargetRef.name} (${foundTargetRef.category})."
                )
            }
        }

        return ParsedNluQuery(
            value = value,
            fromUnitKey = foundUnitKey,
            category = foundCategory,
            fromUnitDisplayName = unitDisp,
            targetObject = foundTargetRef,
            isAmbiguous = false
        )
    }

    /**
     * Executes the conversion and comparison deterministically
     */
    fun executeNluQuery(parsed: ParsedNluQuery): ParsedNluResponse {
        if (parsed.errorOrPrompt != null) {
            return ParsedNluResponse(
                success = false,
                resultText = "No calculation performed.",
                comparisons = emptyList(),
                fact = "Knowledge base stays accurate.",
                simpleExplanation = parsed.errorOrPrompt,
                structuredFormat = "{\n  \"value\": ${parsed.value},\n  \"from_unit\": \"${parsed.fromUnitKey}\"\n}"
            )
        }

        val category = parsed.category
        val value = parsed.value
        val fromUnitKey = parsed.fromUnitKey

        // Get standard companion unit for direct conversion
        val activeUnits = WeirdCalculatorEngine.unitsMap[category] ?: emptyList()
        val fromDef = activeUnits.find { it.key == fromUnitKey } ?: return ParsedNluResponse(
            success = false,
            resultText = "Error",
            comparisons = emptyList(),
            fact = "Math error",
            simpleExplanation = "Source unit key not found in data lists.",
            structuredFormat = "{}"
        )

        // Find a complementary companion unit for output representation
        val companionDef = if (activeUnits.size > 1) {
            // Find a unit distinct from fromUnit
            activeUnits.find { it.key != fromUnitKey } ?: activeUnits[0]
        } else {
            fromDef
        }

        // Calculate value in base SI unit of the category first
        val siBaseValue = value * fromDef.factorToBase

        // Direct conversion result
        val companionValue = siBaseValue / companionDef.factorToBase
        val formattedSrc = formatDecimal(value, 4)
        val formattedDest = formatDecimal(companionValue, 4)
        val directResult = "$formattedSrc ${fromDef.displayName.substringBefore(" (")} = $formattedDest ${companionDef.displayName.substringBefore(" (")}"

        val finalComparisons = mutableListOf<String>()
        var explanation = ""

        // If a target object was specified, perform custom calculation first
        if (parsed.targetObject != null) {
            val target = parsed.targetObject
            // Formula is: Input value in SI / Target object base value in SI
            val relativeValue = siBaseValue / target.baseValue
            val formattedRel = formatDecimal(relativeValue, 4)

            finalComparisons.add("🎯 ~${formatCount(relativeValue)} times the size of ${target.name}")
            explanation = "$formattedSrc ${fromDef.displayName.substringBefore(" (")} is mathematically equivalent to about $formattedRel of a ${target.name}."

            // Also fill up with some other standard comparisons for that category
            val extraComparisons = WeirdCalculatorEngine.getComparisons(category, siBaseValue)
            for (comp in extraComparisons) {
                if (finalComparisons.size < 5 && !comp.contains(target.name, ignoreCase = true)) {
                    finalComparisons.add(comp)
                }
            }
        } else {
            // Normal comparisons
            val extraComparisons = WeirdCalculatorEngine.getComparisons(category, siBaseValue)
            finalComparisons.addAll(extraComparisons.take(4))
            
            explanation = "$formattedSrc ${fromDef.displayName.substringBefore(" (")} is a metric value of $category, equal to several interesting physical indicators."
        }

        // Structured NLU json representation as requested:
        val structuredJson = """
            {
              "value": $value,
              "from_unit": "${fromDef.displayName.substringBefore(" (").trim().lowercase()}"${if (parsed.targetObject != null) ",\n  \"target\": \"${parsed.targetObject.name.lowercase().replace(" ", "_")}\"" else ""}
            }
        """.trimIndent()

        return ParsedNluResponse(
            success = true,
            resultText = directResult,
            comparisons = finalComparisons,
            fact = WeirdCalculatorEngine.getFunFactForCategory(category),
            simpleExplanation = explanation,
            structuredFormat = structuredJson
        )
    }

    private fun formatCount(v: Double): String {
        return when {
            v.isNaN() || v.isInfinite() -> "0"
            v >= 1e6 -> String.format("%,.1f Million", v / 1e6)
            v >= 1000 -> String.format("%,d", v.toInt())
            v >= 1.0 -> String.format("%.2f", v)
            v >= 0.001 -> String.format("%.4f", v)
            v > 0 -> String.format("%.6f", v)
            else -> "0"
        }
    }

    private fun formatDecimal(v: Double, decimals: Int): String {
        if (v.isNaN() || v.isInfinite()) return "0"
        return if (v % 1.0 == 0.0) {
            String.format("%,d", v.toLong())
        } else {
            val formatStr = "%.,${decimals}f"
            try {
                String.format(formatStr, v)
            } catch (e: Exception) {
                v.toString()
            }
        }
    }
}

data class ParsedNluResponse(
    val success: Boolean,
    val resultText: String,
    val comparisons: List<String>,
    val fact: String,
    val simpleExplanation: String,
    val structuredFormat: String
)
