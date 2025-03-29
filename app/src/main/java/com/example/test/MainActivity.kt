package com.example.test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.test.ui.theme.TestTheme
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import java.text.Normalizer
import java.util.Locale
import com.example.tuapp.util.SpanishNumberNormalizer

class MainActivity : ComponentActivity() {
    private val keyValues = listOf(
        "ANCHO DE PECHO",
        "ANCHO DELANTERO (A 1/2 SISA)",
        "ANCHO DE CINTURA",
        "ANCHO DEL BAJO",
        "ANCHO INFERIOR",
        "ALTO DEL RIB DEL BAJO",
        "LARGO DEL CUERPO",
        "LARGO DE LA ESPALDA",
        "LARGO DE HOMBRO"
    )

    private val fields = mutableStateListOf<String>().apply {
        addAll(List(keyValues.size) { "" })
    }

    // Estados para el reconocimiento de voz
    private val recognizedText = mutableStateOf("")
    private val partialRecognizedText = mutableStateOf("")
    private val isListening = mutableStateOf(false)
    private val showStopMessage = mutableStateOf(false)

    private val handler = android.os.Handler(android.os.Looper.getMainLooper())


    // Screen state
    private enum class Screen {
        WELCOME, SELECTION, MAIN
    }

    // Speech recognition helper
    private lateinit var speechRecognitionHelper: ContinuousSpeechRecognitionHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize speech recognition helper
        speechRecognitionHelper = ContinuousSpeechRecognitionHelper(
            activity = this,
            onPartialResult = { partialText ->
                runOnUiThread {
                    partialRecognizedText.value = partialText
                    processPartialText(partialText)

                    // Verificar si el texto parcial contiene "stop"
                    if (containsStopWord(partialText)) {
                        showStopMessage.value = true
                        handler.postDelayed({ showStopMessage.value = false }, 2000)
                    }
                }
            },
            onFinalResult = { finalText ->
                runOnUiThread {
                    recognizedText.value = finalText
                    processSpokenText(finalText)

                    // Verificar si el texto final contiene "stop"
                    if (containsStopWord(finalText)) {
                        showStopMessage.value = true
                        handler.postDelayed({ showStopMessage.value = false }, 2000)
                    }
                }
            },
            onError = { error ->
                runOnUiThread {
                    Toast.makeText(this, "Error: $error", Toast.LENGTH_SHORT).show()
                    isListening.value = false
                }
            }
        )

        setContent {
            TestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Track current screen
                    val currentScreen = remember { mutableStateOf(Screen.WELCOME) }

                    // Welcome screen with animation
                    AnimatedVisibility(
                        visible = currentScreen.value == Screen.WELCOME,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        WelcomeScreen(
                            onContinueClick = {
                                currentScreen.value = Screen.SELECTION
                            }
                        )
                    }

                    // Selection screen with animation
                    AnimatedVisibility(
                        visible = currentScreen.value == Screen.SELECTION,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        SelectionScreen(
                            onBackClick = {
                                currentScreen.value = Screen.WELCOME
                            },
                            onValidacionMuestraClick = {
                                currentScreen.value = Screen.MAIN
                            }
                        )
                    }

                    // Main content with animation
                    AnimatedVisibility(
                        visible = currentScreen.value == Screen.MAIN,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        MainContent(
                            modifier = Modifier.padding(innerPadding),
                            keyValues = keyValues,
                            fields = fields,
                            debugText = recognizedText.value,
                            partialDebugText = partialRecognizedText.value,
                            onFieldChange = { index, value ->
                                fields[index] = value
                            },
                            onMicClick = {
                                toggleSpeechRecognition()
                            },
                            isListening = isListening.value,
                            showStopMessage = showStopMessage.value,
                            onClearClick = { clearAllFields() },
                            onBackClick = {
                                currentScreen.value = Screen.SELECTION
                            }
                        )
                    }
                }
            }
        }
    }

    private fun toggleSpeechRecognition() {
        if (isListening.value) {
            speechRecognitionHelper.stopRecognition()
            isListening.value = false
        } else {
            speechRecognitionHelper.startContinuousRecognition()
            isListening.value = true
            showStopMessage.value = false
        }
    }

    private fun containsStopWord(text: String): Boolean {
        val stopWords = listOf("stop", "alto", "parar", "detener")
        val cleanText = text.trim().lowercase(Locale.getDefault())
        return stopWords.any { cleanText.contains(it) }
    }

    private fun normalizarKeyValue(key: String): String {
        return key
            .lowercase(Locale.getDefault())
            .replace(Regex("[^a-z0-9áéíóúüñ\\s]"), "")
            .split(" ")
            .filter { it.length >= 4 }
            .joinToString(" ")
            .trim()
    }

    private fun normalizeText(text: String): String {
        return Normalizer.normalize(text, Normalizer.Form.NFD)
            .lowercase(Locale.getDefault())
            .replace("[^a-z0-9\\s]".toRegex(), "")
            .replace("\\s+".toRegex(), " ")
            .trim()
    }

    private fun removeShortWords(text: String, minLength: Int = 4): String {
        return text.split(" ")
            .filter { word ->
                word.length >= minLength ||
                        word.equals("con", ignoreCase = true) ||
                        word.any { it.isDigit() }
            }
            .joinToString(" ")
    }

    private fun processPartialText(partialText: String) {
        val cleanText = normalizeText(partialText)
        val filteredText = removeShortWords(cleanText)
        partialRecognizedText.value = filteredText
    }

    private fun processSpokenText(spokenText: String) {
        val cleanText = normalizeText(spokenText)
        val fullyNormalizedText = SpanishNumberNormalizer.normalize(cleanText)
        val filteredText = removeShortWords(fullyNormalizedText)

        recognizedText.value = filteredText

        val normalizedKeyMap = keyValues
            .withIndex()
            .associate { (index, key) ->
                normalizarKeyValue(key) to index
            }

        MeasurementProcessor.process(filteredText, normalizedKeyMap) { index, measurement ->
            updateField(index, measurement)
        }
    }

    private fun updateField(fieldIndex: Int, measurement: String) {
        fields[fieldIndex] = measurement
    }

    private fun clearAllFields() {
        for (i in fields.indices) {
            fields[i] = ""
        }
        recognizedText.value = ""
        partialRecognizedText.value = ""
        isListening.value = false
        speechRecognitionHelper.stopRecognition()
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognitionHelper.destroy()
    }
}