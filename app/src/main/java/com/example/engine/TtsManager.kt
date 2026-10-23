package com.example.engine

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class TtsManager(context: Context) {
    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false
    private var onSpeechStateChanged: ((Boolean) -> Unit)? = null

    init {
        textToSpeech = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    textToSpeech?.setLanguage(Locale.ENGLISH)
                }
                textToSpeech?.setSpeechRate(0.95f)
                textToSpeech?.setPitch(1.0f)
                isInitialized = true
                setupProgressListener()
            } else {
                Log.e("TtsManager", "TextToSpeech initialization failed with code: $status")
            }
        }
    }

    private fun setupProgressListener() {
        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                onSpeechStateChanged?.invoke(true)
            }

            override fun onDone(utteranceId: String?) {
                onSpeechStateChanged?.invoke(false)
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                onSpeechStateChanged?.invoke(false)
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                onSpeechStateChanged?.invoke(false)
            }
        })
    }

    fun setSpeechStateCallback(callback: (Boolean) -> Unit) {
        this.onSpeechStateChanged = callback
    }

    fun speak(text: String) {
        if (!isInitialized || text.isBlank()) {
            return
        }
        try {
            stop()
            val utteranceId = "ScamShieldUtterance_${System.currentTimeMillis()}"
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } catch (e: Exception) {
            Log.e("TtsManager", "Error speaking text", e)
            onSpeechStateChanged?.invoke(false)
        }
    }

    fun stop() {
        try {
            textToSpeech?.stop()
            onSpeechStateChanged?.invoke(false)
        } catch (e: Exception) {
            Log.e("TtsManager", "Error stopping TTS", e)
        }
    }

    fun shutdown() {
        try {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
            textToSpeech = null
            isInitialized = false
        } catch (e: Exception) {
            Log.e("TtsManager", "Error shutting down TTS", e)
        }
    }
}
