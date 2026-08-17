package com.example.quizlet.utils

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

object SoundManager {
    private var toneGenerator: ToneGenerator? = null

    private fun getToneGenerator(): ToneGenerator? {
        if (toneGenerator == null) {
            try {
                // Keep the volume reasonable
                toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return toneGenerator
    }

    fun playClick() {
        try {
            getToneGenerator()?.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playCorrect() {
        try {
            getToneGenerator()?.startTone(ToneGenerator.TONE_PROP_ACK, 250)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playIncorrect() {
        try {
            getToneGenerator()?.startTone(ToneGenerator.TONE_PROP_NACK, 300)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

class TextToSpeechHelper(context: Context) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                tts?.language = Locale.US
            }
        }
    }

    fun speak(text: String, isForeign: Boolean = true) {
        if (!isInitialized || text.isBlank()) return
        try {
            // Default to US for foreign, Vietnamese for native
            tts?.language = if (isForeign) Locale.US else Locale("vi", "VN")
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun shutdown() {
        try {
            tts?.shutdown()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@Composable
fun rememberTextToSpeech(): TextToSpeechHelper {
    val context = LocalContext.current
    val ttsHelper = remember { TextToSpeechHelper(context) }
    DisposableEffect(ttsHelper) {
        onDispose {
            ttsHelper.shutdown()
        }
    }
    return ttsHelper
}
