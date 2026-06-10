package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiApiHelper
import com.example.calculator.ConversionResult
import com.example.calculator.WeirdCalculatorEngine
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

    // AI Tab State
    val aiQueryState = MutableStateFlow("")
    val aiResultState = MutableStateFlow("")
    val aiLoadingState = MutableStateFlow(false)

    // Voice Input State
    val voiceRecordingState = MutableStateFlow(false)
    val voiceResultState = MutableStateFlow("")

    // OCR Scanning State
    val ocrScanningState = MutableStateFlow(false)
    val ocrResultState = MutableStateFlow("")

    // AR Camera measurement State
    val arCameraState = MutableStateFlow(false)
    val arResultState = MutableStateFlow("")

    init {
        performConversion()
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

    fun simulateVoiceInput() {
        if (voiceRecordingState.value) return
        voiceRecordingState.value = true
        voiceResultState.value = "Listening to voice input..."
        viewModelScope.launch {
            delay(1800)
            voiceRecordingState.value = false
            val randomVoiceQuery = listOf(
                "How many cats weigh 500 kilograms?",
                "How many heartbeats happen in two years?",
                "How many movies fit into 1 TB?",
                "How many Eiffel Towers equal 5 kilometers?",
                "How many phone charges are in 1 kilowatt-hour?"
            ).random()
            voiceResultState.value = "Parsed: \"$randomVoiceQuery\""
            askGeminiQuery(randomVoiceQuery)
        }
    }

    fun simulateOcrScan(imageType: String) {
        if (ocrScanningState.value) return
        ocrScanningState.value = true
        ocrResultState.value = "Analyzing image structure..."
        viewModelScope.launch {
            delay(2000)
            ocrScanningState.value = false
            val (tripleData, pairData) = when (imageType) {
                "Receipt" -> Triple("Extracted Weight: 5000 kg from Elephant bill", "5000", "Weight / Mass") to Pair("kg", "t")
                "Blueprint" -> Triple("Extracted Length: 1000 meters from Runway plan", "1000", "Length") to Pair("m", "km")
                "StorageLog" -> Triple("Extracted Volume capacity: 2 TB from Cloud server log", "2", "Digital Storage") to Pair("tb", "gb")
                else -> Triple("Extracted Currency: 15 Euros from Dinner balance", "15", "Money") to Pair("eur", "usd")
            }
            val (resultText, valStr, cat) = tripleData
            val (fromU, toU) = pairData
            ocrResultState.value = "Successfully parsed: $resultText"
            inputValueState.value = valStr
            setCategory(cat)
            setFromUnit(fromU)
            setToUnit(toU)
        }
    }

    fun simulateArCameraMeasure() {
        if (arCameraState.value) return
        arCameraState.value = true
        arResultState.value = "Calibrating camera gyroscope..."
        viewModelScope.launch {
            delay(1000)
            arResultState.value = "Detecting plane levels..."
            delay(1000)
            arResultState.value = "Measured length: 5 meters (Placed laser tape)"
            delay(800)
            arCameraState.value = false
            inputValueState.value = "5"
            setCategory("Length")
            setFromUnit("m")
            setToUnit("km")
        }
    }
}
