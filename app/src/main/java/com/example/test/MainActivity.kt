package com.example.test

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.example.test.ui.theme.TestTheme
import java.util.Locale
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.filled.Delete

/**
 * MainActivity: The main entry point for the application.
 * This app allows users to input garment measurements using voice recognition.
 * Users can speak measurements like "sleeve width is 20 cm" and the app will 
 * automatically populate the appropriate field.
 */
class MainActivity : ComponentActivity() {
    // State variables for the three measurement input fields
    private var field1 by mutableStateOf("")
    private var field2 by mutableStateOf("")
    private var field3 by mutableStateOf("")

    // Map of field names to their corresponding indices
    private val fieldIdentifiers = mapOf(
        "Ancho de manga" to 0,
        "Largo de manga" to 1,
        "Ancho de espalda" to 2
    )

    // Map of alternative terms to the standard field names
    // This allows the app to recognize various ways users might refer to the same measurement
    private val fieldAliases = mapOf(
        "ancho de manga" to "ancho de manga",
        "manga ancho" to "ancho de manga",
        "anchura de manga" to "ancho de manga",
        "ancho de la manga" to "ancho de manga",
        
        "largo de manga" to "largo de manga",
        "manga largo" to "largo de manga",
        "longitud de manga" to "largo de manga",
        "largo de la manga" to "largo de manga",
        
        "ancho de espalda" to "ancho de espalda",
        "espalda ancho" to "ancho de espalda",
        "anchura de espalda" to "ancho de espalda",
        "ancho de la espalda" to "ancho de espalda",
        "ancho de hombros" to "ancho de espalda"
    )

    // Mapping from recognized phrases to field indices
    private val patternToFieldIndex = mapOf(
        "ancho de manga" to 0,
        "manga ancho" to 0,
        "anchura de manga" to 0,
        "ancho de la manga" to 0,
        
        "largo de manga" to 1,
        "manga largo" to 1,
        "longitud de manga" to 1,
        "largo de la manga" to 1,
        
        "ancho de espalda" to 2,
        "espalda ancho" to 2,
        "anchura de espalda" to 2,
        "ancho de la espalda" to 2,
        "ancho de hombros" to 2
    )

    // Regular expression pattern to match field names and measurements with variations
    // This regex captures the field name and the numerical value from speech input
    private val measurementPattern = """(ancho\s+de\s+(?:la\s+)?manga|manga\s+ancho|anchura\s+de\s+manga|largo\s+de\s+(?:la\s+)?manga|manga\s+largo|longitud\s+de\s+manga|ancho\s+de\s+(?:la\s+)?espalda|espalda\s+ancho|anchura\s+de\s+espalda|ancho\s+de\s+hombros)(?:\s+(?:es|mide|igual\s+a|de|tiene|marca|aproximadamente|como))?\s+(\d+(?:\.\d+)?)\s*(?:cm|centímetros|centímetro|c\.m\.|cms)"""

    // Activity result launcher for speech recognition
    // This handles the result from the speech recognition intent
    private val speechRecognitionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val data = result.data
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = results?.get(0) ?: ""

