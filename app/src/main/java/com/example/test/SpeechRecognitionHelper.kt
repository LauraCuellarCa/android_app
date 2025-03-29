package com.example.test

import android.Manifest
import android.content.Intent
import android.os.Bundle // Importación añadida
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
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
    private var isProcessingResult = false
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private val stopWords = listOf("stop", "alto", "parar", "detener")

    private val requestPermissionLauncher =
        activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) startContinuousRecognition()
            else onError("Se requieren permisos de micrófono")
        }

    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            activity.runOnUiThread {
                Toast.makeText(activity, "Puedes hablar ahora...", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onResults(results: Bundle?) {
            isProcessingResult = true
            try {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val spokenText = matches?.firstOrNull() ?: ""

                if (containsStopWord(spokenText)) {
                    stopRecognition()
                    activity.runOnUiThread {
                        Toast.makeText(activity, "Micrófono detenido por comando de voz", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    onFinalResult(spokenText)
                }
            } finally {
                isProcessingResult = false
            }

            if (isListening) {
                handler.postDelayed({ startListening() }, 200)
            }
        }

        override fun onError(error: Int) {
            val errorMsg = getErrorText(error)
            activity.runOnUiThread {
                onError(errorMsg)
            }

            if (isListening && isRecoverableError(error)) {
                handler.postDelayed({ startListening() }, 1000)
            } else {
                isListening = false
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            if (!isProcessingResult) {
                val partialMatches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val partialText = partialMatches?.firstOrNull() ?: ""

                if (partialText.isNotEmpty()) {
                    if (containsStopWord(partialText)) {
                        stopRecognition()
                        activity.runOnUiThread {
                            Toast.makeText(activity, "Micrófono detenido por comando de voz", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        onPartialResult(partialText)
                    }
                }
            }
        }

        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    private fun containsStopWord(text: String): Boolean {
        val cleanText = text.trim().lowercase()
        return stopWords.any { stopWord ->
            cleanText.contains(stopWord) ||
                    cleanText.contains("$stopWord.") ||
                    cleanText.contains("$stopWord!")
        }
    }

    fun startContinuousRecognition() {
        if (SpeechRecognizer.isRecognitionAvailable(activity)) {
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
        } else {
            onError("El reconocimiento de voz no está disponible")
        }
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
            putExtra(
                RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,
                3000L
            )
            putExtra(
                RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
                2500L
            )
        }

        try {
            speechRecognizer.startListening(intent)
            activity.runOnUiThread {
                Toast.makeText(activity, "Escuchando...", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            onError("Error al iniciar: ${e.message}")
            if (isListening) {
                handler.postDelayed({ startListening() }, 1000)
            }
        }
    }

    fun stopRecognition() {
        isListening = false
        handler.removeCallbacksAndMessages(null)
        if (::speechRecognizer.isInitialized) {
            try {
                speechRecognizer.stopListening()
            } catch (e: Exception) {
                onError("Error al detener: ${e.message}")
            }
        }
    }

    fun destroy() {
        stopRecognition()
        if (::speechRecognizer.isInitialized) {
            try {
                speechRecognizer.destroy()
            } catch (e: Exception) {
                onError("Error al destruir: ${e.message}")
            }
        }
    }

    private fun isRecoverableError(errorCode: Int): Boolean {
        return errorCode == SpeechRecognizer.ERROR_NO_MATCH ||
                errorCode == SpeechRecognizer.ERROR_SPEECH_TIMEOUT
    }

    private fun getErrorText(errorCode: Int): String {
        return when (errorCode) {
            SpeechRecognizer.ERROR_NO_MATCH -> "No se reconoció voz. Sigue hablando..."
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No se detectó voz. Sigue hablando..."
            else -> "Error de reconocimiento (Código: $errorCode)"
        }
    }
}