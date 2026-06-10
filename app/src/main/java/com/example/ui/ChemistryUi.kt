package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.calculator.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChemistryTabScreen(viewModel: CalculatorViewModel) {
    var activeSubModule by remember { mutableStateOf(0) } // 0: NLP search, 1: Stoichiometry, 2: Solutions, 3: Thermodynamics, 4: Periodic Table

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Module Row Tabs
        ScrollableTabRow(
            selectedTabIndex = activeSubModule,
            edgePadding = 12.dp,
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
        ) {
            Tab(
                selected = activeSubModule == 0,
                onClick = { activeSubModule = 0 },
                text = { Text("🧠 Smart Lab NLP", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Search, "NLP") }
            )
            Tab(
                selected = activeSubModule == 1,
                onClick = { activeSubModule = 1 },
                text = { Text("⚖️ Stoichiometry", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.PlayArrow, "Stoich") }
            )
            Tab(
                selected = activeSubModule == 2,
                onClick = { activeSubModule = 2 },
                text = { Text("💧 Solutions & Dilution", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Refresh, "Solutions") }
            )
            Tab(
                selected = activeSubModule == 3,
                onClick = { activeSubModule = 3 },
                text = { Text("⚡ Thermo & Kinetics", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Warning, "Thermo") }
            )
            Tab(
                selected = activeSubModule == 4,
                onClick = { activeSubModule = 4 },
                text = { Text("🔬 Periodic elements", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Info, "Periodic") }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(16.dp)
        ) {
            when (activeSubModule) {
                0 -> ChemistryNlpSubScreen(viewModel)
                1 -> StoichiometrySubScreen()
                2 -> SolutionsSubScreen()
                3 -> ThermoKineticsSubScreen()
                4 -> PeriodicTableSubScreen()
            }
        }
    }
}

