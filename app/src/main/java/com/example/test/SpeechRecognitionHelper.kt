package com.example.test

import android.Manifest
import android.content.Intent
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import android.os.Bundle
import androidx.activity.ComponentActivity

class SpeechRecognitionHelper(
    private val activity: ComponentActivity,
    private val onResult: (String) -> Unit,
    private val onError: (String) -> Unit
) {
    private lateinit var speechRecognizer: SpeechRecognizer

    // Permission launcher
    private val requestPermissionLauncher =
        activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) startRecognition()
            else onError("Microphone permission required")
        }

    // Recognition listener
    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            Toast.makeText(activity, "Dime lo que necesitas...", Toast.LENGTH_SHORT).show()
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val spokenText = matches?.get(0) ?: ""
            onResult(spokenText)
        }

        override fun onError(error: Int) {
            onError(getErrorText(error))
        }

        override fun onBeginningOfSpeech() {
            // Retroalimentación cuando se empieza a hablar
            Toast.makeText(activity, "Escuchando...", Toast.LENGTH_SHORT).show()
        }

        override fun onRmsChanged(rmsdB: Float) {
            // Podrías usar este valor para medir la calidad del audio, pero no es necesario en este caso
        }

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            // Mensaje de finalización
            Toast.makeText(activity, "Detenido. Procesando...", Toast.LENGTH_SHORT).show()
        }

        override fun onPartialResults(partialResults: Bundle?) {}

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    // Start speech recognition
    fun startRecognition() {
        if (SpeechRecognizer.isRecognitionAvailable(activity)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(activity).apply {
                setRecognitionListener(recognitionListener)
            }
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH) // Más preciso para comandos
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "es-ES")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true) // Para resultados en tiempo real

                // Configuración específica para números:
                putExtra("android.speech.extra.GET_AUDIO", false) // Reduce sobrecarga
                putExtra("android.speech.extra.GET_AUDIO_FORMAT", "audio/AMR") // Formato ligero

                // Timeouts ajustados:
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 2000L) // Mínimo 2 segundos
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2000L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)

                // Configuración de resultados:
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3) // Permite alternativas
            }
            // Mostrar mensaje visual de que está escuchando
            Toast.makeText(activity, "Escuchando...", Toast.LENGTH_SHORT).show()
            speechRecognizer.startListening(intent)
        } else {
            onError("Reconocimiento de voz no disponible")
        }
    }

    // Check permissions and start recognition
    fun checkPermissionAndStartRecognition() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) == PermissionChecker.PERMISSION_GRANTED) {
            startRecognition()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Destroy speech recognizer
    fun destroy() {
        if (::speechRecognizer.isInitialized) speechRecognizer.destroy()
    }

    // Helper function to get error text
    private fun getErrorText(errorCode: Int): String {
        return when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> "Error en la grabación de audio. Intenta más tarde."
            SpeechRecognizer.ERROR_CLIENT -> "Error en el cliente. Verifica tu configuración."
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permisos insuficientes para usar el micrófono."
            SpeechRecognizer.ERROR_NETWORK -> "Problema de conexión. Asegúrate de tener una buena red."
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "La conexión de red se agotó. Intenta de nuevo."
            SpeechRecognizer.ERROR_NO_MATCH -> "No se pudo reconocer lo que dijiste. Intenta de nuevo."
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "El servicio de reconocimiento está ocupado. Intenta más tarde."
            SpeechRecognizer.ERROR_SERVER -> "Error del servidor. Intenta más tarde."
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No detectamos audio, por favor habla más claro."
            else -> "No te entendimos. Intenta de nuevo."
        }
    }
}
