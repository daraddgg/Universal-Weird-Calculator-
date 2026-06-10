package com.example.calculator

import java.util.regex.Pattern
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow

// --- CORE DATA STRUCTURES ---

data class Element(
    val symbol: String,
    val name: String,
    val atomicNumber: Int,
    val atomicMass: Double,
    val electronegativity: Double,
    val standardState: String,
    val chemicalGroup: String,
    val commonValence: List<Int>
)

data class CompoundThermo(
    val formula: String,
    val name: String,
    val hDeltaF: Double, // kJ/mol
    val sEntropy: Double, // J/(mol·K)
    val gDeltaF: Double  // kJ/mol
)

data class ReactionParticipant(
    val coefficient: Int,
    val formula: String,
    val isGas: Boolean = false
)

data class parsedEquation(
    val reactants: List<ReactionParticipant>,
    val products: List<ReactionParticipant>
)

// --- CHEMISTRY KNOWLEDGE DATABASE (OFFLINE) ---

object ChemistryDatabase {
    val elements = mapOf(
        "H" to Element("H", "Hydrogen", 1, 1.008, 2.20, "Gas", "Nonmetal", listOf(1, -1)),
        "He" to Element("He", "Helium", 2, 4.0026, 0.0, "Gas", "Noble Gas", listOf(0)),
        "Li" to Element("Li", "Lithium", 3, 6.94, 0.98, "Solid", "Alkali Metal", listOf(1)),
        "Be" to Element("Be", "Beryllium", 4, 9.0122, 1.57, "Solid", "Alkaline Earth Metal", listOf(2)),
        "B" to Element("B", "Boron", 5, 10.81, 2.04, "Solid", "Metalloid", listOf(3)),
        "C" to Element("C", "Carbon", 6, 12.011, 2.55, "Solid", "Nonmetal", listOf(4, -4)),
        "N" to Element("N", "Nitrogen", 7, 14.007, 3.04, "Gas", "Nonmetal", listOf(-3, 3, 5)),
        "O" to Element("O", "Oxygen", 8, 15.999, 3.44, "Gas", "Nonmetal", listOf(-2)),
        "F" to Element("F", "Fluorine", 9, 18.998, 3.98, "Gas", "Halogen", listOf(-1)),
        "Ne" to Element("Ne", "Neon", 10, 20.180, 0.0, "Gas", "Noble Gas", listOf(0)),
        "Na" to Element("Na", "Sodium", 11, 22.990, 0.93, "Solid", "Alkali Metal", listOf(1)),
        "Mg" to Element("Mg", "Magnesium", 12, 24.305, 1.31, "Solid", "Alkaline Earth Metal", listOf(2)),
        "Al" to Element("Al", "Aluminum", 13, 26.982, 1.61, "Solid", "Post-Transition Metal", listOf(3)),
        "Si" to Element("Si", "Silicon", 14, 28.085, 1.90, "Solid", "Metalloid", listOf(4)),
        "P" to Element("P", "Phosphorus", 15, 30.974, 2.19, "Solid", "Nonmetal", listOf(-3, 3, 5)),
        "S" to Element("S", "Sulfur", 16, 32.06, 2.58, "Solid", "Nonmetal", listOf(-2, 4, 6)),
        "Cl" to Element("Cl", "Chlorine", 17, 35.45, 3.16, "Gas", "Halogen", listOf(-1)),
        "Ar" to Element("Ar", "Argon", 18, 39.948, 0.0, "Gas", "Noble Gas", listOf(0)),
        "K" to Element("K", "Potassium", 19, 39.098, 0.82, "Solid", "Alkali Metal", listOf(1)),
        "Ca" to Element("Ca", "Calcium", 20, 40.078, 1.00, "Solid", "Alkaline Earth Metal", listOf(2)),
        "Cr" to Element("Cr", "Chromium", 24, 51.996, 1.66, "Solid", "Transition Metal", listOf(2, 3, 6)),
        "Mn" to Element("Mn", "Manganese", 25, 54.938, 1.55, "Solid", "Transition Metal", listOf(2, 4, 7)),
        "Fe" to Element("Fe", "Iron", 26, 55.845, 1.83, "Solid", "Transition Metal", listOf(2, 3)),
        "Co" to Element("Co", "Cobalt", 27, 58.933, 1.88, "Solid", "Transition Metal", listOf(2, 3)),
        "Ni" to Element("Ni", "Nickel", 28, 58.693, 1.91, "Solid", "Transition Metal", listOf(2)),
        "Cu" to Element("Cu", "Copper", 29, 63.546, 1.90, "Solid", "Transition Metal", listOf(1, 2)),
        "Zn" to Element("Zn", "Zinc", 30, 65.38, 1.65, "Solid", "Transition Metal", listOf(2)),
        "Br" to Element("Br", "Bromine", 35, 79.904, 2.96, "Liquid", "Halogen", listOf(-1)),
        "Ag" to Element("Ag", "Silver", 47, 107.87, 1.93, "Solid", "Transition Metal", listOf(1)),
        "I" to Element("I", "Iodine", 53, 126.90, 2.66, "Solid", "Halogen", listOf(-1)),
        "Ba" to Element("Ba", "Barium", 56, 137.33, 0.89, "Solid", "Alkaline Earth Metal", listOf(2)),
        "Pt" to Element("Pt", "Platinum", 78, 195.08, 2.28, "Solid", "Transition Metal", listOf(2, 4)),
        "Au" to Element("Au", "Gold", 79, 196.97, 2.54, "Solid", "Transition Metal", listOf(3)),
        "Hg" to Element("Hg", "Mercury", 80, 200.59, 2.00, "Liquid", "Transition Metal", listOf(1, 2)),
        "Pb" to Element("Pb", "Lead", 82, 207.2, 2.33, "Solid", "Post-Transition Metal", listOf(2, 4)),
        "U" to Element("U", "Uranium", 92, 238.03, 1.38, "Solid", "Actinide", listOf(3, 4, 6))
    )

