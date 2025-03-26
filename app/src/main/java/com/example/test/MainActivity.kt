package com.example.test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.test.ui.theme.TestTheme
import android.widget.Toast // Para Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize // Para Modifier.fillMaxSize()
import androidx.compose.foundation.layout.padding // Para Modifier.padding()
import androidx.compose.material3.Scaffold // Para Scaffold
import androidx.compose.ui.Modifier // Para Modifier
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember //this idk
import java.text.Normalizer
import java.util.Locale
import com.example.tuapp.util.SpanishNumberNormalizer
//import com.example.test.SpanishNumberNormalizer


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
    // Usar mutableStateListOf para que los cambios sean observables por Compose
    private val fields = mutableStateListOf<String>().apply {
        addAll(List(keyValues.size) { "" })
    }
    
    // Estado para mostrar el texto reconocido para debug
    private val recognizedText = mutableStateOf("")

    // Speech recognition helper
    private lateinit var speechRecognitionHelper: SpeechRecognitionHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize speech recognition
        speechRecognitionHelper = SpeechRecognitionHelper(this,
            onResult = { spokenText -> processSpokenText(spokenText) },
            onError = { error -> Toast.makeText(this, "Error: $error", Toast.LENGTH_SHORT).show() }
        )

        setContent {
            TestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Always start with the welcome screen
                    val showWelcomeScreen = remember { mutableStateOf(true) }
                    
                    // Welcome screen with animation
                    AnimatedVisibility(
                        visible = showWelcomeScreen.value,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        WelcomeScreen(
                            onContinueClick = {
                                showWelcomeScreen.value = false
                            }
                        )
                    }
                    
                    // Main content with animation
                    AnimatedVisibility(
                        visible = !showWelcomeScreen.value,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        MainContent(
                            modifier = Modifier.padding(innerPadding),
                            keyValues = keyValues,
                            fields = fields,
                            debugText = recognizedText.value,
                            onFieldChange = { index, value ->
                                fields[index] = value // Actualizamos el campo dinámicamente
                            },
                            onMicClick = { speechRecognitionHelper.checkPermissionAndStartRecognition() },
                            onClearClick = { clearAllFields() }
                        )
                    }
                }
            }
        }
    }

    fun normalizarKeyValue(key: String): String {
        return key
            .lowercase(Locale.getDefault())
            .replace(Regex("[^a-z0-9áéíóúüñ\\s]"), "")  // Conserva letras, números y espacios
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
                word.length >= minLength || word.equals("con", ignoreCase = true)
            }
            .joinToString(" ")
    }


    private fun processSpokenText(spokenText: String) {
        // Paso 1: Normalización básica del texto reconocido
        val cleanText = normalizeText(spokenText)

        // Paso 2: Eliminar palabras cortas (menos de 3 caracteres)
        val filteredText = removeShortWords(cleanText)

        val fullyNormalizedText = SpanishNumberNormalizer.normalize(filteredText)

        // Mostrar el texto procesado en la UI
        runOnUiThread {
            recognizedText.value = """
            Texto procesado: "$fullyNormalizedText"
        """.trimIndent()
        }

        // Crear el mapa de claves normalizadas
        val normalizedKeyMap = keyValues
            .withIndex()
            .associate { (index, key) ->
                normalizarKeyValue(key) to index
            }

        // Procesar el texto con MeasurementProcessor
        MeasurementProcessor.process(fullyNormalizedText, normalizedKeyMap) { index, measurement ->
            updateField(index, measurement)
        }
    }

    private fun updateField(fieldIndex: Int, measurement: String) {
        // Asignar el valor al campo correspondiente según el índice
        runOnUiThread {
            fields[fieldIndex] = measurement
        }
    }

    private fun clearAllFields() {
        runOnUiThread {
            for (i in fields.indices) {
                fields[i] = ""
            }
            recognizedText.value = ""
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognitionHelper.destroy()
    }
}