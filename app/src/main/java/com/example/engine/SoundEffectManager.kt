package com.example.engine

import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log

object SoundEffectManager {
    private var toneGenerator: ToneGenerator? = null
    var isSoundEnabled: Boolean = true

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 70)
        } catch (e: Exception) {
            Log.e("SoundEffectManager", "Failed to initialize ToneGenerator", e)
        }
    }

    fun playScanStartSound() {
        if (!isSoundEnabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 120)
        } catch (e: Exception) {
            Log.e("SoundEffectManager", "Error playing scan start sound", e)
        }
    }

    fun playScanCompleteSound(isHighRisk: Boolean) {
        if (!isSoundEnabled) return
        try {
            if (isHighRisk) {
                // High risk alert sound
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 250)
            } else {
                // Positive resolution chime
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 180)
            }
        } catch (e: Exception) {
            Log.e("SoundEffectManager", "Error playing scan complete sound", e)
        }
    }

    fun playButtonClickSound() {
        if (!isSoundEnabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 50)
        } catch (e: Exception) {
            Log.e("SoundEffectManager", "Error playing click sound", e)
        }
    }
}