    // Standard thermodynamic database (at 298.15 K)
    val thermodynamics = mapOf(
        "H2O(l)" to CompoundThermo("H2O(l)", "Liquid Water", -285.83, 69.91, -237.13),
        "H2O(g)" to CompoundThermo("H2O(g)", "Water Vapor", -241.82, 188.83, -228.57),
        "CO2(g)" to CompoundThermo("CO2(g)", "Carbon Dioxide", -393.51, 213.74, -394.36),
        "CO(g)" to CompoundThermo("CO(g)", "Carbon Monoxide", -110.53, 197.67, -137.17),
        "O2(g)" to CompoundThermo("O2(g)", "Oxygen Gas", 0.0, 205.15, 0.0),
        "H2(g)" to CompoundThermo("H2(g)", "Hydrogen Gas", 0.0, 130.68, 0.0),
        "N2(g)" to CompoundThermo("N2(g)", "Nitrogen Gas", 0.0, 191.61, 0.0),
        "NH3(g)" to CompoundThermo("NH3(g)", "Ammonia Gas", -45.90, 192.77, -16.45),
        "CH4(g)" to CompoundThermo("CH4(g)", "Methane", -74.81, 186.26, -50.72),
        "C2H5OH(l)" to CompoundThermo("C2H5OH(l)", "Ethanol", -277.69, 160.70, -174.78),
        "NaCl(s)" to CompoundThermo("NaCl(s)", "Sodium Chloride", -411.12, 72.13, -384.14),
        "HCl(g)" to CompoundThermo("HCl(g)", "Hydrogen Chloride Gas", -92.31, 186.91, -95.30),
        "NaOH(s)" to CompoundThermo("NaOH(s)", "Sodium Hydroxide", -425.61, 64.44, -379.48),
        "NH4Cl(s)" to CompoundThermo("NH4Cl(s)", "Ammonium Chloride", -314.43, 94.60, -202.96),
        "NH4NO3(s)" to CompoundThermo("NH4NO3(s)", "Ammonium Nitrate", -365.56, 151.08, -183.89),
        "CaCO3(s)" to CompoundThermo("CaCO3(s)", "Calcium Carbonate", -1206.92, 92.90, -1128.84),
        "CaO(s)" to CompoundThermo("CaO(s)", "Calcium Oxide (Lime)", -635.09, 39.75, -604.20),
        "SO2(g)" to CompoundThermo("SO2(g)", "Sulfur Dioxide", -296.81, 248.22, -300.19),
        "SO3(g)" to CompoundThermo("SO3(g)", "Sulfur Trioxide", -395.72, 256.76, -371.06)
    )

    // Chemistry Constants
    const val R = 8.314462618 // Gas constant, J/(mol·K)
    const val AVOGADRO = 6.02214076e23 // Avogadro's Number
}

// --- CORE CHEMICAL COMPUTATION ENGINE ---

object ChemistryEngine {

    /**
     * Parses a chemical formula (e.g., "H2O", "Ca(OH)2", "Fe2(SO4)3")
     * and returns a map of atom counts, e.g., {"Fe" to 2, "S" to 3, "O" to 12}.
     * Throws IllegalArgumentException on invalid structures.
     */
    enum class TokenType {
        ELEMENT, NUMBER, OPEN_PAREN, CLOSE_PAREN
    }

    data class Token(val type: TokenType, val value: String)

    interface FormulaNode {
        fun flatten(multiplier: Int): Map<String, Int>
    }

    class ElementNode(val symbol: String, val count: Int) : FormulaNode {
        override fun flatten(multiplier: Int): Map<String, Int> {
            return mapOf(symbol to count * multiplier)
        }
    }

    class GroupNode(val children: List<FormulaNode>, val multiplier: Int) : FormulaNode {
        override fun flatten(outMultiplier: Int): Map<String, Int> {
            val merged = mutableMapOf<String, Int>()
            val totalMultiplier = multiplier * outMultiplier
            for (child in children) {
                val childMap = child.flatten(totalMultiplier)
                for ((sym, count) in childMap) {
                    merged[sym] = (merged[sym] ?: 0) + count
                }
            }
            return merged
        }
    }

    fun tokenizeFormula(formula: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        val length = formula.length
        while (i < length) {
            val char = formula[i]
            when {
                char == '(' -> {
                    tokens.add(Token(TokenType.OPEN_PAREN, "("))
                    i++
                }
                char == ')' -> {
                    tokens.add(Token(TokenType.CLOSE_PAREN, ")"))
                    i++
                }
                char.isUpperCase() -> {
                    var symbol = char.toString()
                    i++
                    while (i < length && formula[i].isLowerCase()) {
                        symbol += formula[i]
                        i++
                    }
                    tokens.add(Token(TokenType.ELEMENT, symbol))
                }
                char.isDigit() -> {
                    var numStr = char.toString()
                    i++
                    while (i < length && formula[i].isDigit()) {
                        numStr += formula[i]
                        i++
                    }
                    tokens.add(Token(TokenType.NUMBER, numStr))
                }
                else -> {
                    throw IllegalArgumentException("Unexpected character '$char' in formula.")
                }
            }
        }
        return tokens
    }

    fun parseTokensToNodes(tokens: List<Token>): List<FormulaNode> {
        var index = 0
        fun parseNext(): List<FormulaNode> {
            val nodes = mutableListOf<FormulaNode>()
            while (index < tokens.size) {
                val token = tokens[index]
                when (token.type) {
                    TokenType.OPEN_PAREN -> {
                        index++ // consume '('
                        val subNodes = parseNext() // parse content inside parens
                        if (index >= tokens.size || tokens[index].type != TokenType.CLOSE_PAREN) {
                            throw IllegalArgumentException("Mismatched parenthesis in formula.")
                        }
                        index++ // consume ')'
                        var mult = 1
                        if (index < tokens.size && tokens[index].type == TokenType.NUMBER) {
                            mult = tokens[index].value.toInt()
                            index++ // consume multiplier
                        }
                        nodes.add(GroupNode(subNodes, mult))
                    }
                    TokenType.CLOSE_PAREN -> {
                        break
                    }
                    TokenType.ELEMENT -> {
                        val symbol = token.value
                        index++ // consume symbol
                        var count = 1
                        if (index < tokens.size && tokens[index].type == TokenType.NUMBER) {
                            count = tokens[index].value.toInt()
                            index++ // consume multiplier
                        }
                        nodes.add(ElementNode(symbol, count))
                    }
                    TokenType.NUMBER -> {
                        throw IllegalArgumentException("Unexpected number token without element or group prefix.")
                    }
                }
            }
            return nodes
        }
        
        val rootNodes = parseNext()
        if (index < tokens.size) {
            throw IllegalArgumentException("Extraneous tokens near end of formula.")
        }
        return rootNodes
    }

