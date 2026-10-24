package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("heena_ai_preferences", Context.MODE_PRIVATE)

    private val _assistantName = MutableStateFlow(prefs.getString(KEY_ASSISTANT_NAME, "Heena AI") ?: "Heena AI")
    val assistantName: StateFlow<String> = _assistantName.asStateFlow()

    private val _language = MutableStateFlow(prefs.getString(KEY_LANGUAGE, "hi") ?: "hi")
    val language: StateFlow<String> = _language.asStateFlow()

    private val _themeName = MutableStateFlow(prefs.getString(KEY_THEME_NAME, "purple") ?: "purple")
    val themeName: StateFlow<String> = _themeName.asStateFlow()

    private val _wakeWordEnabled = MutableStateFlow(prefs.getBoolean(KEY_WAKE_WORD_ENABLED, true))
    val wakeWordEnabled: StateFlow<Boolean> = _wakeWordEnabled.asStateFlow()

    private val _autoSpeakResponse = MutableStateFlow(prefs.getBoolean(KEY_AUTO_SPEAK, true))
    val autoSpeakResponse: StateFlow<Boolean> = _autoSpeakResponse.asStateFlow()

    private val _speechRate = MutableStateFlow(prefs.getFloat(KEY_SPEECH_RATE, 1.0f))
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    private val _speechPitch = MutableStateFlow(prefs.getFloat(KEY_SPEECH_PITCH, 1.05f))
    val speechPitch: StateFlow<Float> = _speechPitch.asStateFlow()

    private val _customApiKey = MutableStateFlow(prefs.getString(KEY_CUSTOM_API_KEY, "") ?: "")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    private val _customModel = MutableStateFlow(prefs.getString(KEY_CUSTOM_MODEL, "gemini-2.5-flash") ?: "gemini-2.5-flash")
    val customModel: StateFlow<String> = _customModel.asStateFlow()

    private val _customEndpoint = MutableStateFlow(prefs.getString(KEY_CUSTOM_ENDPOINT, "") ?: "")
    val customEndpoint: StateFlow<String> = _customEndpoint.asStateFlow()

    private val _hasCompletedOnboarding = MutableStateFlow(prefs.getBoolean(KEY_ONBOARDING_DONE, false))
    val hasCompletedOnboarding: StateFlow<Boolean> = _hasCompletedOnboarding.asStateFlow()

    private val _reducedMotion = MutableStateFlow(prefs.getBoolean(KEY_REDUCED_MOTION, false))
    val reducedMotion: StateFlow<Boolean> = _reducedMotion.asStateFlow()

    private val _greetingMessage = MutableStateFlow(
        prefs.getString(KEY_GREETING_MESSAGE, "नमस्ते! मैं हीना हूँ। आज मैं आपकी क्या मदद करूँ?")
            ?: "नमस्ते! मैं हीना हूँ। आज मैं आपकी क्या मदद करूँ?"
    )
    val greetingMessage: StateFlow<String> = _greetingMessage.asStateFlow()

    fun setAssistantName(name: String) {
        val cleanName = name.ifBlank { "Heena AI" }
        prefs.edit().putString(KEY_ASSISTANT_NAME, cleanName).apply()
        _assistantName.value = cleanName
    }

    fun setLanguage(lang: String) {
        prefs.edit().putString(KEY_LANGUAGE, lang).apply()
        _language.value = lang
    }

    fun setThemeName(theme: String) {
        prefs.edit().putString(KEY_THEME_NAME, theme).apply()
        _themeName.value = theme
    }

    fun setWakeWordEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_WAKE_WORD_ENABLED, enabled).apply()
        _wakeWordEnabled.value = enabled
    }

    fun setAutoSpeakResponse(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_SPEAK, enabled).apply()
        _autoSpeakResponse.value = enabled
    }

    fun setSpeechRate(rate: Float) {
        prefs.edit().putFloat(KEY_SPEECH_RATE, rate).apply()
        _speechRate.value = rate
    }

    fun setSpeechPitch(pitch: Float) {
        prefs.edit().putFloat(KEY_SPEECH_PITCH, pitch).apply()
        _speechPitch.value = pitch
    }

    fun setCustomApiKey(key: String) {
        prefs.edit().putString(KEY_CUSTOM_API_KEY, key.trim()).apply()
        _customApiKey.value = key.trim()
    }

    fun setCustomModel(model: String) {
        prefs.edit().putString(KEY_CUSTOM_MODEL, model.trim()).apply()
        _customModel.value = model.trim()
    }

    fun setCustomEndpoint(endpoint: String) {
        prefs.edit().putString(KEY_CUSTOM_ENDPOINT, endpoint.trim()).apply()
        _customEndpoint.value = endpoint.trim()
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_DONE, completed).apply()
        _hasCompletedOnboarding.value = completed
    }

    fun setReducedMotion(reduced: Boolean) {
        prefs.edit().putBoolean(KEY_REDUCED_MOTION, reduced).apply()
        _reducedMotion.value = reduced
    }

    fun setGreetingMessage(greeting: String) {
        prefs.edit().putString(KEY_GREETING_MESSAGE, greeting).apply()
        _greetingMessage.value = greeting
    }

    fun resetToDefaults() {
        prefs.edit().clear().apply()
        _assistantName.value = "Heena AI"
        _language.value = "hi"
        _themeName.value = "purple"
        _wakeWordEnabled.value = true
        _autoSpeakResponse.value = true
        _speechRate.value = 1.0f
        _speechPitch.value = 1.05f
        _customApiKey.value = ""
        _customModel.value = "gemini-2.5-flash"
        _customEndpoint.value = ""
        _hasCompletedOnboarding.value = true
        _reducedMotion.value = false
        _greetingMessage.value = "नमस्ते! मैं हीना हूँ। आज मैं आपकी क्या मदद करूँ?"
    }

    companion object {
        private const val KEY_ASSISTANT_NAME = "key_assistant_name"
        private const val KEY_LANGUAGE = "key_language"
        private const val KEY_THEME_NAME = "key_theme_name"
        private const val KEY_WAKE_WORD_ENABLED = "key_wake_word_enabled"
        private const val KEY_AUTO_SPEAK = "key_auto_speak"
        private const val KEY_SPEECH_RATE = "key_speech_rate"
        private const val KEY_SPEECH_PITCH = "key_speech_pitch"
        private const val KEY_CUSTOM_API_KEY = "key_custom_api_key"
        private const val KEY_CUSTOM_MODEL = "key_custom_model"
        private const val KEY_CUSTOM_ENDPOINT = "key_custom_endpoint"
        private const val KEY_ONBOARDING_DONE = "key_onboarding_done"
        private const val KEY_REDUCED_MOTION = "key_reduced_motion"
        private const val KEY_GREETING_MESSAGE = "key_greeting_message"
    }
}
