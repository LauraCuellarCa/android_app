package com.example.test

import android.os.Bundle
import android.util.Log
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
    // Variables de estado
    private var currentKeyValues = Constants.VALIDACION_MUESTRA_KEYS
    private var currentFormTitle = "Validacion de Muestra"
    private val _fields = mutableStateListOf<String>().apply {
        addAll(List(currentKeyValues.size) { "" })
    }
    val fields: List<String> = _fields // Exposición inmutable

    private val recognizedText = mutableStateOf("")
    private val partialRecognizedText = mutableStateOf("")
    private val isListening = mutableStateOf(false)
    private val showStopMessage = mutableStateOf(false)

    private val handler = android.os.Handler(android.os.Looper.getMainLooper())

    // Estados de pantalla
    private enum class Screen {
        WELCOME, SELECTION, MAIN, CONFIRMATION
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
                    val currentScreen = remember { mutableStateOf(Screen.WELCOME) }

                    // Welcome screen
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
                                loadForm(Constants.VALIDACION_MUESTRA_KEYS, "Validacion de Muestra")
                                currentScreen.value = Screen.MAIN
                            },
                            onDisenoPatronesClick = {
                                loadForm(Constants.DISENO_PATRONES_KEYS, "Diseño de Patrones")
                                currentScreen.value = Screen.MAIN
                            },
                            onDatosProveedoresClick = {
                                loadForm(Constants.DATOS_PROVEEDORES_KEYS, "Datos Proveedores")
                                currentScreen.value = Screen.MAIN
                            },
                            onInventarioAlmacenClick = {
                                loadForm(Constants.INVENTARIO_ALMACEN_KEYS, "Inventario de Almacen")
                                currentScreen.value = Screen.MAIN
                            },
                            onAsistenteTiendaClick = {
                                loadForm(Constants.ASISTENTE_TIENDA_KEYS, "Asistente de Tienda")
                                currentScreen.value = Screen.MAIN
                            },
                            onAltaClienteClick = {
                                loadForm(Constants.ALTA_CLIENTE_KEYS, "Alta de Cliente")
                                currentScreen.value = Screen.MAIN
                            }
                        )
                    }

                    // Main content
                    AnimatedVisibility(
                        visible = currentScreen.value == Screen.MAIN,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        MainContent(
                            modifier = Modifier.padding(innerPadding),
                            formTitle = currentFormTitle,
                            keyValues = currentKeyValues,
                            fields = fields,
                            debugText = recognizedText.value,
                            partialDebugText = partialRecognizedText.value,
                            onFieldChange = { index, value ->
                                if (index in _fields.indices) {
                                    _fields[index] = value
                                }
                            },
                            onMicClick = {
                                toggleSpeechRecognition()
                            },
                            isListening = isListening.value,
                            showStopMessage = showStopMessage.value,
                            onClearClick = {
                                clearAllFields()
                            },
                            onBackClick = {
                                currentScreen.value = Screen.SELECTION
                            },
                            onConfirmClick = {
                                // Navigate to confirmation screen
                                currentScreen.value = Screen.CONFIRMATION
                            }
                        )
                    }

                    // Confirmation screen
                    AnimatedVisibility(
                        visible = currentScreen.value == Screen.CONFIRMATION,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        ConfirmationScreen(
                            formTitle = currentFormTitle,
                            onBackToSelectionClick = {
                                currentScreen.value = Screen.SELECTION
                            }
                        )
                    }
                }
            }
        }
    }

    private fun loadForm(newKeyValues: List<String>, title: String) {
        currentKeyValues = newKeyValues
        currentFormTitle = title
        clearAllFields()
        _fields.addAll(List(currentKeyValues.size) { "" })
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
        val stopWords = listOf("stop", "parar", "detener")
        val cleanText = text.trim().lowercase(Locale.getDefault())
        return stopWords.any { cleanText.contains(it) }
    }

    private fun normalizarKeyValue(key: String): String {
        return key
            .lowercase(Locale.getDefault())
            // Primero reemplazar vocales acentuadas
            .replace("á", "a")
            .replace("é", "e")
            .replace("í", "i")
            .replace("ó", "o")
            .replace("ú", "u")
            .replace("ü", "u")
            .replace("ñ", "n")  // Opcional: puedes mantener la ñ si lo prefieres
            // Luego eliminar otros caracteres especiales
            .replace(Regex("[^a-z0-9\\s]"), "")
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

        val normalizedKeyMap = currentKeyValues
            .withIndex()
            .associate { (index, key) ->
                normalizarKeyValue(key) to index
            }

        Log.d("VOICE_INPUT", "Texto procesado: $filteredText")
        Log.d("VOICE_INPUT", "Mapa de claves: $normalizedKeyMap")

        MeasurementProcessor.process(filteredText, normalizedKeyMap) { index, measurement ->
            Log.d("VOICE_INPUT", "Intentando actualizar campo $index con '$measurement'")
            if (index in _fields.indices) {
                _fields[index] = measurement
                Log.d("VOICE_INPUT", "Campo actualizado: ${currentKeyValues[index]} = $measurement")
            } else {
                Log.e("VOICE_INPUT", "Índice $index fuera de rango (0..${_fields.size-1})")
            }
        }
    }

    private fun clearAllFields() {
        _fields.clear()
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