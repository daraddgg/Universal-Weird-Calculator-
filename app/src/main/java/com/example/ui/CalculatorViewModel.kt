package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiApiHelper
import com.example.calculator.*
import com.example.data.ConversionHistory
import com.example.data.DatabaseProvider
import com.example.data.HistoryRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {
    private val database = DatabaseProvider.getDatabase(application)
    private val repository = HistoryRepository(database.historyDao())

    val allHistory = repository.allHistory.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
    val favorites = repository.favorites.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Unit Converter UI State
    val categoryState = MutableStateFlow("Length")
    val fromUnitState = MutableStateFlow("m")
    val toUnitState = MutableStateFlow("km")
    val inputValueState = MutableStateFlow("1000")

    val calculationResult = MutableStateFlow<ConversionResult?>(null)

    // AI Tab / NLU Engine State
    val nluQueryState = MutableStateFlow("1000 meters")
    val nluResultState = MutableStateFlow<ParsedNluResponse?>(null)
    val nluParsedState = MutableStateFlow<ParsedNluQuery?>(null)

    // AI Tab State
    val aiQueryState = MutableStateFlow("")
    val aiResultState = MutableStateFlow("")
    val aiLoadingState = MutableStateFlow(false)

    // Chemistry Tab State
    val chemistryQueryState = MutableStateFlow("2 H2(g) + O2(g) -> 2 H2O(l)")
    val chemistryNlpResultState = MutableStateFlow<ParsedChemNlpResponse?>(null)

    init {
        performConversion()
        processNluQuery("1000 meters")
        processChemistryNlpQuery("2 H2(g) + O2(g) -> 2 H2O(l)")
    }

    fun setCategory(category: String) {
        categoryState.value = category
        val list = WeirdCalculatorEngine.unitsMap[category] ?: emptyList()
        if (list.isNotEmpty()) {
            fromUnitState.value = list[0].key
            toUnitState.value = if (list.size > 1) list[1].key else list[0].key
        }
        performConversion()
    }

    fun setFromUnit(unit: String) {
        fromUnitState.value = unit
        performConversion()
    }

    fun setToUnit(unit: String) {
        toUnitState.value = unit
        performConversion()
    }

    fun setInputValue(value: String) {
        inputValueState.value = value
        performConversion()
    }

    fun performConversion() {
        val valDouble = inputValueState.value.toDoubleOrNull() ?: 0.0
        val cat = categoryState.value
        val res = WeirdCalculatorEngine.convertValue(
            category = cat,
            inputValue = valDouble,
            fromUnitKey = fromUnitState.value,
            toUnitKey = toUnitState.value
        )
        calculationResult.value = res
    }

    fun saveConversionToHistory() {
        val res = calculationResult.value ?: return
        val valDouble = inputValueState.value.toDoubleOrNull() ?: 0.0
        val historyItem = ConversionHistory(
            query = "Custom: ${inputValueState.value} ${fromUnitState.value} -> ${toUnitState.value}",
            value = valDouble,
            category = categoryState.value,
            fromUnit = fromUnitState.value,
            toUnit = toUnitState.value,
            result = res.resultString,
            comparisonText = res.comparisons.joinToString("\n"),
            fact = res.fact
        )
        viewModelScope.launch {
            repository.insert(historyItem)
        }
    }

    fun toggleFavorite(item: ConversionHistory) {
        viewModelScope.launch {
            repository.update(item.copy(isFavorite = !item.isFavorite))
        }
    }

    fun deleteHistoryItem(item: ConversionHistory) {
        viewModelScope.launch {
            repository.delete(item)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    fun askGeminiQuery(query: String) {
        aiQueryState.value = query
        aiLoadingState.value = true
        aiResultState.value = ""
        viewModelScope.launch {
            val response = GeminiApiHelper.askGemini(query)
            aiResultState.value = response
            aiLoadingState.value = false

            // Save this neat AI response to Room history too!
            val historyItem = ConversionHistory(
                query = query,
                value = 0.0,
                category = "AI NLU Mode",
                fromUnit = "Text",
                toUnit = "Insight",
                result = "Calculated via Gemini",
                comparisonText = response,
                fact = "Dynamic comparative calculation completed."
            )
            repository.insert(historyItem)
        }
    }

    fun processChemistryNlpQuery(query: String) {
        chemistryQueryState.value = query
        val response = ChemistryEngine.parseChemistryNlp(query)
        chemistryNlpResultState.value = response

        if (response.success) {
            val historyItem = ConversionHistory(
                query = "Chemistry Engine: $query",
                value = 0.0,
                category = "Chemistry Lab 🧪",
                fromUnit = response.queryType,
                toUnit = "Molar/Thermo Result",
                result = response.interpretation,
                comparisonText = response.outputMarkdown,
                fact = "Solved with 100% deterministic offline Chemistry Engine."
            )
            viewModelScope.launch {
                repository.insert(historyItem)
            }
        }
    }

    fun processNluQuery(query: String) {
        nluQueryState.value = query
        val parsed = WeirdCalculatorNluParser.parseQuery(query)
        nluParsedState.value = parsed
        val response = WeirdCalculatorNluParser.executeNluQuery(parsed)
        nluResultState.value = response

        if (response.success && parsed.errorOrPrompt == null) {
            val historyItem = ConversionHistory(
                query = "NLP Match: $query",
                value = parsed.value,
                category = parsed.category,
                fromUnit = parsed.fromUnitKey,
                toUnit = parsed.targetObject?.name ?: "NLU-Insight",
                result = response.resultText,
                comparisonText = response.comparisons.joinToString("\n"),
                fact = response.fact
            )
            viewModelScope.launch {
                repository.insert(historyItem)
            }
        }
    }
}
