package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.HeenaApplication
import com.example.automation.AppLauncherHelper
import com.example.automation.CommandResult
import com.example.automation.CommandRouter
import com.example.data.api.GeminiClient
import com.example.data.local.ChatMessageEntity
import com.example.data.local.RoutineEntity
import com.example.data.local.UserMemoryEntity
import com.example.service.HeenaAccessibilityService
import com.example.service.HeenaWakeWordService
import com.example.ui.components.AvatarState
import com.example.voice.SpeechManager
import com.example.voice.TtsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class Screen {
    SPLASH,
    ONBOARDING,
    HOME,
    CHAT,
    AUTOMATION,
    PERMISSIONS,
    AI_CONFIG,
    THEME_STUDIO,
    SETTINGS
}

data class ConfirmationDialogState(
    val title: String,
    val message: String,
    val onConfirm: () -> Unit
)

class HeenaViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as HeenaApplication
    val prefs = app.preferencesManager
    private val db = app.database

    // Voice & Speech Engines
    val speechManager = SpeechManager(application)
    val ttsManager = TtsManager(application)
    private val commandRouter = CommandRouter(application)

    // Gemini Client
    val geminiClient = GeminiClient(
        getApiKey = { prefs.customApiKey.value },
        getModel = { prefs.customModel.value },
        getAssistantName = { prefs.assistantName.value },
        getLanguage = { prefs.language.value }
    )

    // Navigation Backstack
    private val _screenStack = MutableStateFlow(listOf(Screen.SPLASH))
    val currentScreen: StateFlow<Screen> = MutableStateFlow(Screen.SPLASH).apply {
        viewModelScope.launch {
            _screenStack.collect { stack ->
                value = stack.lastOrNull() ?: Screen.HOME
            }
        }
    }

    // Avatar State
    private val _avatarState = MutableStateFlow(AvatarState.IDLE)
    val avatarState: StateFlow<AvatarState> = _avatarState.asStateFlow()

    // Confirmation Modal for sensitive phone actions
    private val _confirmationDialog = MutableStateFlow<ConfirmationDialogState?>(null)
    val confirmationDialog: StateFlow<ConfirmationDialogState?> = _confirmationDialog.asStateFlow()

    // Status Banner / Snackbars
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    // Chat Messages Flow
    val chatMessages = db.chatDao().getMessagesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Routines Flow
    val routines = db.routineDao().getAllRoutinesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Memory Flow
    val memories = db.memoryDao().getAllMemoryFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        speechManager.initialize()

        // Sync speech recognizer result
        speechManager.onSpeechResult = { recognizedText ->
            handleUserInput(recognizedText, isVoice = true)
        }

        // Monitor TTS speaking state to animate Avatar
        viewModelScope.launch {
            ttsManager.isSpeaking.collect { speaking ->
                if (speaking) {
                    _avatarState.value = AvatarState.SPEAKING
                } else if (_avatarState.value == AvatarState.SPEAKING) {
                    _avatarState.value = AvatarState.IDLE
                }
            }
        }

        // Monitor Speech recognition state to animate Avatar
        viewModelScope.launch {
            speechManager.isListening.collect { listening ->
                if (listening) {
                    _avatarState.value = AvatarState.LISTENING
                } else if (_avatarState.value == AvatarState.LISTENING) {
                    _avatarState.value = AvatarState.IDLE
                }
            }
        }

        // Check if wake word triggered
        viewModelScope.launch {
            HeenaWakeWordService.wakeWordDetectedTrigger.collect { triggerTime ->
                if (triggerTime > 0) {
                    startVoiceListening()
                }
            }
        }
    }

    // Navigation functions
    fun navigateTo(screen: Screen) {
        val currentList = _screenStack.value.toMutableList()
        currentList.add(screen)
        _screenStack.value = currentList
    }

    fun navigateBack(): Boolean {
        val currentList = _screenStack.value.toMutableList()
        if (currentList.size > 1) {
            currentList.removeAt(currentList.lastIndex)
            _screenStack.value = currentList
            return true
        }
        return false
    }

    fun completeSplash() {
        val next = if (prefs.hasCompletedOnboarding.value) Screen.HOME else Screen.ONBOARDING
        _screenStack.value = listOf(next)
    }

    fun completeOnboarding() {
        prefs.setOnboardingCompleted(true)
        _screenStack.value = listOf(Screen.HOME)
    }

    // Voice Interaction
    fun toggleVoiceListening() {
        if (speechManager.isListening.value) {
            speechManager.stopListening()
            _avatarState.value = AvatarState.IDLE
        } else {
            ttsManager.stop()
            startVoiceListening()
        }
    }

    fun startVoiceListening() {
        val langCode = if (prefs.language.value == "en") "en-IN" else "hi-IN"
        speechManager.startListening(langCode)
    }

    fun stopSpeaking() {
        ttsManager.stop()
        _avatarState.value = AvatarState.IDLE
    }

    // Unified Input Processor (Voice or Text)
    fun handleUserInput(text: String, isVoice: Boolean = false) {
        val query = text.trim()
        if (query.isBlank()) return

        viewModelScope.launch {
            // Save user message to database
            db.chatDao().insertMessage(
                ChatMessageEntity(
                    sender = "user",
                    text = query,
                    isVoice = isVoice
                )
            )

            // Step 1: Route through local action interpreter
            val routeResult = commandRouter.processCommand(query, prefs.assistantName.value)

            when (routeResult) {
                is CommandResult.Handled -> {
                    _avatarState.value = AvatarState.HAPPY
                    db.chatDao().insertMessage(
                        ChatMessageEntity(
                            sender = "assistant",
                            text = routeResult.spokenResponse,
                            actionType = routeResult.actionExecuted
                        )
                    )
                    if (prefs.autoSpeakResponse.value) {
                        ttsManager.speak(
                            routeResult.spokenResponse,
                            pitch = prefs.speechPitch.value,
                            speed = prefs.speechRate.value,
                            languageCode = prefs.language.value
                        )
                    }
                }

                is CommandResult.NeedsConfirmation -> {
                    _avatarState.value = AvatarState.THINKING
                    _confirmationDialog.value = ConfirmationDialogState(
                        title = routeResult.title,
                        message = routeResult.message,
                        onConfirm = {
                            routeResult.onConfirm()
                            _confirmationDialog.value = null
                            _avatarState.value = AvatarState.HAPPY
                        }
                    )
                }

                is CommandResult.FallbackToAi -> {
                    // Step 2: Route through Gemini AI
                    callGemini(query)
                }
            }
        }
    }

    private suspend fun callGemini(prompt: String) {
        _avatarState.value = AvatarState.THINKING
        // Fetch recent history
        val recentMessages = db.chatDao().getMessages().takeLast(6).map { it.sender to it.text }

        val result = geminiClient.generateResponse(prompt, recentMessages)
        result.fold(
            onSuccess = { rawReply ->
                // Check if AI included action tag
                val actionMatch = Regex("\\[\\[ACTION:\\s*([A-Z_]+)\\s*\\|?\\s*(.*?)\\]\\]").find(rawReply)
                val cleanReply = if (actionMatch != null) {
                    val type = actionMatch.groupValues[1].trim()
                    val param = actionMatch.groupValues[2].trim()
                    executeModelAction(type, param)
                    rawReply.replace(actionMatch.value, "").trim()
                } else rawReply

                db.chatDao().insertMessage(
                    ChatMessageEntity(
                        sender = "assistant",
                        text = cleanReply,
                        actionType = actionMatch?.groupValues?.get(1)
                    )
                )

                if (prefs.autoSpeakResponse.value) {
                    ttsManager.speak(
                        cleanReply,
                        pitch = prefs.speechPitch.value,
                        speed = prefs.speechRate.value,
                        languageCode = prefs.language.value
                    )
                } else {
                    _avatarState.value = AvatarState.IDLE
                }
            },
            onFailure = { error ->
                Log.e("HeenaViewModel", "Gemini error", error)
                _avatarState.value = AvatarState.ERROR
                val errorMsg = if (error.message?.contains("API_KEY_MISSING") == true) {
                    "नमस्ते! AI चैट के लिए कृपया AI Configuration Center में अपनी Gemini API Key सेट करें।"
                } else {
                    "माफ़ कीजिये, AI उत्तर प्राप्त नहीं हो सका: ${error.localizedMessage ?: "Network error"}"
                }

                db.chatDao().insertMessage(
                    ChatMessageEntity(
                        sender = "assistant",
                        text = errorMsg,
                        status = "error"
                    )
                )

                if (prefs.autoSpeakResponse.value) {
                    ttsManager.speak(errorMsg, languageCode = prefs.language.value)
                }
            }
        )
    }

    private fun executeModelAction(type: String, param: String) {
        val context = getApplication<Application>()
        when (type) {
            "OPEN_APP" -> AppLauncherHelper.launchAppByPackage(context, param)
            "SEARCH_YOUTUBE" -> AppLauncherHelper.openYouTube(context, param)
            "SEARCH_WEB" -> AppLauncherHelper.openChrome(context, param)
            "OPEN_MAPS" -> AppLauncherHelper.openMaps(context, param)
            "MAKE_CALL" -> AppLauncherHelper.openDialer(context, param)
            "SEND_MESSAGE" -> AppLauncherHelper.openWhatsApp(context, param)
            "SET_TIMER" -> {
                val sec = param.toIntOrNull() ?: 300
                AppLauncherHelper.setTimer(context, sec)
            }
        }
    }

    fun dismissConfirmationDialog() {
        _confirmationDialog.value = null
    }

    fun clearChat() {
        viewModelScope.launch {
            db.chatDao().clearAllMessages()
            showStatus("Chat history cleared")
        }
    }

    // Smart Routines execution
    fun executeRoutine(routine: RoutineEntity) {
        viewModelScope.launch {
            showStatus("Running: ${routine.title}")
            _avatarState.value = AvatarState.HAPPY
            // Execute routine actions
            try {
                val array = org.json.JSONArray(routine.actionsJson)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val action = obj.optString("action")
                    val param = obj.optString("param")
                    when (action) {
                        "GREETING" -> {
                            ttsManager.speak(param, languageCode = prefs.language.value)
                        }
                        "DATE_TIME" -> {
                            val time = java.text.SimpleDateFormat("hh:mm a, EEEE d MMMM", java.util.Locale.getDefault()).format(java.util.Date())
                            ttsManager.speak("आज का समय $time है", languageCode = prefs.language.value)
                        }
                        "OPEN_APP" -> {
                            AppLauncherHelper.launchAppByPackage(getApplication(), param)
                        }
                        "SEARCH_YOUTUBE" -> {
                            AppLauncherHelper.openYouTube(getApplication(), param)
                        }
                        "SET_TIMER" -> {
                            val sec = param.toIntOrNull() ?: 300
                            AppLauncherHelper.setTimer(getApplication(), sec)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("HeenaViewModel", "Routine failed", e)
            }
        }
    }

    fun toggleRoutine(routine: RoutineEntity) {
        viewModelScope.launch {
            db.routineDao().updateRoutine(routine.copy(isEnabled = !routine.isEnabled))
        }
    }

    fun createCustomRoutine(title: String, trigger: String, description: String, actionType: String, param: String) {
        viewModelScope.launch {
            val json = org.json.JSONArray().apply {
                put(org.json.JSONObject().apply {
                    put("action", actionType)
                    put("param", param)
                })
            }.toString()

            db.routineDao().insertRoutine(
                RoutineEntity(
                    title = title,
                    voiceTrigger = trigger,
                    description = description,
                    isEnabled = true,
                    actionsJson = json
                )
            )
            showStatus("Routine created: $title")
        }
    }

    fun deleteRoutine(routineId: Long) {
        viewModelScope.launch {
            db.routineDao().deleteRoutine(routineId)
            showStatus("Routine removed")
        }
    }

    // Memory functions
    fun addMemory(key: String, value: String, category: String = "preference") {
        viewModelScope.launch {
            db.memoryDao().insertMemory(
                UserMemoryEntity(
                    key = key,
                    value = value,
                    category = category
                )
            )
            showStatus("Memory saved: $key")
        }
    }

    fun deleteMemory(id: Long) {
        viewModelScope.launch {
            db.memoryDao().deleteMemory(id)
            showStatus("Memory deleted")
        }
    }

    fun clearAllMemory() {
        viewModelScope.launch {
            db.memoryDao().clearAllMemory()
            showStatus("All memories deleted")
        }
    }

    // Wake word background service toggle
    fun toggleWakeWordService(enable: Boolean) {
        prefs.setWakeWordEnabled(enable)
        val context = getApplication<Application>()
        val serviceIntent = Intent(context, HeenaWakeWordService::class.java)
        if (enable) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
                showStatus("Background wake word listening enabled")
            } catch (e: Exception) {
                Log.e("HeenaViewModel", "Could not start wake service", e)
                showStatus("Please grant microphone permission to enable background wake word")
            }
        } else {
            context.stopService(serviceIntent)
            showStatus("Background wake word listening disabled")
        }
    }

    fun showStatus(msg: String) {
        _statusMessage.value = msg
    }

    fun clearStatus() {
        _statusMessage.value = null
    }

    override fun onCleared() {
        speechManager.destroy()
        ttsManager.shutdown()
        super.onCleared()
    }
}
