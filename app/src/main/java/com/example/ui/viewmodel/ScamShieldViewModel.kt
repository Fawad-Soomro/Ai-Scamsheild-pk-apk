package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Recommendation
import com.example.data.model.RiskIndicator
import com.example.data.model.ScamAnalysisResult
import com.example.data.model.ScanRecord
import com.example.data.model.UrlFeatures
import com.example.data.repository.DashboardMetrics
import com.example.data.repository.ScamShieldRepository
import com.example.engine.DemoDataProvider
import com.example.engine.DemoScenario
import com.example.engine.SoundEffectManager
import com.example.engine.TtsManager
import android.content.Context
import com.example.engine.PdfReportGenerator
import java.io.File
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AssistantMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class ScanUiState(
    val isScanning: Boolean = false,
    val analysisStep: String = "",
    val activeResult: ScamAnalysisResult? = null,
    val activeRecordId: Long? = null,
    val errorMessage: String? = null,
    val inputType: String = "MESSAGE" // "MESSAGE", "SCREENSHOT", "URL"
)

class ScamShieldViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ScamShieldRepository
    private val ttsManager: TtsManager

    // Sound & TTS states
    val soundEffectsEnabled = MutableStateFlow(true)
    private val _isTtsSpeaking = MutableStateFlow(false)
    val isTtsSpeaking: StateFlow<Boolean> = _isTtsSpeaking.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ScamShieldRepository(database.scanDao())
        ttsManager = TtsManager(application)
        ttsManager.setSpeechStateCallback { speaking ->
            _isTtsSpeaking.value = speaking
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        soundEffectsEnabled.value = enabled
        SoundEffectManager.isSoundEnabled = enabled
    }

    fun speakText(text: String) {
        ttsManager.speak(text)
    }

    fun stopSpeech() {
        ttsManager.stop()
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
    }

    // Scan Execution State
    private val _scanState = MutableStateFlow(ScanUiState())
    val scanState: StateFlow<ScanUiState> = _scanState.asStateFlow()

    // Inputs
    val messageInput = MutableStateFlow("")
    val urlInput = MutableStateFlow("")
    val selectedImageBitmap = MutableStateFlow<Bitmap?>(null)

    // Settings
    val selectedModel = MutableStateFlow("gemini-3.5-flash")
    val isDarkMode = MutableStateFlow(true)
    val appLanguage = MutableStateFlow("English")

    // History Filters
    val historySearchQuery = MutableStateFlow("")
    val historyFilterRisk = MutableStateFlow("ALL") // "ALL", "Critical", "High", "Moderate", "Low"
    val historyFilterType = MutableStateFlow("ALL") // "ALL", "MESSAGE", "SCREENSHOT", "URL"

    // Detail View Record
    private val _selectedDetailRecord = MutableStateFlow<ScanRecord?>(null)
    val selectedDetailRecord: StateFlow<ScanRecord?> = _selectedDetailRecord.asStateFlow()

    // AI Scam Assistant State
    private val _assistantChatMessages = MutableStateFlow<List<AssistantMessage>>(emptyList())
    val assistantChatMessages: StateFlow<List<AssistantMessage>> = _assistantChatMessages.asStateFlow()
    val isAssistantLoading = MutableStateFlow(false)

    // Multilingual Explanation State
    private val _translatedExplanations = MutableStateFlow<Map<String, String>>(emptyMap())
    val translatedExplanations: StateFlow<Map<String, String>> = _translatedExplanations.asStateFlow()
    val currentExplanationLanguage = MutableStateFlow("English")
    val isTranslating = MutableStateFlow(false)

    // Database Flows
    val allScans: StateFlow<List<ScanRecord>> = repository.getAllScans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dashboardMetrics: StateFlow<DashboardMetrics> = repository.getDashboardMetrics()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardMetrics())

    val filteredScans: StateFlow<List<ScanRecord>> = combine(
        allScans,
        historySearchQuery,
        historyFilterRisk,
        historyFilterType
    ) { scans, query, risk, type ->
        scans.filter { record ->
            val matchesQuery = query.isBlank() ||
                    record.contentPreview.contains(query, ignoreCase = true) ||
                    record.category.contains(query, ignoreCase = true) ||
                    record.summary.contains(query, ignoreCase = true)

            val matchesRisk = risk == "ALL" || record.riskLevel.equals(risk, ignoreCase = true)
            val matchesType = type == "ALL" || record.inputType.equals(type, ignoreCase = true)

            matchesQuery && matchesRisk && matchesType
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun scanMessage(onComplete: () -> Unit = {}) {
        val text = messageInput.value.trim()
        if (text.isBlank()) {
            _scanState.value = _scanState.value.copy(errorMessage = "Please enter or paste a message to analyze.")
            return
        }

        viewModelScope.launch {
            SoundEffectManager.playScanStartSound()
            _scanState.value = ScanUiState(
                isScanning = true,
                analysisStep = "Preprocessing text and linguistic markers...",
                inputType = "MESSAGE"
            )

            delay(400)
            _scanState.value = _scanState.value.copy(analysisStep = "Connecting to Gemini AI reasoning engine...")

            delay(300)
            _scanState.value = _scanState.value.copy(analysisStep = "Evaluating social engineering signals and risk score...")

            val result = repository.analyzeMessage(text, selectedModel.value)
            if (result.isSuccess) {
                val scamResult = result.getOrThrow()
                val isHighRisk = scamResult.riskLevel.equals("CRITICAL", true) || scamResult.riskLevel.equals("HIGH", true)
                SoundEffectManager.playScanCompleteSound(isHighRisk)
                val recordId = repository.saveScanRecord(scamResult, "MESSAGE", text)
                _scanState.value = ScanUiState(
                    isScanning = false,
                    activeResult = scamResult,
                    activeRecordId = recordId,
                    inputType = "MESSAGE"
                )
                onComplete()
            } else {
                _scanState.value = ScanUiState(
                    isScanning = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Analysis failed. Please try again."
                )
            }
        }
    }

    fun scanUrl(onComplete: () -> Unit = {}) {
        val url = urlInput.value.trim()
        if (url.isBlank()) {
            _scanState.value = _scanState.value.copy(errorMessage = "Please enter a valid website URL.")
            return
        }

        viewModelScope.launch {
            SoundEffectManager.playScanStartSound()
            _scanState.value = ScanUiState(
                isScanning = true,
                analysisStep = "Parsing URL structure & domain hierarchy...",
                inputType = "URL"
            )

            delay(400)
            _scanState.value = _scanState.value.copy(analysisStep = "Checking Pakistani brand typosquats & TLD reputation...")

            delay(300)
            _scanState.value = _scanState.value.copy(analysisStep = "Reasoning over static features with Gemini...")

            val result = repository.analyzeUrl(url, selectedModel.value)
            if (result.isSuccess) {
                val scamResult = result.getOrThrow()
                val isHighRisk = scamResult.riskLevel.equals("CRITICAL", true) || scamResult.riskLevel.equals("HIGH", true)
                SoundEffectManager.playScanCompleteSound(isHighRisk)
                val recordId = repository.saveScanRecord(scamResult, "URL", url)
                _scanState.value = ScanUiState(
                    isScanning = false,
                    activeResult = scamResult,
                    activeRecordId = recordId,
                    inputType = "URL"
                )
                onComplete()
            } else {
                _scanState.value = ScanUiState(
                    isScanning = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "URL analysis failed."
                )
            }
        }
    }

    fun scanScreenshot(bitmap: Bitmap?, onComplete: () -> Unit = {}) {
        if (bitmap == null) {
            _scanState.value = _scanState.value.copy(errorMessage = "Please select or capture a screenshot first.")
            return
        }

        viewModelScope.launch {
            SoundEffectManager.playScanStartSound()
            _scanState.value = ScanUiState(
                isScanning = true,
                analysisStep = "Performing Multimodal Optical Character Recognition (OCR)...",
                inputType = "SCREENSHOT"
            )

            delay(500)
            _scanState.value = _scanState.value.copy(analysisStep = "Extracting visual artifacts, brand logos, and sender context...")

            delay(400)
            _scanState.value = _scanState.value.copy(analysisStep = "Running AI scam risk classification...")

            val result = repository.analyzeScreenshot(bitmap, selectedModel.value)
            if (result.isSuccess) {
                val scamResult = result.getOrThrow()
                val isHighRisk = scamResult.riskLevel.equals("CRITICAL", true) || scamResult.riskLevel.equals("HIGH", true)
                SoundEffectManager.playScanCompleteSound(isHighRisk)
                val recordId = repository.saveScanRecord(
                    result = scamResult,
                    inputType = "SCREENSHOT",
                    rawInput = scamResult.extractedText ?: "Screenshot Analysis"
                )
                _scanState.value = ScanUiState(
                    isScanning = false,
                    activeResult = scamResult,
                    activeRecordId = recordId,
                    inputType = "SCREENSHOT"
                )
                onComplete()
            } else {
                _scanState.value = ScanUiState(
                    isScanning = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Screenshot analysis failed."
                )
            }
        }
    }

    fun runDemoScenario(scenario: DemoScenario, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            if (scenario.inputType == "URL") {
                urlInput.value = scenario.sampleContent
                scanUrl(onComplete)
            } else {
                messageInput.value = scenario.sampleContent
                scanMessage(onComplete)
            }
        }
    }

    fun selectRecordForDetail(recordId: Long) {
        viewModelScope.launch {
            val record = repository.getScanByIdSync(recordId)
            _selectedDetailRecord.value = record
            currentExplanationLanguage.value = "English"
            resetAssistantChat(record)
        }
    }

    fun askAssistant(record: ScanRecord, question: String) {
        if (question.isBlank() || isAssistantLoading.value) return

        val userMsg = AssistantMessage(text = question.trim(), isFromUser = true)
        _assistantChatMessages.value = _assistantChatMessages.value + userMsg
        isAssistantLoading.value = true

        viewModelScope.launch {
            val result = repository.askAssistant(record, question, selectedModel.value)
            val answer = if (result.isSuccess) {
                result.getOrThrow()
            } else {
                "Unable to generate response right now. Please check your network or try again."
            }
            val assistantMsg = AssistantMessage(text = answer, isFromUser = false)
            _assistantChatMessages.value = _assistantChatMessages.value + assistantMsg
            isAssistantLoading.value = false
        }
    }

    fun resetAssistantChat(record: ScanRecord? = null) {
        if (record == null) {
            _assistantChatMessages.value = emptyList()
        } else {
            val welcomeMsg = AssistantMessage(
                text = "Hello! I am your AI Scam Assistant. I analyzed this ${record.inputType.lowercase()} with a ${record.riskLevel} risk assessment (${record.riskScore}/100) under '${record.category}'. Ask me anything about why it was flagged, how this scam works, or what steps to take next.",
                isFromUser = false
            )
            _assistantChatMessages.value = listOf(welcomeMsg)
        }
    }

    fun requestTranslation(record: ScanRecord, language: String) {
        currentExplanationLanguage.value = language
        val key = "${record.id}_$language"
        if (_translatedExplanations.value.containsKey(key)) {
            return
        }

        isTranslating.value = true
        viewModelScope.launch {
            val result = repository.translateExplanation(record, language, selectedModel.value)
            if (result.isSuccess) {
                val currentMap = _translatedExplanations.value.toMutableMap()
                currentMap[key] = result.getOrThrow()
                _translatedExplanations.value = currentMap
            }
            isTranslating.value = false
        }
    }

    fun exportPdfReport(
        context: Context,
        record: ScanRecord,
        includeRawContent: Boolean,
        onResult: (Result<File>) -> Unit
    ) {
        viewModelScope.launch {
            val indicators = getIndicatorsForRecord(record)
            val recommendations = getRecommendationsForRecord(record)
            val result = PdfReportGenerator.generateReport(
                context = context,
                record = record,
                indicators = indicators,
                recommendations = recommendations,
                includeRawContent = includeRawContent
            )
            onResult(result)
        }
    }

    fun clearActiveResult() {
        _scanState.value = ScanUiState()
    }

    fun clearError() {
        _scanState.value = _scanState.value.copy(errorMessage = null)
    }

    fun clearInputs() {
        messageInput.value = ""
        urlInput.value = ""
        selectedImageBitmap.value = null
    }

    fun deleteScan(id: Long) {
        viewModelScope.launch {
            repository.deleteScan(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.deleteAllScans()
        }
    }

    fun getIndicatorsForRecord(record: ScanRecord): List<RiskIndicator> {
        return repository.deserializeIndicators(record.indicatorsJson)
    }

    fun getRecommendationsForRecord(record: ScanRecord): List<Recommendation> {
        return repository.deserializeRecommendations(record.recommendationsJson)
    }

    fun getUrlFeaturesForRecord(record: ScanRecord): UrlFeatures? {
        return repository.deserializeUrlFeatures(record.urlFeaturesJson)
    }
}
