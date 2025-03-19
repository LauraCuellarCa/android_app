package com.example.test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.test.ui.theme.TestTheme
import android.widget.Toast // Para Toast
import androidx.compose.foundation.layout.fillMaxSize // Para Modifier.fillMaxSize()
import androidx.compose.foundation.layout.padding // Para Modifier.padding()
import androidx.compose.material3.Scaffold // Para Scaffold
import androidx.compose.ui.Modifier // Para Modifier
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import android.util.Log

class MainActivity : ComponentActivity() {
    private val keyValues = listOf(
        "ancho de manga", "ancho de espalda", "largo de manga", "largo de pecho"
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

    private fun processSpokenText(spokenText: String) {
        // Actualizar el texto de depuración para mostrar lo que se reconoció
        runOnUiThread {
            recognizedText.value = "Reconocido: \"$spokenText\""
        }
        
        // Log para depuración
        Log.d("SpeechRecognition", "Recognized text: $spokenText")
        
        // Crear el mapa de keyMap desde la lista de key_values
        val keyMap = keyValues.withIndex().associate { (index, key) ->
            key to index
        }

        // Llamar a MeasurementProcessor pasando el mapa keyMap y la función updateField
        MeasurementProcessor.process(spokenText, keyMap) { index, measurement ->
            updateField(index, measurement)
        }
    }

    private fun updateField(fieldIndex: Int, measurement: String) {
        // Asignar el valor al campo correspondiente según el índice
        // Esto gatillará una recomposición automática en Compose
        runOnUiThread {
            fields[fieldIndex] = measurement
        }
    }

    private fun clearAllFields() {
        runOnUiThread {
            for (i in fields.indices) {
                fields[i] = ""
            }
            // También limpiar el texto de depuración
            recognizedText.value = ""
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognitionHelper.destroy()
    }
}