    fun cleanStateIndicator(formula: String): String {
        val trimmed = formula.trim()
        val suffixList = listOf("(g)", "(l)", "(s)", "(aq)", "(G)", "(L)", "(S)", "(AQ)")
        for (suffix in suffixList) {
            if (trimmed.endsWith(suffix)) {
                return trimmed.dropLast(suffix.length).trim()
            }
        }
        return trimmed
    }

    fun checkParsingCompleteness(input: String) {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            throw IllegalArgumentException("Parsing incomplete - expanding chemical structure")
        }
        val openCount = trimmed.count { it == '(' }
        val closeCount = trimmed.count { it == ')' }
        if (openCount != closeCount) {
            throw IllegalArgumentException("Parsing incomplete - expanding chemical structure")
        }
        if (trimmed.endsWith("+") || trimmed.endsWith("->") || trimmed.endsWith("=") || trimmed.endsWith("→")) {
            throw IllegalArgumentException("Parsing incomplete - expanding chemical structure")
        }
    }

    /**
     * Parses a chemical formula (e.g., "H2O", "Ca(OH)2", "Fe2(SO4)3")
     * and returns a map of atom counts, e.g., {"Fe" to 2, "S" to 3, "O" to 12}.
     * Throws IllegalArgumentException on invalid structures.
     * Guaranteed to adhere to requested structural parsing pipeline order:
     * 1. Parse formula (Tokenization)
     * 2. Expand parentheses (AST node scaling)
     * 3. Count atoms (Map reduction and periodic database verification)
     */
    fun parseFormula(input: String): Map<String, Int> {
        val cleanInput = cleanStateIndicator(input.replace("\\s".toRegex(), ""))
        checkParsingCompleteness(cleanInput)

        // 1. Parse formula
        val tokens = tokenizeFormula(cleanInput)

        // 2. Expand parentheses
        val nodes = parseTokensToNodes(tokens)

        // 3. Count atoms
        val result = mutableMapOf<String, Int>()
        for (node in nodes) {
            val flatNode = node.flatten(1)
            for ((key, value) in flatNode) {
                result[key] = (result[key] ?: 0) + value
            }
        }

        // Validate elements against Database
        for (elem in result.keys) {
            if (!ChemistryDatabase.elements.containsKey(elem)) {
                throw IllegalArgumentException("Unknown element symbol '$elem'. We only compute verified elements in Periodic Table.")
            }
        }

        return result
    }

    /**
     * Calculates molar mass of a parsed formula in g/mol
     */
    fun calculateMolarMass(atoms: Map<String, Int>): Double {
        var totalMass = 0.0
        for ((symbol, count) in atoms) {
            val elem = ChemistryDatabase.elements[symbol] ?: throw IllegalArgumentException("Element $symbol not configured in database.")
            totalMass += elem.atomicMass * count
        }
        return totalMass
    }

    /**
     * Parses a reaction string (e.g. "2 H2 + O2 -> 2 H2O") and returns a parsedEquation
     */
    fun parseReaction(reactionStr: String): parsedEquation {
        checkParsingCompleteness(reactionStr)
        val delimiters = listOf("->", "→", "=")
        var splitResult: List<String> = emptyList()
        for (delim in delimiters) {
            if (reactionStr.contains(delim)) {
                splitResult = reactionStr.split(delim)
                break
            }
        }

        if (splitResult.size != 2) {
            throw IllegalArgumentException("Reaction must contain one clear separator (e.g., '->' or '=') separating reactants from products.")
        }

        val reactantsStr = splitResult[0].trim()
        val productsStr = splitResult[1].trim()

        fun parseSide(sideStr: String): List<ReactionParticipant> {
            if (sideStr.isEmpty()) return emptyList()
            val terms = sideStr.split("+")
            return terms.map { term ->
                val trimmed = term.trim()
                if (trimmed.isEmpty()) throw IllegalArgumentException("Empty chemical term found in reaction side.")
                
                // Find first index where element symbol (capital letter) or parenthesis starts
                var coeffStr = ""
                var idx = 0
                while (idx < trimmed.length && (trimmed[idx].isDigit() || trimmed[idx].isWhitespace())) {
                    coeffStr += trimmed[idx]
                    idx++
                }

                val coeffVal = if (coeffStr.trim().isEmpty()) {
                    1
                } else {
                    coeffStr.trim().toIntOrNull() ?: throw IllegalArgumentException("Invalid coefficient '$coeffStr' in reaction.")
                }

                val formulaRaw = trimmed.substring(idx).trim()
                if (formulaRaw.isEmpty()) {
                    throw IllegalArgumentException("Missing chemical formula in term '$trimmed'.")
                }

                // Check standard state indicator: e.g. "H2O(l)" or "CO2(g)"
                val isGas = formulaRaw.endsWith("(g)", ignoreCase = true)
                // Clean standard state indicators using cleanStateIndicator helper
                val cleanFormula = cleanStateIndicator(formulaRaw)

                // Validate formula parses successfully (fully parses and completely expands)
                parseFormula(cleanFormula)

                ReactionParticipant(coeffVal, formulaRaw, isGas)
            }
        }

        val reactantsSet = parseSide(reactantsStr)
        val productsSet = parseSide(productsStr)

        if (reactantsSet.isEmpty() || productsSet.isEmpty()) {
            throw IllegalArgumentException("Equation must have at least one reactant and one product.")
        }

        return parsedEquation(reactantsSet, productsSet)
    }

    /**
     * Step 4: Balance equation
     * Determines positive integer coefficients to satisfy the conservation of elements.
     */
    fun balanceEquation(eq: parsedEquation): parsedEquation {
        val reactantFormulas = eq.reactants.map { cleanStateIndicator(it.formula) }
        val productFormulas = eq.products.map { cleanStateIndicator(it.formula) }

        val reactantAtoms = reactantFormulas.map { parseFormula(it) }
        val productAtoms = productFormulas.map { parseFormula(it) }

        // Short-circuit if already balanced with existing coefficients
        val existingReactants = eq.reactants.map { it.coefficient }
        val existingProducts = eq.products.map { it.coefficient }
        if (isBalanced(reactantAtoms, existingReactants, productAtoms, existingProducts)) {
            return eq
        }

        val numReactants = eq.reactants.size
        val numProducts = eq.products.size
        val numTerms = numReactants + numProducts

        val maxCoeff = when {
            numTerms <= 3 -> 15
            numTerms == 4 -> 10
            numTerms == 5 -> 6
            else -> 4
        }

        val coeffs = IntArray(numTerms) { 1 }

        fun search(index: Int): Boolean {
            if (index == numTerms) {
                val leftCoeffs = coeffs.slice(0 until numReactants)
                val rightCoeffs = coeffs.slice(numReactants until numTerms)
                return isBalanced(reactantAtoms, leftCoeffs, productAtoms, rightCoeffs)
            }

            for (c in 1..maxCoeff) {
                coeffs[index] = c
                if (search(index + 1)) {
                    return true
                }
            }
            return false
        }

        if (search(0)) {
            val balancedReactants = eq.reactants.mapIndexed { idx, part ->
                part.copy(coefficient = coeffs[idx])
            }
            val balancedProducts = eq.products.mapIndexed { idx, part ->
                part.copy(coefficient = coeffs[numReactants + idx])
            }
            return parsedEquation(balancedReactants, balancedProducts)
        } else {
            return eq
        }
    }

    private fun isBalanced(
        reactants: List<Map<String, Int>>,
        reactantCoeffs: List<Int>,
        products: List<Map<String, Int>>,
        productCoeffs: List<Int>
    ): Boolean {
        val reactantTotals = mutableMapOf<String, Int>()
        val productTotals = mutableMapOf<String, Int>()

        for (i in reactants.indices) {
            val coeff = reactantCoeffs[i]
            for ((elem, count) in reactants[i]) {
                reactantTotals[elem] = (reactantTotals[elem] ?: 0) + count * coeff
            }
        }
        for (i in products.indices) {
            val coeff = productCoeffs[i]
            for ((elem, count) in products[i]) {
                productTotals[elem] = (productTotals[elem] ?: 0) + count * coeff
            }
        }
        return reactantTotals == productTotals
    }

    /**
     * Step 5: Validate conservation.
     * Verifies conservation of mass: checks if atoms of each element are balanced on both sides.
     * Throws exception if atomic sums do not match.
     */
    fun validateConservationOfMass(eq: parsedEquation) {
        val reactantAtoms = mutableMapOf<String, Int>()
        val productAtoms = mutableMapOf<String, Int>()

        for (part in eq.reactants) {
            // Remove state indicator to parse formula properly
            val cleanForm = cleanStateIndicator(part.formula)
            val atomsMap = parseFormula(cleanForm)
            for ((symbol, count) in atomsMap) {
                reactantAtoms[symbol] = (reactantAtoms[symbol] ?: 0) + count * part.coefficient
            }
        }

        for (part in eq.products) {
            val cleanForm = cleanStateIndicator(part.formula)
            val atomsMap = parseFormula(cleanForm)
            for ((symbol, count) in atomsMap) {
                productAtoms[symbol] = (productAtoms[symbol] ?: 0) + count * part.coefficient
            }
        }

        if (reactantAtoms != productAtoms) {
            val diffs = mutableListOf<String>()
            val allKeys = reactantAtoms.keys + productAtoms.keys
            for (key in allKeys) {
                val left = reactantAtoms[key] ?: 0
                val right = productAtoms[key] ?: 0
                if (left != right) {
                    diffs.add("$key: Reactants sum=$left VS Products sum=$right")
                }
            }
            throw IllegalArgumentException("Conservation of mass violated! Equation is not balanced: " + diffs.joinToString("; "))
        }
    }

    /**
     * Complete sequential processing pipeline strictly adhering to the requested order:
     * 1. Parse formula (Internal tokenization)
     * 2. Expand parentheses (Internal AST node scaling)
     * 3. Count atoms (Internal Map reduction)
     * 4. Balance equation (Determine positive integer coefficients using backtracking)
     * 5. Validate conservation (Verify final stoichiometry element counts)
     */
    fun processAndValidateReaction(reactionStr: String): parsedEquation {
        // Steps 1 to 3 happen sequentially for every chemical element inside parseReaction -> parseFormula
        val eq = parseReaction(reactionStr)
        // Step 4: Balance equation
        val balancedEq = balanceEquation(eq)
        // Step 5: Validate conservation
        validateConservationOfMass(balancedEq)
        return balancedEq
    }

    // --- COMPUTATION MODULES IMPLEMENTATION ---

    /**
     * Solve Stoichiometry with user masses
     * inputs: reactive masses (Map of Reactant Formula -> Mass in grams)
     */
    fun solveStoichiometry(eq: parsedEquation, inputMasses: Map<String, Double>): StoichiometryResult {
        // 1. Validate conservation of mass first
        validateConservationOfMass(eq)

        // 2. Prepare physical properties of reactants
        val participantMolarMasses = mutableMapOf<String, Double>()
        for (r in eq.reactants) {
            val cleanForm = cleanStateIndicator(r.formula)
            val atoms = parseFormula(cleanForm)
            participantMolarMasses[r.formula] = calculateMolarMass(atoms)
        }
        for (p in eq.products) {
            val cleanForm = cleanStateIndicator(p.formula)
            val atoms = parseFormula(cleanForm)
            participantMolarMasses[p.formula] = calculateMolarMass(atoms)
        }

        // 3. Find limiting reagent
        var limitingReactantFormula: String? = null
        var minExtent = Double.MAX_VALUE
        val reactantMoles = mutableMapOf<String, Double>()
        val reactantExtents = mutableMapOf<String, Double>()

        for (r in eq.reactants) {
            val inputMass = inputMasses[r.formula] ?: 0.0
            val M = participantMolarMasses[r.formula] ?: 1.0
            val moles = inputMass / M
            reactantMoles[r.formula] = moles

            val extent = moles / r.coefficient
            reactantExtents[r.formula] = extent
            if (extent < minExtent) {
                minExtent = extent
                limitingReactantFormula = r.formula
            }
        }

        val limitRef = limitingReactantFormula ?: throw IllegalArgumentException("Could not determine limiting reagent.")

        // 4. Calculate outputs
        val excessDetails = mutableListOf<String>()
        val productOutputs = mutableListOf<String>()

        for (r in eq.reactants) {
            val molesIn = reactantMoles[r.formula] ?: 0.0
            val M = participantMolarMasses[r.formula] ?: 1.0
            if (r.formula == limitRef) {
                excessDetails.add("• Reactant **${r.formula}** is completely consumed (Limiting Reagent).")
            } else {
                val molesConsumed = r.coefficient * minExtent
                val molesRemaining = molesIn - molesConsumed
                val massRemaining = molesRemaining * M
                excessDetails.add(
                    "• Reactant **${r.formula}** is in Excess:\n" +
                    "  - Consumed: ${String.format("%.4f", molesConsumed)} mol (${String.format("%.3f", molesConsumed * M)} g)\n" +
                    "  - Unused backlog: ${String.format("%.4f", molesRemaining)} mol (${String.format("%.3f", massRemaining)} g remaining)"
                )
            }
        }

        for (p in eq.products) {
            val molesProduced = p.coefficient * minExtent
            val M = participantMolarMasses[p.formula] ?: 1.0
            val massProduced = molesProduced * M
            productOutputs.add(
                "• **${p.formula}** Theoretical Yield:\n" +
                "  - Moles: ${String.format("%.4f", molesProduced)} mol\n" +
                "  - Mass: **${String.format("%.3f", massProduced)} g**"
            )
        }

        return StoichiometryResult(
            limitingReagent = limitRef,
            extentOfReaction = minExtent,
            excessStatus = excessDetails,
            productYields = productOutputs
        )
    }

    /**
     * Solve Thermodynamics for standard reactions
     */
    fun solveThermodynamics(eq: parsedEquation, temperatureK: Double): ThermoResult {
        var hRxn = 0.0
        var sRxn = 0.0
        val missingDataList = mutableListOf<String>()

        fun evaluateComponent(part: ReactionParticipant, isProduct: Boolean) {
            val sign = if (isProduct) 1 else -1
            val thermo = ChemistryDatabase.thermodynamics[part.formula]
            if (thermo == null) {
                missingDataList.add(part.formula)
            } else {
                hRxn += sign * part.coefficient * thermo.hDeltaF
                sRxn += sign * part.coefficient * thermo.sEntropy
            }
        }

        for (r in eq.reactants) evaluateComponent(r, isProduct = false)
        for (p in eq.products) evaluateComponent(p, isProduct = true)

        if (missingDataList.isNotEmpty()) {
            return ThermoResult(
                isValid = false,
                hDelta = 0.0,
                sDelta = 0.0,
                gDelta = 0.0,
                isSpontaneous = false,
                explanation = "Thermo data missing in database for: ${missingDataList.joinToString(", ")}. Insufficient standard state thermodynamics properties."
            )
        }

        // ΔG = ΔH − T * ΔS
        // ΔS is in J/(mol·K), we divide by 1000 to keep it in kJ/(mol·K)
        val sDeltaKj = sRxn / 1000.0
        val gRxn = hRxn - (temperatureK * sDeltaKj)

        val tempC = temperatureK - 273.15
        val thermState = if (hRxn < 0) "Exothermic (Releases thermal energy)" else "Endothermic (Absorbs thermal energy)"
        val entropyState = if (sRxn > 0) "Increases molecular chaos/disorder" else "Decreases molecular chaos/disorder"
        val spontaneity = if (gRxn < 0) {
            "SPONTANEOUS (Thermodynamically feasible, shifts forward to reach equilibrium)"
        } else if (gRxn > 0) {
            "NON-SPONTANEOUS (Requires external chemical or electrical work inputs)"
        } else {
            "AT EQUILIBRIUM"
        }

        val stepByStep = """
            1. Identified standard standard potentials at Temperature T = $temperatureK K (${String.format("%.1f", tempC)} °C)
            2. Calculated Reaction Enthalpy change (ΔH°):
               ΔH° = Σ(Products) - Σ(Reactants) = ${String.format("%.2f", hRxn)} kJ/mol ($thermState)
            3. Calculated Reaction Entropy change (ΔS°):
               ΔS° = Σ(Products) - Σ(Reactants) = ${String.format("%.2f", sRxn)} J/(mol·K) ($entropyState)
            4. Formulated standard Gibbs Free Energy change (ΔG°):
               ΔG° = ΔH° - T * ΔS°
               ΔG° = ${String.format("%.2f", hRxn)} kJ - ($temperatureK K * ${String.format("%.5f", sDeltaKj)} kJ/K)
               ΔG° = ${String.format("%.2f", gRxn)} kJ/mol
        """.trimIndent()

        return ThermoResult(
            isValid = true,
            hDelta = hRxn,
            sDelta = sRxn,
            gDelta = gRxn,
            isSpontaneous = gRxn < 0,
            explanation = spontaneity,
            breakdown = stepByStep
        )
    }

    /**
     * Solve Equilibrium shifts and reactions
     */
    fun solveEquilibrium(
        eq: parsedEquation,
        kConstant: Double,
        activeConcentrations: Map<String, Double>
    ): EquilibriumResult {
        // Calculate Reaction Quotient Q
        // Q = Π (Products^coeff) / Π (Reactants^coeff)
        var reactantProduct = 1.0
        var productProduct = 1.0

        for (r in eq.reactants) {
            val conc = activeConcentrations[r.formula] ?: 1.0
            reactantProduct *= conc.pow(r.coefficient)
        }

        for (p in eq.products) {
            val conc = activeConcentrations[p.formula] ?: 1.0
            productProduct *= conc.pow(p.coefficient)
        }

        val qQuotient = productProduct / reactantProduct

        val shiftDir: String
        val shiftDescription: String
        if (qQuotient < kConstant) {
            shiftDir = "SHIFTS RIGHT (Favors Forward Reaction - Products form)"
            shiftDescription = "The current Reaction Quotient Q (${String.format("%.4e", qQuotient)}) is LESS than the equilibrium constant K (${String.format("%.4e", kConstant)}). The forward rate exceeds reverse rate to build product concentrations and restore equilibrium."
        } else if (qQuotient > kConstant) {
            shiftDir = "SHIFTS LEFT (Favors Reverse Reaction - Reactants form)"
            shiftDescription = "The current Reaction Quotient Q (${String.format("%.4e", qQuotient)}) is GREATER than the equilibrium constant K (${String.format("%.4e", kConstant)}). Reverse pathway reactions proceed faster to decompose product excess and restore equilibrium."
        } else {
            shiftDir = "AT EQUILIBRIUM (Steady State)"
            shiftDescription = "The Reaction Quotient Q is exactly equal to the equilibrium constant K ($kConstant). Chemical forward and reverse reaction rates are perfectly balanced."
        }

        // Le Chatelier Gas Mole Shift Principle
        val gasReactants = eq.reactants.filter { it.isGas }.sumOf { it.coefficient }
        val gasProducts = eq.products.filter { it.isGas }.sumOf { it.coefficient }
        val pressureAnalysis = if (gasReactants != gasProducts) {
            if (gasReactants > gasProducts) {
                "• Pressure Increase: Shifts RIGHT towards **Products** side (fewer gas molecules: $gasProducts VS $gasReactants)\n" +
                "• Pressure Decrease: Shifts LEFT towards **Reactants** side (more gas molecules)"
            } else {
                "• Pressure Increase: Shifts LEFT towards **Reactants** side (fewer gas molecules: $gasReactants VS $gasProducts)\n" +
                "• Pressure Decrease: Shifts RIGHT towards **Products** side (more gas molecules)"
            }
        } else {
            "• Pressure Shifts: No effect! Both sides share equal gaseous coefficients ($gasReactants moles)."
        }

        return EquilibriumResult(
            qValue = qQuotient,
            kValue = kConstant,
            shiftDirection = shiftDir,
            explanation = shiftDescription + "\n\n**Le Chatelier's Gas/Volume Stress Analysis:**\n" + pressureAnalysis
        )
    }

    /**
     * Kinetics Reaction Half-Lives and rates
     */
    fun solveKinetics(
        rateConstantK: Double,
        order: Int,
        initialConcentrationA0: Double
    ): KineticsResult {
        if (rateConstantK <= 0.0) throw IllegalArgumentException("Rate speed constant k must be positive.")
        if (initialConcentrationA0 <= 0.0) throw IllegalArgumentException("Initial concentration [A]₀ must be positive.")

        val halfLife: Double
        val formula: String
        when (order) {
            0 -> {
                halfLife = initialConcentrationA0 / (2 * rateConstantK)
                formula = "t_{1/2} = [A]₀ / 2k"
            }
            1 -> {
                halfLife = ln(2.0) / rateConstantK
                formula = "t_{1/2} = ln(2) / k"
            }
            2 -> {
                halfLife = 1.0 / (rateConstantK * initialConcentrationA0)
                formula = "t_{1/2} = 1 / (k * [A]₀)"
            }
            else -> {
                throw IllegalArgumentException("Only 0th, 1st, or 2nd reaction orders are supported for clean deterministic half-life calculation.")
            }
        }

        return KineticsResult(
            halfLife = halfLife,
            formulaUsed = formula,
            explanation = "At standard kinetics rate constant k = $rateConstantK and reactant concentration [A]₀ = $initialConcentrationA0 M, the compound decays with ${order}-order kinetics. The half-life is exactly ${String.format("%.4f", halfLife)} seconds."
        )
    }

    /**
     * Dilution calculator
     */
    fun solveDilution(c1: Double?, v1: Double?, c2: Double?, v2: Double?): SolutionResult {
        // C1 * V1 = C2 * V2
        // Find whichever is null
        val nullCount = listOf(c1, v1, c2, v2).count { it == null }
        if (nullCount != 1) {
            return SolutionResult(
                success = false,
                resultText = "Dilution solver needs exactly 3 known variables and 1 unknown (set unknown to empty/null).",
                stepCalculations = ""
            )
        }

        var resValue = 0.0
        val formula: String
        val outputText: String

        when {
            c1 == null -> {
                resValue = (c2!! * v2!!) / v1!!
                formula = "C1 = (C2 * V2) / V1"
                outputText = "Initial Concentration (C1): **${String.format("%.4f", resValue)} M**"
            }
            v1 == null -> {
                resValue = (c2!! * v2!!) / c1
                formula = "V1 = (C2 * V2) / C1"
                outputText = "Initial Volume (V1): **${String.format("%.4f", resValue)} L** (or ${String.format("%.2f", resValue * 1000)} mL)"
            }
            c2 == null -> {
                resValue = (c1 * v1!!) / v2!!
                formula = "C2 = (C1 * V1) / V2"
                outputText = "Final Concentration (C2): **${String.format("%.4f", resValue)} M**"
            }
            v2 == null -> {
                resValue = (c1 * v1!!) / c2
                formula = "V2 = (C1 * V1) / C2"
                outputText = "Final Volume (V2): **${String.format("%.4f", resValue)} L** (or ${String.format("%.2f", resValue * 1000)} mL)"
            }
            else -> {
                return SolutionResult(success = false, resultText = "Error", stepCalculations = "")
            }
        }

        val stepByStep = "Using dilution conservation law: C1 * V1 = C2 * V2\n" +
                "Rearranging formulas for the unknown yields:\n" +
                "• Formula: $formula\n" +
                "• Calculation: ($c1 * $v1) = ($c2 * $v2)\n" +
                "• Target Result: ${String.format("%.5f", resValue)}"

        return SolutionResult(
            success = true,
            resultText = outputText,
            stepCalculations = stepByStep
        )
    }

    /**
     * Chemical NLP Parser matching natural logic
     */
    fun parseChemistryNlp(query: String): ParsedChemNlpResponse {
        val q = query.trim().lowercase()

        // 1. Dilution matching e.g. "Dilute 2M to 0.5M"
        if (q.contains("dilute") || q.contains("dilution")) {
            val molarities = mutableListOf<Double>()
            val pattern = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*m\\b")
            val matcher = pattern.matcher(q)
            while (matcher.find()) {
                matcher.group(1)?.toDoubleOrNull()?.let { molarities.add(it) }
            }
            if (molarities.size >= 2) {
                val c1 = molarities[0]
                val c2 = molarities[1]
                // assume dilute 1 Liter of C1, solver V2
                val v1 = 1.0
                val c2Final = solveDilution(c1, v1, c2, null)
                return ParsedChemNlpResponse(
                    success = true,
                    queryType = "Dilution Request",
                    interpretation = "How to dilute a $c1 M solution to $c2 M (Assuming starting volume of 1.0 Liter).",
                    outputMarkdown = "### 🧪 Dilution Solver Results\n\n" +
                            "**Inputs Detected:**\n" +
                            "• C1 = $c1 M\n" +
                            "• V1 = 1.0 L\n" +
                            "• C2 = $c2 M\n\n" +
                            c2Final.resultText + "\n\n" +
                            "**Step-by-step Execution:**\n" +
                            c2Final.stepCalculations
                )
            }
        }

        // 2. Gibbs thermodynamic query matching
        if (q.contains("gibbs") || q.contains("free energy") || q.contains("spontaneity")) {
            // Find temperature
            var tempK = 298.15
            val tempPattern = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(k|kelvin)")
            val matcher = tempPattern.matcher(q)
            if (matcher.find()) {
                tempK = matcher.group(1)?.toDoubleOrNull() ?: 298.15
            }
            // Check standard reactions in standard database
            // Let's analyze water gas formula
            val eqStr = when {
                q.contains("water") || q.contains("h2o") -> "2 H2(g) + O2(g) -> 2 H2O(l)"
                q.contains("methane") || q.contains("ch4") -> "CH4(g) + 2 O2(g) -> CO2(g) + 2 H2O(g)"
                else -> "2 H2(g) + O2(g) -> 2 H2O(l)" // default demo
            }

            try {
                val eq = parseReaction(eqStr)
                val res = solveThermodynamics(eq, tempK)
                return ParsedChemNlpResponse(
                    success = true,
                    queryType = "Thermodynamics Analysis",
                    interpretation = "Calculate standard state standard Gibbs free energy change (ΔG) for reaction '$eqStr' at $tempK K.",
                    outputMarkdown = "### ⚡ Gibbs Free Energy calculations\n\n" +
                            "**Reaction:** `$eqStr` at temperature **$tempK K**\n\n" +
                            "**Result Status:**\n" +
                            "• Gibbs Energy Change (ΔG°): **${String.format("%.2f", res.gDelta)} kJ/mol**\n" +
                            "• Enthalpy Change (ΔH°): **${String.format("%.2f", res.hDelta)} kJ/mol**\n" +
                            "• Entropy Change (ΔS°): **${String.format("%.2f", res.sDelta)} J/(mol·K)**\n" +
                            "• Spontaneity Profile: **${res.explanation}**\n\n" +
                            "**Step-by-step Molar Math:**\n" +
                            res.breakdown
                )
            } catch (e: Exception) {
                return ParsedChemNlpResponse(
                    success = false,
                    queryType = "Thermodynamics",
                    interpretation = "Failing thermodynamics parser",
                    outputMarkdown = "Error compiling automatic reactions: ${e.message}"
                )
            }
        }

        // 3. Grams ↔ Mole conversions matching
        // Pattern matches: "10 g NaCl" or "10 grams of H2O" or "moles in 5.5 g of NaOH"
        val massPattern = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(g|grams?)\\s*(?:of\\s+)?([A-Za-z0-9()]+)")
        val massMatcher = massPattern.matcher(q)
        if (massMatcher.find()) {
            val valStr = massMatcher.group(1)
            val compoundStr = massMatcher.group(3)
            if (valStr != null && compoundStr != null) {
                val mass = valStr.toDoubleOrNull() ?: 1.0
                try {
                    // Try to capitalize first letters of tokens to match chemical abbreviations cleanly (e.g. "nacl" to "NaCl")
                    val capCompound = formatChemicalCap(compoundStr)
                    val atoms = parseFormula(capCompound)
                    val mm = calculateMolarMass(atoms)
                    val moles = mass / mm

                    val breakdown = atoms.entries.joinToString("\n") { (sym, count) ->
                        val el = ChemistryDatabase.elements[sym]!!
                        "  - $sym : $count atoms × ${el.atomicMass} = ${String.format("%.3f", el.atomicMass * count)} g/mol"
                    }

                    return ParsedChemNlpResponse(
                        success = true,
                        queryType = "Molar Conversion Request",
                        interpretation = "Determine quantity of moles in $mass grams of chemical compound '$capCompound'.",
                        outputMarkdown = "### 🧪 Grams to Moles Solver\n\n" +
                                "**Result:** $mass g of **$capCompound** is equivalent to **${String.format("%.5f", moles)} mol**.\n\n" +
                                "Molar Mass calculations of `$capCompound`:\n" +
                                breakdown + "\n" +
                                "• **Total Compound weight (M): ${String.format("%.4f", mm)} g/mol**\n\n" +
                                "**Step-by-step Mathematical breakdown:**\n" +
                                "Formula: n = m / M\n" +
                                "n = $mass g / ${String.format("%.4f", mm)} g/mol = **${String.format("%.5f", moles)} moles**"
                    )
                } catch (e: Exception) {
                    // fallthrough to reactions parsing
                }
            }
        }

        // 4. Balanced reactions parsing: "2 H2 + O2 -> 2 H2O"
        if (q.contains("->") || q.contains("→") || q.contains("=")) {
            try {
                val inputReactCap = formatChemicalCap(query)
                val eq = processAndValidateReaction(inputReactCap)
                
                // Let's solve thermodynamic balances if available, else standard balancing summaries
                val thermo = solveThermodynamics(eq, 298.15)
                val extraThermoInfo = if (thermo.isValid) {
                    "### ⚡ Spontaneous Energy Profiles\n" +
                    "• Enthalpy change (ΔH°): **${String.format("%.2f", thermo.hDelta)} kJ/mol**\n" +
                    "• Entropy change (ΔS°): **${String.format("%.2f", thermo.sDelta)} J/(mol·K)**\n" +
                    "• Gibbs energy change (ΔG° at 298.15K): **${String.format("%.2f", thermo.gDelta)} kJ/mol**\n" +
                    "• Spontaneity Profile: **${thermo.explanation}**\n\n"
                } else {
                    "Thermodynamic standard constants are not fully registered for these formulas. However, mass conservation balances beautifully.\n\n"
                }

                return ParsedChemNlpResponse(
                    success = true,
                    queryType = "Stochiometric Balance Verification",
                    interpretation = "Verify stoichiometry and atoms balances for reaction: $inputReactCap",
                    outputMarkdown = "### ✅ Mass Conservation Verified\n" +
                            "This reaction obeys **Universal Conservation of Atoms**!\n\n" +
                            "**Summary of Participant Coefficients:**\n" +
                            "• Reactants: " + eq.reactants.joinToString(" and ") { "${it.coefficient} mol of `${it.formula}`" } + "\n" +
                            "• Products: " + eq.products.joinToString(" and ") { "${it.coefficient} mol of `${it.formula}`" } + "\n\n" +
                            extraThermoInfo +
                            "**Analytical Status: VALIDATED**"
                )
            } catch (e: Exception) {
                return ParsedChemNlpResponse(
                    success = false,
                    queryType = "Chemical Formula Parse Engine",
                    interpretation = "Parsed input contains unrecognized abbreviations or incorrect mathematical coefficients.",
                    outputMarkdown = "❌ **Parsing or Validation Error:**\n\n${e.message}\n\nPlease enter fully-specified formulas with initial uppercase characters (e.g., NaCl, H2O, Ca(OH)2)."
                )
            }
        }

        // Catch-all ambiguous
        return ParsedChemNlpResponse(
            success = false,
            queryType = "Ambigious Chemistry Inquiry",
            interpretation = "Query does not match standard patterns (molar mass, stochiometry, kinetics, or solutions thermodynamics).",
            outputMarkdown = "### ⚠️ Clarification Request\n\n" +
                    "We did not recognize a clear mathematical solver pathway for: *\"$query\"*.\n\n" +
                    "**Try these deterministic scientific examples offline:**\n" +
                    "1. Molar calculations: **\"10 g NaCl in mol\"**\n" +
                    "2. Balance & Thermodynamics: **\"2 H2 + O2 -> 2 H2O\"**\n" +
                    "3. Concentration Dilution: **\"Dilute 2M to 0.5M\"**\n" +
                    "4. Thermodynamic Spontaneity: **\"Gibbs free energy of methane at 350K\"**"
        )
    }

    /**
     * Clever recursive formula formatter to make lowercase letters uppercase when needed
     * E.g. "nacl" to "NaCl", "h2o" to "H2O", "ca(oh)2" to "Ca(OH)2"
     */
    private fun formatChemicalCap(input: String): String {
        val mappingSymbols = ChemistryDatabase.elements.keys.sortedByDescending { it.length }
        var result = input
        // Simple heuristic: capitalize all matching element symbols
        for (symbol in mappingSymbols) {
            val lower = symbol.lowercase()
            // Only replace if they match boundaries or boundary bounds
            val regex = "(?i)\\b$lower\\b".toRegex()
            result = result.replace(regex, symbol)
        }
        // Also ensure individual symbols like H, O, N etc are capitalized
        val stringBuilder = StringBuilder()
        var lastChar: Char? = null
        for (i in result.indices) {
            val c = result[i]
            if (c.isLetter()) {
                val symbolMatches = mappingSymbols.filter { it.startsWith(c, ignoreCase = true) }
                if (symbolMatches.isNotEmpty() && (lastChar == null || !lastChar.isLetter())) {
                    // capitalize the first letter
                    stringBuilder.append(c.uppercaseChar())
                } else {
                    stringBuilder.append(c)
                }
            } else {
                stringBuilder.append(c)
            }
            lastChar = c
        }
        return stringBuilder.toString()
    }
}

// --- SOLVER RESULTS STRUCTURES ---

data class StoichiometryResult(
    val limitingReagent: String,
    val extentOfReaction: Double,
    val excessStatus: List<String>,
    val productYields: List<String>
)

data class ThermoResult(
    val isValid: Boolean,
    val hDelta: Double,
    val sDelta: Double,
    val gDelta: Double,
    val isSpontaneous: Boolean,
    val explanation: String,
    val breakdown: String = ""
)

data class EquilibriumResult(
    val qValue: Double,
    val kValue: Double,
    val shiftDirection: String,
    val explanation: String
)

data class KineticsResult(
    val halfLife: Double,
    val formulaUsed: String,
    val explanation: String
)

data class SolutionResult(
    val success: Boolean,
    val resultText: String,
    val stepCalculations: String
)

data class ParsedChemNlpResponse(
    val success: Boolean,
    val queryType: String,
    val interpretation: String,
    val outputMarkdown: String
)
