package com.example.test

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker

class ContinuousSpeechRecognitionHelper(
    private val activity: ComponentActivity,
    private val onPartialResult: (String) -> Unit,
    private val onFinalResult: (String) -> Unit,
    private val onError: (String) -> Unit
) {
    private lateinit var speechRecognizer: SpeechRecognizer
    private var isListening = false
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private val stopWords = listOf("stop", "parar", "detener")

    private val requestPermissionLauncher =
        activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) startContinuousRecognition()
            else onError("Microphone permission required")
        }

    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) = Unit
        override fun onBeginningOfSpeech() = Unit
        override fun onRmsChanged(rmsdB: Float) = Unit
        override fun onBufferReceived(buffer: ByteArray?) = Unit
        override fun onEndOfSpeech() = Unit
        override fun onEvent(eventType: Int, params: Bundle?) = Unit

        override fun onResults(results: Bundle?) {
            processResults(results, isPartial = false)
            if (isListening) handler.postDelayed({ startListening() }, 200)
        }

        override fun onPartialResults(partialResults: Bundle?) {
            processResults(partialResults, isPartial = true)
        }

        override fun onError(error: Int) {
            val errorMsg = when (error) {
                SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
                else -> "Recognition error (Code: $error)"
            }
            onError(errorMsg)

            if (isListening && isRecoverableError(error)) {
                handler.postDelayed({ startListening() }, 1000)
            } else {
                isListening = false
            }
        }
    }

    private fun processResults(results: Bundle?, isPartial: Boolean) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val spokenText = matches?.firstOrNull()?.trim() ?: return

        if (containsStopWord(spokenText)) {
            stopRecognition()
            return
        }

        if (isPartial) onPartialResult(spokenText)
        else onFinalResult(spokenText)
    }

    private fun containsStopWord(text: String): Boolean {
        return stopWords.any { word ->
            text.lowercase().contains(word)
        }
    }

    fun startContinuousRecognition() {
        if (!SpeechRecognizer.isRecognitionAvailable(activity)) {
            onError("Speech recognition not available")
            return
        }

        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.RECORD_AUDIO
            ) != PermissionChecker.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }

        isListening = true
        initializeRecognizer()
        startListening()
    }

    private fun initializeRecognizer() {
        if (::speechRecognizer.isInitialized) {
            speechRecognizer.destroy()
        }
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(activity).apply {
            setRecognitionListener(recognitionListener)
        }
    }

    private fun startListening() {
        if (!isListening || !::speechRecognizer.isInitialized) return

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 2500L)
        }

        try {
            speechRecognizer.startListening(intent)
        } catch (e: Exception) {
            onError("Start error: ${e.message}")
            if (isListening) handler.postDelayed({ startListening() }, 1000)
        }
    }

    fun stopRecognition() {
        isListening = false
        handler.removeCallbacksAndMessages(null)
        if (::speechRecognizer.isInitialized) {
            try {
                speechRecognizer.stopListening()
            } catch (e: Exception) {
                onError("Stop error: ${e.message}")
            }
        }
    }

    fun destroy() {
        stopRecognition()
        if (::speechRecognizer.isInitialized) {
            speechRecognizer.destroy()
        }
    }

    private fun isRecoverableError(errorCode: Int): Boolean {
        return errorCode == SpeechRecognizer.ERROR_NO_MATCH ||
                errorCode == SpeechRecognizer.ERROR_SPEECH_TIMEOUT
    }
}