            processSpokenText(spokenText)
        }
    }

    // Permission request launcher for microphone access
    // This handles the result of the permission request dialog
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startSpeechRecognition()
        } else {
            Toast.makeText(this, "Se requiere permiso del micrófono para el reconocimiento de voz", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Initiates the speech recognition process
     * Configures and launches the system speech recognition intent
     */
    private fun startSpeechRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Hable ahora...")
            
            // Set a longer speech timeout for better user experience
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 10000)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 10000)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 1000)
            
            // Enable partial results to provide feedback during speech
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            
            // Set max results to improve recognition accuracy
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
        }

        try {
            speechRecognitionLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "El reconocimiento de voz no está soportado en este dispositivo", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Checks for microphone permission and starts speech recognition if granted
     * Requests permission if not already granted
     */
    private fun checkPermissionAndStartSpeechRecognition() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            == PermissionChecker.PERMISSION_GRANTED) {
            startSpeechRecognition()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    /**
     * Processes the text received from speech recognition
     * Extracts measurement values and updates the appropriate fields
     * 
     * @param spokenText The text received from speech recognition
     */
    private fun processSpokenText(spokenText: String) {
        // Show the full recognized text for debugging
        Toast.makeText(this, "Reconocido: $spokenText", Toast.LENGTH_SHORT).show()
        
        // Debug log for the entire speech input
        android.util.Log.d("SpeechRecognition", "Input: $spokenText")
        
        // Convert to lowercase for case-insensitive matching
        val lowerCaseText = spokenText.lowercase()
        
        // Create a regex pattern with case insensitive flag
        val pattern = Regex(measurementPattern, RegexOption.IGNORE_CASE)
        
        // Find all matches in the spoken text
        val matches = pattern.findAll(lowerCaseText).toList()
        
        // Log the number of matches found
        android.util.Log.d("SpeechRecognition", "Number of matches found: ${matches.size}")
        
        // Flag to track if we found any matches
        var matchFound = false
        var matchCount = 0
        val updatedFields = mutableListOf<String>()
        
        // Process each match found in the spoken text
        for (match in matches) {
            // Log each match for debugging
            android.util.Log.d("SpeechRecognition", "Processing match: ${match.value}")
            
            matchFound = true
            matchCount++
            
            // Get the matched field name (could be an alias)
            val matchedFieldName = match.groupValues[1].lowercase().trim()
            android.util.Log.d("SpeechRecognition", "Matched field name: $matchedFieldName")
            
            // Find the field index based on the matched pattern
            val fieldIndex = findFieldIndex(matchedFieldName)
            if (fieldIndex == -1) {
                android.util.Log.d("SpeechRecognition", "No field index found for: $matchedFieldName")
                continue
            }
            
            // Get the measurement value
            val measurementValue = match.groupValues[2]
            android.util.Log.d("SpeechRecognition", "Measurement value: $measurementValue")
            
            // Format the measurement
            val measurement = "$measurementValue cm"
            
            // Update the appropriate field based on the field index
            when (fieldIndex) {
                0 -> {
                    field1 = measurement
                    updatedFields.add("Ancho de manga")
                    android.util.Log.d("SpeechRecognition", "Updated field1 (Ancho de manga) to: $measurement")
                }
                1 -> {
                    field2 = measurement
                    updatedFields.add("Largo de manga")
                    android.util.Log.d("SpeechRecognition", "Updated field2 (Largo de manga) to: $measurement")
                }
                2 -> {
                    field3 = measurement
                    updatedFields.add("Ancho de espalda")
                    android.util.Log.d("SpeechRecognition", "Updated field3 (Ancho de espalda) to: $measurement")
                }
            }
        }
        
        // Show feedback to the user about which fields were updated
        if (matchFound) {
            val updatedFieldsStr = updatedFields.joinToString(", ")
            Toast.makeText(this, "Campos actualizados: $updatedFieldsStr", Toast.LENGTH_SHORT).show()
        } else {
            // If no matches were found, show a helpful message with an example
            Toast.makeText(this, "No se detectaron medidas. Intente decir 'largo de manga es 57 cm'", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Helper function to find field index from matched text
     * Tries multiple matching strategies to identify the correct field
     * 
     * @param matchedText The text to find a matching field for
     * @return The index of the matched field, or -1 if no match found
     */
    private fun findFieldIndex(matchedText: String): Int {
        // Try exact match first
        patternToFieldIndex[matchedText]?.let { return it }
        
        // If no exact match, try partial matches
        for ((pattern, index) in patternToFieldIndex) {
            if (matchedText.contains(pattern)) {
                return index
            }
        }
        
        // Check for specific keywords as a fallback
        when {
            matchedText.contains("manga") && (matchedText.contains("ancho") || matchedText.contains("anchura")) -> return 0
            matchedText.contains("manga") && (matchedText.contains("largo") || matchedText.contains("longitud")) -> return 1
            matchedText.contains("espalda") || matchedText.contains("hombros") -> return 2
        }
        
        return -1  // No match found
    }

    /**
     * Helper function to capitalize first letter of each word
     * 
     * @return A string with the first letter of each word capitalized
     */
    private fun String.capitalize(): String {
        return this.split(" ").joinToString(" ") { word ->
            if (word.isNotEmpty()) word[0].uppercase() + word.substring(1) else ""
        }
    }

    /**
     * Clears all measurement input fields
     */
    private fun clearAllFields() {
        field1 = ""
        field2 = ""
        field3 = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainContent(
                        modifier = Modifier.padding(innerPadding),
                        field1 = field1,
                        field2 = field2,
                        field3 = field3,
                        onField1Change = { field1 = it },
                        onField2Change = { field2 = it },
                        onField3Change = { field3 = it },
                        onMicClick = { checkPermissionAndStartSpeechRecognition()} ,
                        onClearClick = { clearAllFields() }
                    )
                }
            }
        }
    }
}

/**
 * Main composable that contains the entire UI structure
 * Includes title, input fields, and action buttons
 */
@Composable
fun MainContent(
    modifier: Modifier = Modifier,
    field1: String,
    field2: String,
    field3: String,
    onField1Change: (String) -> Unit,
    onField2Change: (String) -> Unit,
    onField3Change: (String) -> Unit,
    onMicClick: () -> Unit,
    onClearClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FormTitle()
        InputFields(
            field1 = field1,
            field2 = field2,
            field3 = field3,
            onField1Change = onField1Change,
            onField2Change = onField2Change,
            onField3Change = onField3Change
        )
        // Row for microphone and clear buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Botón de micrófono
            IconButton(onClick = onMicClick) {
                Icon(Icons.Default.Mic, contentDescription = "Reconocimiento de voz")
            }
            // Botón de papelera
            IconButton(onClick = onClearClick) {
                Icon(Icons.Default.Delete, contentDescription = "Borrar todos los campos")
            }
        }
    }
}

/**
 * Composable that contains the three measurement input fields
 */
@Composable
fun InputFields(
    modifier: Modifier = Modifier,
    field1: String,
    field2: String,
    field3: String,
    onField1Change: (String) -> Unit,
    onField2Change: (String) -> Unit,
    onField3Change: (String) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Sleeve width input field
        OutlinedTextField(
            value = field1,
            onValueChange = onField1Change,
            label = { Text("Ancho de manga") },
            modifier = Modifier.fillMaxWidth()
        )

        // Sleeve length input field
        OutlinedTextField(
            value = field2,
            onValueChange = onField2Change,
            label = { Text("Largo de manga") },
            modifier = Modifier.fillMaxWidth()
        )

        // Back width input field
        OutlinedTextField(
            value = field3,
            onValueChange = onField3Change,
            label = { Text("Ancho de espalda") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Title component that displays the form's purpose
 */
@Composable
fun FormTitle(modifier: Modifier = Modifier) {
    Text(
        text = "Formulario de Medidas",
        modifier = modifier
    )
}



