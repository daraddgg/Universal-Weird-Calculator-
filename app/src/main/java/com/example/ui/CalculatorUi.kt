package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
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
import com.example.calculator.ComparisonObject
import com.example.calculator.WeirdCalculatorEngine
import com.example.data.ConversionHistory
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigationScreen(viewModel: CalculatorViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "🌌 WeirdCalc",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.SansSerif,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        },
        bottomBar = {
            NavigationBar(
                windowInsets = WindowInsets.navigationBars,
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Text("⚖️", fontSize = 20.sp) },
                    label = { Text("Converter", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_tab_converter")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Text("🧠", fontSize = 20.sp) },
                    label = { Text("AI Parser", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_tab_ai")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Text("🔍", fontSize = 20.sp) },
                    label = { Text("Explore DB", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_tab_explore")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Text("🗄️", fontSize = 20.sp) },
                    label = { Text("History", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_tab_history")
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Text("🧪", fontSize = 20.sp) },
                    label = { Text("Chem Lab", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_tab_chemistry")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> ConverterTabScreen(viewModel)
                1 -> AiModeTabScreen(viewModel)
                2 -> CategoryExplorerTabScreen(viewModel)
                3 -> HistoryTabScreen(viewModel)
                4 -> ChemistryTabScreen(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ConverterTabScreen(viewModel: CalculatorViewModel) {
    val category by viewModel.categoryState.collectAsStateWithLifecycle()
    val fromUnit by viewModel.fromUnitState.collectAsStateWithLifecycle()
    val toUnit by viewModel.toUnitState.collectAsStateWithLifecycle()
    val inputValue by viewModel.inputValueState.collectAsStateWithLifecycle()
    val calculationResult by viewModel.calculationResult.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val focusManager = LocalFocusManager.current

    var showFromDropdown by remember { mutableStateOf(false) }
    var showToDropdown by remember { mutableStateOf(false) }

    val categoryIcons = mapOf(
        "Length" to "📏",
        "Weight / Mass" to "🐘",
        "Time" to "🕰️",
        "Speed" to "🐆",
        "Energy" to "⚡",
        "Digital Storage" to "💾",
        "Money" to "💵",
        "Astronomy" to "🪐",
        "Human Body" to "🧍",
        "Historical" to "🏛️"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Category scrollable selection row
        item {
            Text(
                text = "Pick Category",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(WeirdCalculatorEngine.categories) { catName ->
                    val isSelected = catName == category
                    val emoji = categoryIcons[catName] ?: "📐"
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setCategory(catName) },
                        label = { Text("$emoji $catName", fontSize = 13.sp) },
                        modifier = Modifier.testTag("chip_$catName")
                    )
                }
            }
        }

        // Numerical Input & Dropdowns Block inside an Elegant elevated Card
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Number Input
                    OutlinedTextField(
                        value = inputValue,
                        onValueChange = { viewModel.setInputValue(it) },
                        label = { Text("Enter Numeric Value", fontWeight = FontWeight.Bold) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("converter_number_input")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Unit Picking Selector Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // From Unit Dropdown
                        Box(modifier = Modifier.weight(1f)) {
                            val matchingUnits = WeirdCalculatorEngine.unitsMap[category] ?: emptyList()
                            val fromDisplayName = matchingUnits.find { it.key == fromUnit }?.displayName ?: fromUnit
                            Button(
                                onClick = { showFromDropdown = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("from_unit_selector"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            ) {
                                Text(fromDisplayName, maxLines = 1, fontSize = 12.sp)
                                Icon(Icons.Default.KeyboardArrowDown, "Down")
                            }
                            DropdownMenu(
                                expanded = showFromDropdown,
                                onDismissRequest = { showFromDropdown = false }
                            ) {
                                matchingUnits.forEach { u ->
                                    DropdownMenuItem(
                                        text = { Text(u.displayName) },
                                        onClick = {
                                            viewModel.setFromUnit(u.key)
                                            showFromDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        // Arrow
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = "to",
                            modifier = Modifier.align(Alignment.CenterVertically),
                            tint = MaterialTheme.colorScheme.primary
                        )

                        // To Unit Dropdown
                        Box(modifier = Modifier.weight(1f)) {
                            val matchingUnits = WeirdCalculatorEngine.unitsMap[category] ?: emptyList()
                            val toDisplayName = matchingUnits.find { it.key == toUnit }?.displayName ?: toUnit
                            Button(
                                onClick = { showToDropdown = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("to_unit_selector"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            ) {
                                Text(toDisplayName, maxLines = 1, fontSize = 12.sp)
                                Icon(Icons.Default.KeyboardArrowDown, "Down")
                            }
                            DropdownMenu(
                                expanded = showToDropdown,
                                onDismissRequest = { showToDropdown = false }
                            ) {
                                matchingUnits.forEach { u ->
                                    DropdownMenuItem(
                                        text = { Text(u.displayName) },
                                        onClick = {
                                            viewModel.setToUnit(u.key)
                                            showToDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Insert custom button to trigger database insertion
                    Button(
                        onClick = {
                            viewModel.saveConversionToHistory()
                            Toast.makeText(context, "Saved to App logs!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_conversion_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Star, "Save")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Log & Save to History", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }

        // Output Display Card
        item {
            calculationResult?.let { result ->
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Scientific Result Bubble
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "🧪 Professional Conversion",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(result.resultString))
                                        Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(Icons.Default.Share, "Copy", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Text(
                                text = result.resultString,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(vertical = 4.dp).testTag("scientific_result")
                            )
                        }
                    }

                    // Weird Real-world Comparisons
                    Text(
                        "🤪 Weird Real-World Counterparts",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    result.comparisons.forEach { itemText ->
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize(animationSpec = spring()),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = itemText,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Educational Fact
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("💡", fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Weird Educational Fact:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                                Text(
                                    result.fact,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }

                    // Dynamic Shareable Social Media Card Showcase!
                    Text(
                        "🎨 Visual Social Media Card Previews",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // A breathtakingly styled canvas card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF8E2DE2),
                                        Color(0xFF4A00E0)
                                    )
                                )
                            ),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "🌌 UNIVERSAL WEIRD CALCULATION",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            val cleanShortResult = result.comparisons.firstOrNull()?.substringAfter(" ") ?: result.resultString
                            Text(
                                "\"My $inputValue $fromUnit equals roughly $cleanShortResult!\"",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center,
                                fontFamily = FontFamily.Serif
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            Text(
                                "💡 ${result.fact}",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString("My $inputValue $fromUnit is equal to $cleanShortResult! Created via Weird Calculator 🪐"))
                                    Toast.makeText(context, "Card shared to gallery & text copied to clipboard!", Toast.LENGTH_LONG).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                                modifier = Modifier.testTag("social_share_button")
                            ) {
                                Text("🪄 Share to Socials", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiModeTabScreen(viewModel: CalculatorViewModel) {
    val nluQuery by viewModel.nluQueryState.collectAsStateWithLifecycle()
    val nluResult by viewModel.nluResultState.collectAsStateWithLifecycle()
    val nluParsed by viewModel.nluParsedState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val clipboardManager = LocalClipboardManager.current
    var textInput by remember { mutableStateOf(nluQuery) }

    val presetQueries = listOf(
        "1000 meters",
        "500 kg in elephants",
        "1 TB in photos",
        "2 hours in heartbeats",
        "100 dollars in pizzas",
        "how many football fields is 3 km",
        "energy of 1 kWh in phone charges"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title block
        Text(
            text = "🧠 Smart Natural Language & Reality Engine",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Type fully-formed natural inquiries below. The deterministic NLU engine normalizes quantities, matches unit synonyms offline, and delivers mathematically exact comparisons without AI hallucination.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Text input field with instant NLU updates
        OutlinedTextField(
            value = textInput,
            onValueChange = {
                textInput = it
                viewModel.processNluQuery(it) // Live results while typing
            },
            label = { Text("Search / Convert... (e.g., '1000 meters' or '100 dollars in pizzas')") },
            maxLines = 3,
            singleLine = false,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("ai_text_input"),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                if (textInput.isNotBlank()) {
                    viewModel.processNluQuery(textInput)
                }
                focusManager.clearFocus()
            }),
            trailingIcon = {
                if (textInput.isNotBlank()) {
                    IconButton(onClick = {
                        textInput = ""
                        viewModel.processNluQuery("")
                    }) {
                        Icon(Icons.Default.Clear, "Clear")
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        // Preset Quick Chips selection grid
        Text(
            "💡 Tap quick comparison presets:",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            presetQueries.forEach { pq ->
                SuggestionChip(
                    onClick = {
                        textInput = pq
                        viewModel.processNluQuery(pq)
                    },
                    label = { Text(pq, fontSize = 11.sp, maxLines = 1) },
                    modifier = Modifier.padding(bottom = 4.dp).testTag("suggest_${pq.take(15)}")
                )
            }
        }

        // Active output visualization depending on the result success state
        nluResult?.let { result ->
            if (!result.success) {
                // Return an elegant "Clarification Needed" or "Insufficient Data" Warning Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚠️", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Ambiguous input / Insufficient Data",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                result.simpleExplanation,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            } else {
                // Success Case - Master display cards block
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                    // 1. Direct conversion result
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "✨ EXACT CONVERSION RESULT",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.sp
                                )
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(result.resultText))
                                        Toast.makeText(context, "Copied result to clipboard!", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(Icons.Default.Share, "Copy", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Text(
                                text = result.resultText,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.testTag("ai_result_text")
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = result.simpleExplanation,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    // 2. Structured NLU Parser Normalized output JSON (as requested)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                "🖥️ NLP Engine Normalized Parser Formats",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = result.structuredFormat,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .padding(10.dp)
                            )
                        }
                    }

                    // 3. Real-world comparisons
                    Text(
                        "🍎 Comparative Real-World Analogies",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    result.comparisons.forEach { comp ->
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = comp,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // 4. Fun Fact
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("💡", fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Scientifically Accurate Fun Fact:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                                Text(
                                    text = result.fact,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryExplorerTabScreen(viewModel: CalculatorViewModel) {
    var searchInput by remember { mutableStateOf("") }
    var selectedToConvertItem by remember { mutableStateOf<ComparisonObject?>(null) }
    var customVolumeScaleInput by remember { mutableStateOf("10") }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val filteredList = remember(searchInput) {
        if (searchInput.isBlank()) {
            WeirdCalculatorEngine.dbObjects
        } else {
            WeirdCalculatorEngine.dbObjects.filter {
                it.name.contains(searchInput, ignoreCase = true) ||
                        it.category.contains(searchInput, ignoreCase = true) ||
                        it.description.contains(searchInput, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "📚 Fun Comparison Database Explorer",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Browse physical sizes, weights, energy limits, and cool facts about famous items to perform active scale calculations.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Search Field
        OutlinedTextField(
            value = searchInput,
            onValueChange = { searchInput = it },
            label = { Text("Search Animals, Landmarks, Technology...") },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, "Search") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("db_search_input")
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            items(filteredList) { item ->
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(item.icon, fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    item.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "Category: ${item.category} • Base Standard value: ${item.baseValue}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = item.description,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Row(modifier = Modifier.padding(10.dp)) {
                                Text("💡", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    item.fact,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Active calculator button
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { selectedToConvertItem = item },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.testTag("scale_calculate_${item.name.replace(" ", "_")}")
                        ) {
                            Text("⚡ Try Scale Integration", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Animated Dialog/Bottom Sheet Simulation for scaling
        selectedToConvertItem?.let { obj ->
            AlertDialog(
                onDismissRequest = { selectedToConvertItem = null },
                confirmButton = {
                    Button(
                        onClick = {
                            val inVal = customVolumeScaleInput.toDoubleOrNull() ?: 1.0
                            val scaleOutput = inVal / obj.baseValue
                            val roundedRepresentation = String.format("%,.4f", scaleOutput)
                            val finalShareableText = "$inVal standard units matches $roundedRepresentation times of ${obj.name}!"
                            clipboardManager.setText(AnnotatedString(finalShareableText))
                            Toast.makeText(context, "Calculated! Share message copied to clipboard.", Toast.LENGTH_LONG).show()
                            selectedToConvertItem = null
                        }
                    ) {
                        Text("Calculate & Copy")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedToConvertItem = null }) {
                        Text("Close")
                    }
                },
                title = { Text("Scale custom targets!", fontWeight = FontWeight.Black) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("How many units fit into a target? Enter target value below:")
                        OutlinedTextField(
                            value = customVolumeScaleInput,
                            onValueChange = { customVolumeScaleInput = it },
                            label = { Text("Target Quantity") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Text(
                            "Formula: Target / (${obj.name} base value of ${obj.baseValue})",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            )
        }
    }
}

private val ComparisonObject.valStringRepresent: String
    get() = name

@Composable
fun HistoryTabScreen(viewModel: CalculatorViewModel) {
    val history by viewModel.allHistory.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedSegment by remember { mutableStateOf(0) } // 0 = All history, 1 = Favorites

    // Statistics numbers calculation
    val totalConversions = history.size
    val totalFavs = favorites.size
    val mostPopularCategory = remember(history) {
        if (history.isEmpty()) "None"
        else history.groupBy { it.category }.maxByOrNull { it.value.size }?.key ?: "None"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "📊 App Logs, Metrics & Stats",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Grid of Stats Numbers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ElevatedCard(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Conversions", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("$totalConversions", fontSize = 20.sp, fontWeight = FontWeight.Black)
                }
            }

            ElevatedCard(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Favorites", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("$totalFavs", fontSize = 20.sp, fontWeight = FontWeight.Black)
                }
            }

            ElevatedCard(
                modifier = Modifier.weight(1.2f),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Top Category", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(mostPopularCategory, fontSize = 14.sp, fontWeight = FontWeight.Black, maxLines = 1)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Visual Graph/Chart using Compose drawing
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "Conversion Category distribution:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Standard customized horizontal stacking bar diagram representation using custom drawing Canvas modifier
                val barColors = listOf(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.secondary,
                    MaterialTheme.colorScheme.tertiary,
                    MaterialTheme.colorScheme.error,
                    MaterialTheme.colorScheme.inversePrimary
                )
                val catCounts = remember(history) {
                    history.groupBy { it.category }.mapValues { it.value.size }
                }
                val totalCount = catCounts.values.sum()

                if (totalCount == 0) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No logs yet. Perform some calculations to plot metrics!", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        catCounts.entries.forEachIndexed { index, entry ->
                            val proportion = entry.value.toFloat() / totalCount
                            val color = barColors[index % barColors.size]
                            Box(
                                modifier = Modifier
                                    .weight(proportion.coerceAtLeast(0.01f))
                                    .fillMaxHeight()
                                    .background(color)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Legends Row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(catCounts.entries.toList()) { entry ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(barColors[catCounts.keys.indexOf(entry.key) % barColors.size])
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${entry.key} (${entry.value})", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // History list Segment Selector
        TabRow(selectedTabIndex = selectedSegment) {
            Tab(selected = selectedSegment == 0, onClick = { selectedSegment = 0 }) {
                Text(
                    "All Calculations",
                    modifier = Modifier.padding(vertical = 12.dp).testTag("tab_all_history"),
                    fontWeight = FontWeight.Bold
                )
            }
            Tab(selected = selectedSegment == 1, onClick = { selectedSegment = 1 }) {
                Text(
                    "Favorites Only",
                    modifier = Modifier.padding(vertical = 12.dp).testTag("tab_favorites"),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val currentListToShow = if (selectedSegment == 0) history else favorites

        if (currentListToShow.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No saved conversions found here yet. Try converting something or saving it as card!",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("history_list"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(currentListToShow, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "[${item.category}] ${item.query}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Row {
                                    IconButton(onClick = { viewModel.toggleFavorite(item) }) {
                                        Icon(
                                            imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Filled.FavoriteBorder,
                                            contentDescription = "Toggle Favorite",
                                            tint = if (item.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(onClick = { viewModel.deleteHistoryItem(item) }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                            Text(
                                item.result,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (item.comparisonText.isNotBlank()) {
                                Text(
                                    item.comparisonText,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (history.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { viewModel.clearAllHistory() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Clear, "Clear")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Clear All Database Log entries", fontWeight = FontWeight.Bold)
            }
        }
    }
}