// --- SUB-SCREEN 0: INTUITION NLP INPUT ENGINE ---

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChemistryNlpSubScreen(viewModel: CalculatorViewModel) {
    val chemistryQuery by viewModel.chemistryQueryState.collectAsStateWithLifecycle()
    val chemistryResult by viewModel.chemistryNlpResultState.collectAsStateWithLifecycle()

    var textInput by remember { mutableStateOf(chemistryQuery) }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val presetQueries = listOf(
        "10 g H2O in mol",
        "2 H2(g) + O2(g) -> 2 H2O(l)",
        "Dilute 12M to 3M",
        "Gibbs free energy of methane at 350K",
        "50 grams of NaOH"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "🧠 Smart Chemical NLP Parser",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Enter raw chemical formulas, balanced reactions, dilution requests, or mass conversions below. Our deterministic engine normalizes molecular properties completely offline without AI calculations.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        OutlinedTextField(
            value = textInput,
            onValueChange = {
                textInput = it
            },
            label = { Text("Search / Equation / Conversion... (e.g. '10 g NaCl in mol')") },
            maxLines = 3,
            singleLine = false,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("chem_nlp_input"),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                if (textInput.isNotBlank()) {
                    viewModel.processChemistryNlpQuery(textInput)
                }
                focusManager.clearFocus()
            }),
            trailingIcon = {
                if (textInput.isNotBlank()) {
                    IconButton(onClick = {
                        textInput = ""
                    }) {
                        Icon(Icons.Default.Clear, "Clear")
                    }
                }
            }
        )

        Button(
            onClick = {
                if (textInput.isNotBlank()) {
                    viewModel.processChemistryNlpQuery(textInput)
                }
                focusManager.clearFocus()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("chem_nlp_solve_button")
        ) {
            Icon(Icons.Default.Search, contentDescription = "Solve")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Verify & Solve Offline")
        }

        // Suggestions Group
        Text(
            "💡 Try Precomputed Scientific Presets:",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            presetQueries.forEach { preset ->
                SuggestionChip(
                    onClick = {
                        textInput = preset
                        viewModel.processChemistryNlpQuery(preset)
                    },
                    label = { Text(preset, fontSize = 11.sp) },
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }

        Divider()

        // Results Card Output
        chemistryResult?.let { response ->
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = if (response.success) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                )
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (response.success) "✨ VALIDATED CHEMICAL NLP MATCH" else "⚠️ SYSTEM INPUT ERROR",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = if (response.success) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            letterSpacing = 1.sp
                        )
                        IconButton(onClick = {
                            clipboardManager.setText(AnnotatedString(response.outputMarkdown))
                            Toast.makeText(context, "Copied analysis results!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.Share, "Share", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Text(
                        text = response.interpretation,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = response.outputMarkdown,
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag("chem_nlp_result_text")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (response.success) "Engine Status: 🟢 DETAILED VALIDATED" else "Engine Status: 🔴 REJECTED / INVALID INPUT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (response.success) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

// --- SUB-SCREEN 1: GENERAL STOICHIOMETRY ENGINE ---

@Composable
fun StoichiometrySubScreen() {
    var reactionInput by remember { mutableStateOf("2 H2 + O2 -> 2 H2O") }
    var reactantsInputs = remember { mutableStateMapOf<String, String>() }
    var solvedOutput by remember { mutableStateOf<StoichiometryResult?>(null) }
    var parsedEqState by remember { mutableStateOf<parsedEquation?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val presetEquations = listOf(
        "2 H2 + O2 -> 2 H2O",
        "CH4 + 2 O2 -> CO2 + 2 H2O",
        "CaCO3 -> CaO + CO2",
        "C2H5OH + 3 O2 -> 2 CO2 + 3 H2O",
        "NaCl + AgNO3 -> AgCl + NaNO3"
    )

    fun setupReactants(equationStr: String) {
        try {
            val parsed = ChemistryEngine.parseReaction(equationStr)
            parsedEqState = parsed
            reactantsInputs.clear()
            for (r in parsed.reactants) {
                reactantsInputs[r.formula] = "10.0" // default mass
            }
            errorMessage = null
            solvedOutput = null
        } catch (e: Exception) {
            errorMessage = e.message
            parsedEqState = null
        }
    }

    // Initialize first preset on load
    LaunchedEffect(Unit) {
        setupReactants("2 H2 + O2 -> 2 H2O")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "⚖️ Stoichiometry Yields & Limits",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        OutlinedTextField(
            value = reactionInput,
            onValueChange = {
                reactionInput = it
            },
            label = { Text("Balanced Equation (e.g. '2 H2 + O2 -> 2 H2O')") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { setupReactants(reactionInput) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Configure Reactants")
            }
        }

        // Suggestions Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(presetEquations) { preset ->
                SuggestionChip(
                    onClick = {
                        reactionInput = preset
                        setupReactants(preset)
                    },
                    label = { Text(preset, fontSize = 11.sp) }
                )
            }
        }

        errorMessage?.let { err ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(
                    text = "⚠️ Equation Error: $err\n\nPlease ensure your reaction coefficients and formulas are fully balanced before entering reactant masses.",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        parsedEqState?.let { eq ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "🧪 ENTER REACTANT MASS INSTANCES (grams)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    eq.reactants.forEach { reactant ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                reactant.formula,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(80.dp),
                                fontSize = 14.sp
                            )
                            OutlinedTextField(
                                value = reactantsInputs[reactant.formula] ?: "",
                                onValueChange = { reactantsInputs[reactant.formula] = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                label = { Text("Mass in grams") }
                            )
                        }
                    }

                    Button(
                        onClick = {
                            try {
                                val masses = reactantsInputs.mapValues { (_, v) -> v.toDoubleOrNull() ?: 0.0 }
                                solvedOutput = ChemistryEngine.solveStoichiometry(eq, masses)
                                errorMessage = null
                            } catch (e: Exception) {
                                errorMessage = e.message
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Solve Balanced Yields & Excess")
                    }
                }
            }
        }

        solvedOutput?.let { res ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "🔬 SOLVER DETERMINISTIC SUMMARY",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🚨 Limiting Reagent: ", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(
                                res.limitingReagent,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Text(
                        "Molar extent of reaction completed: **${String.format("%.4e", res.extentOfReaction)} mol**",
                        fontSize = 13.sp
                    )

                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    Text("📊 Product Yields (Theoretical):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    res.productYields.forEach { Text(it, fontSize = 13.sp) }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    Text("⚙️ Reactant Excess Allocations:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    res.excessStatus.forEach { Text(it, fontSize = 13.sp) }
                    
                    Text(
                        text = "Calculations validated in strict offline adherence to the Conservation of Mass.",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// --- SUB-SCREEN 2: SOLUTION CHEMISTRY MODULE ---

@Composable
fun SolutionsSubScreen() {
    var tabSelected by remember { mutableStateOf(0) } // 0: Dilution (C1V1 = C2V2), 1: Molarity (M = n/V)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TabRow(selectedTabIndex = tabSelected) {
            Tab(selected = tabSelected == 0, onClick = { tabSelected = 0 }, text = { Text("Dilution Solver") })
            Tab(selected = tabSelected == 1, onClick = { tabSelected = 1 }, text = { Text("Molarity & Prep Solver") })
        }

        if (tabSelected == 0) {
            // Dilutions Screen
            var c1 by remember { mutableStateOf("12.0") }
            var v1 by remember { mutableStateOf("") }
            var c2 by remember { mutableStateOf("3.0") }
            var v2 by remember { mutableStateOf("1.0") }
            var dilutionResult by remember { mutableStateOf<String?>(null) }
            var dilutionSteps by remember { mutableStateOf<String?>(null) }

            Text(
                "Dilution Law Solver (C₁V₁ = C₂V₂)",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Text(
                "Leave exactly ONE variable empty to determine its exact value. Dilutions conserve solute moles exactly.",
                fontSize = 13.sp
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = c1,
                    onValueChange = { c1 = it },
                    label = { Text("C1: Initial concentration (M)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = v1,
                    onValueChange = { v1 = it },
                    label = { Text("V1: Initial volume (L)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = c2,
                    onValueChange = { c2 = it },
                    label = { Text("C2: Target concentration (M)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = v2,
                    onValueChange = { v2 = it },
                    label = { Text("V2: Target volume (L)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        val nC1 = c1.toDoubleOrNull()
                        val nV1 = v1.toDoubleOrNull()
                        val nC2 = c2.toDoubleOrNull()
                        val nV2 = v2.toDoubleOrNull()

                        val res = ChemistryEngine.solveDilution(nC1, nV1, nC2, nV2)
                        dilutionResult = res.resultText
                        dilutionSteps = res.stepCalculations
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Solve Dilution Variable")
                }
            }

            dilutionResult?.let { result ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("🧪 TARGET UNKNOWN VALUE SOLVED:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(result, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        dilutionSteps?.let { steps ->
                            Divider(modifier = Modifier.padding(vertical = 4.dp))
                            Text("Step-by-step mathematical rearranged formulation:", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                            Text(steps, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        } else {
            // Molarity Screen
            var compFormula by remember { mutableStateOf("NaCl") }
            var massInput by remember { mutableStateOf("58.44") }
            var volInput by remember { mutableStateOf("1.0") }
            var molarityResultState by remember { mutableStateOf<String?>(null) }

            Text(
                "Molarity Calculation Engine [M = n / V]",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            OutlinedTextField(
                value = compFormula,
                onValueChange = { compFormula = it },
                label = { Text("Solute Compound chemical formula (e.g., NaCl, NaOH)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = massInput,
                onValueChange = { massInput = it },
                label = { Text("Mass of Solute in grams (m)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = volInput,
                onValueChange = { volInput = it },
                label = { Text("Total Solution volume in Liters (V)") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    try {
                        val atoms = ChemistryEngine.parseFormula(compFormula)
                        val mm = ChemistryEngine.calculateMolarMass(atoms)
                        val mass = massInput.toDoubleOrNull() ?: 0.0
                        val vol = volInput.toDoubleOrNull() ?: 1.0

                        val moles = mass / mm
                        val molarity = moles / vol

                        molarityResultState = """
                            • Molar Mass (M) of '$compFormula': **${String.format("%.4f", mm)} g/mol**
                            • Quantity Solute: **${String.format("%.4f", moles)} moles**
                            • Solution Concentration Molarity: **${String.format("%.4f", molarity)} mol/L (M)**
                            
                            **Ion concentrations upon dissolving:**
                            - Assuming complete dissociation:
                            - Concentration cations/anions is matched to molar stoichiometric coefficients.
                        """.trimIndent()
                    } catch (e: Exception) {
                        molarityResultState = "❌ Configuration error: ${e.message}"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Solve Solution Molarity")
            }

            molarityResultState?.let { res ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("🔬 Solutions Results Chemistry", fontWeight = FontWeight.Bold)
                        Text(res, fontSize = 13.sp, lineHeight = 20.sp)
                    }
                }
            }
        }
    }
}

// --- SUB-SCREEN 3: THERMODYNAMICS & KINETICS ---

@Composable
fun ThermoKineticsSubScreen() {
    var moduleSelect by remember { mutableStateOf(0) } // 0: Spontaneity ΔG, 1: Half-Life Kinetics

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TabRow(selectedTabIndex = moduleSelect) {
            Tab(selected = moduleSelect == 0, onClick = { moduleSelect = 0 }, text = { Text("Gibbs Thermodynamics") })
            Tab(selected = moduleSelect == 1, onClick = { moduleSelect = 1 }, text = { Text("Arrhenius & Kinetics") })
        }

        if (moduleSelect == 0) {
            var thermoReactionInput by remember { mutableStateOf("CH4(g) + 2 O2(g) -> CO2(g) + 2 H2O(g)") }
            var tempKInput by remember { mutableStateOf("298.15") }
            var thermoResultState by remember { mutableStateOf<ThermoResult?>(null) }
            var thermoErr by remember { mutableStateOf<String?>(null) }

            val presets = listOf(
                "CH4(g) + 2 O2(g) -> CO2(g) + 2 H2O(g)",
                "2 H2(g) + O2(g) -> 2 H2O(l)",
                "CaCO3(s) -> CaO(s) + CO2(g)",
                "NH4NO3(s) -> NH3(g) + HNO3(g)" // standard reaction tests
            )

            Text("Gibbs Spontaneity Energy Solver (ΔG = ΔH − TΔS)", fontWeight = FontWeight.Bold, fontSize = 16.sp)

            OutlinedTextField(
                value = thermoReactionInput,
                onValueChange = { thermoReactionInput = it },
                label = { Text("Reaction String (Including state indicators, e.g. H2O(l) or CO2(g))") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = tempKInput,
                onValueChange = { tempKInput = it },
                label = { Text("Temperature in Kelvin (K)") },
                modifier = Modifier.fillMaxWidth()
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(presets) { p ->
                    SuggestionChip(
                        onClick = {
                            thermoReactionInput = p
                            tempKInput = "298.15"
                        },
                        label = { Text(p, fontSize = 11.sp) }
                    )
                }
            }

            Button(
                onClick = {
                    try {
                        val eq = ChemistryEngine.parseReaction(thermoReactionInput)
                        val tK = tempKInput.toDoubleOrNull() ?: 298.15
                        val solved = ChemistryEngine.solveThermodynamics(eq, tK)
                        thermoResultState = solved
                        thermoErr = null
                    } catch (e: Exception) {
                        thermoErr = e.message
                        thermoResultState = null
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Examine Thermodynamics Spontaneity")
            }

            thermoErr?.let { err ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text("Error: $err", modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }

            thermoResultState?.let { res ->
                if (!res.isValid) {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Text(res.explanation, modifier = Modifier.padding(14.dp), fontSize = 13.sp)
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("⚡ Spontaneous Energy Profiles", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                            
                            Row {
                                Text("Thermodynamic Feasibility: ", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text(
                                    res.explanation,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (res.isSpontaneous) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                                )
                            }

                            Divider()

                            Text(res.breakdown, fontFamily = FontFamily.SansSerif, fontSize = 13.sp, lineHeight = 20.sp)
                        }
                    }
                }
            }
        } else {
            // Kinetics Screen
            var kValueStr by remember { mutableStateOf("0.023") }
            var initialA0Str by remember { mutableStateOf("1.5") }
            var rxnOrder by remember { mutableStateOf(1) } // 0, 1, 2
            var kineticsResultState by remember { mutableStateOf<KineticsResult?>(null) }
            var kErr by remember { mutableStateOf<String?>(null) }

            Text("Reaction Half-Life Deterministic Solver", fontWeight = FontWeight.Bold, fontSize = 16.sp)

            OutlinedTextField(
                value = kValueStr,
                onValueChange = { kValueStr = it },
                label = { Text("Reaction rate constant k (seconds⁻¹)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = initialA0Str,
                onValueChange = { initialA0Str = it },
                label = { Text("Initial Concentration [A]₀ (M)") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Select Reaction Order Kinetics:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                listOf(0, 1, 2).forEach { o ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = rxnOrder == o, onClick = { rxnOrder = o })
                        Text("${o}-order", fontSize = 13.sp)
                    }
                }
            }

            Button(
                onClick = {
                    try {
                        val k = kValueStr.toDoubleOrNull() ?: 1.0
                        val a0 = initialA0Str.toDoubleOrNull() ?: 1.0
                        val solved = ChemistryEngine.solveKinetics(k, rxnOrder, a0)
                        kineticsResultState = solved
                        kErr = null
                    } catch (e: Exception) {
                        kErr = e.message
                        kineticsResultState = null
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Solve Half-Life Kinetics")
            }

            kErr?.let { err ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text("Error: $err", modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }

            kineticsResultState?.let { res ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("⏳ Kinetics solved values", fontWeight = FontWeight.Bold)
                        Text("• Arrhenius Half Life: **${String.format("%.4f", res.halfLife)} seconds**", fontSize = 14.sp)
                        Text("• Exact Formulation Used: `${res.formulaUsed}`", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                        Text(res.explanation, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

// --- SUB-SCREEN 4: INTERACTIVE ELEMENTS EXPLORER ---

@Composable
fun PeriodicTableSubScreen() {
    val elementList = remember { ChemistryDatabase.elements.values.toList().sortedBy { it.atomicNumber } }
    var selectedElement by remember { mutableStateOf<Element?>(elementList.firstOrNull()) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "🔬 Periodic Database Lookup (Offline)",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        // Element quick specs card
        selectedElement?.let { el ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when (el.chemicalGroup) {
                        "Alkali Metal" -> Color(0xFFFFF3E0)
                        "Alkaline Earth Metal" -> Color(0xFFF3E5F5)
                        "Halogen" -> Color(0xFFE8F5E9)
                        "Noble Gas" -> Color(0xFFE0F7FA)
                        "Metalloid" -> Color(0xFFECEFF1)
                        else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    }
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${el.name} (${el.symbol})",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Group: ${el.chemicalGroup} | Standard State: ${el.standardState}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Atomic number huge display
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(
                                text = el.atomicNumber.toString(),
                                style = androidx.compose.ui.text.TextStyle(
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontFamily = FontFamily.Monospace
                                ),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Electronegativity (Pauling):", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                            Text("${el.electronegativity}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Atomic Mass (Molar weight):", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                            Text("${el.atomicMass} g/mol", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Common Valences:", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                            Text(el.commonValence.joinToString(", "), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Element grid selection list
        Text(
            "TAPs Elements to Explore Properties:",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 75.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(elementList) { el ->
                Card(
                    onClick = { selectedElement = el },
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (selectedElement == el) MaterialTheme.colorScheme.primary else Color.Transparent
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedElement == el) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                    ),
                    modifier = Modifier.height(75.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = el.symbol,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = el.name,
                            fontSize = 11.sp,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
