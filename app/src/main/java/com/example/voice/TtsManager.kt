package com.example.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TtsManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            setupVoice("hi")
            setupListener()
        } else {
            Log.e("TtsManager", "TextToSpeech initialization failed with status $status")
        }
    }

    fun setupVoice(languageCode: String, pitch: Float = 1.05f, speed: Float = 1.0f) {
        if (!isInitialized || tts == null) return

        try {
            val locale = if (languageCode == "en") Locale.ENGLISH else Locale("hi", "IN")
            val result = tts?.setLanguage(locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to default or English
                tts?.setLanguage(Locale.ENGLISH)
            }
            tts?.setPitch(pitch)
            tts?.setSpeechRate(speed)
        } catch (e: Exception) {
            Log.e("TtsManager", "Error setting up voice", e)
        }
    }

    private fun setupListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
            }

            override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false
            }

            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
            }
        })
    }

    fun speak(text: String, pitch: Float = 1.05f, speed: Float = 1.0f, languageCode: String = "hi") {
        if (!isInitialized || tts == null) return

        // Clean out any ACTION tags from text before speaking
        val cleanText = text.replace(Regex("\\[\\[ACTION:.*?\\]\\]"), "").trim()
        if (cleanText.isBlank()) return

        setupVoice(languageCode, pitch, speed)
        val utteranceId = "heena_speech_${System.currentTimeMillis()}"
        tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        try {
            tts?.stop()
            _isSpeaking.value = false
        } catch (e: Exception) {
            Log.e("TtsManager", "Error stopping TTS", e)
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
        } catch (e: Exception) {
            Log.e("TtsManager", "Error shutting down TTS", e)
        }
    }
}
