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

class MainActivity : ComponentActivity() {
    private var field1 by mutableStateOf("")
    private var field2 by mutableStateOf("")
    private var field3 by mutableStateOf("")

    private val fieldIdentifiers = mapOf(
        "Sleeve width" to 0,
        "Sleeve length" to 1,
        "Back width" to 2
    )

    // Map of alternative terms to the standard field names
    private val fieldAliases = mapOf(
        "sleeve width" to "sleeve width",
        "width of sleeve" to "sleeve width",
        "sleeve's width" to "sleeve width",
        "width of the sleeve" to "sleeve width",
        
        "sleeve length" to "sleeve length",
        "length of sleeve" to "sleeve length",
        "sleeve's length" to "sleeve length",
        "length of the sleeve" to "sleeve length",
        
        "back width" to "back width",
        "width of back" to "back width",
        "back's width" to "back width",
        "width of the back" to "back width",
        "shoulder width" to "back width"
    )

    // Regular expression pattern to match field names and measurements with more variations
    private val measurementPattern = """(?:(?:the|for|my|our)\s+)?(?:(?:measurement|measure|value)(?:\s+(?:for|of))?\s+)?(?:(?:the)\s+)?(${fieldAliases.keys.joinToString("|")})(?:\s+(?:is|equals|measures|of|at|reads|shows|about|approximately|comes to|was))?\s+(\d+(?:\.\d+)?)\s*(?:cm|centimeters|centimeter|c\.m\.|cms)"""

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

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startSpeechRecognition()
        } else {
            Toast.makeText(this, "Microphone permission is required for speech recognition", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startSpeechRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
            
            // Set a longer speech timeout
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 5000)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5000)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 1000)
            
            // Enable partial results
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        try {
            speechRecognitionLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Speech recognition not supported on this device", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkPermissionAndStartSpeechRecognition() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            == PermissionChecker.PERMISSION_GRANTED) {
            startSpeechRecognition()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun processSpokenText(spokenText: String) {
        // Show the full recognized text for debugging
        Toast.makeText(this, "Recognized: $spokenText", Toast.LENGTH_SHORT).show()
        
        // Convert to lowercase for case-insensitive matching
        val lowerCaseText = spokenText.lowercase()
        
        // Create a regex pattern with case insensitive flag
        val pattern = Regex(measurementPattern, RegexOption.IGNORE_CASE)
        
        // Find all matches in the spoken text
        val matches = pattern.findAll(lowerCaseText)
        
        // Flag to track if we found any matches
        var matchFound = false
        var matchCount = 0
        val updatedFields = mutableListOf<String>()
        
        for (match in matches) {
            matchFound = true
            matchCount++
            
            // Get the matched field name (could be an alias)
            val matchedFieldName = match.groupValues[1].lowercase()
            
            // Get the standardized field name from the alias map
            val standardFieldName = fieldAliases[matchedFieldName] ?: continue
            
            // Format the measurement
            val measurement = "${match.groupValues[2]} cm"
            
            // Find the corresponding field index
            val fieldIndex = when (standardFieldName) {
                "sleeve width" -> 0
                "sleeve length" -> 1
                "back width" -> 2
                else -> continue
            }
            
            // Update the appropriate field
            when (fieldIndex) {
                0 -> {
                    field1 = measurement
                    updatedFields.add("Sleeve width")
                }
                1 -> {
                    field2 = measurement
                    updatedFields.add("Sleeve length")
                }
                2 -> {
                    field3 = measurement
                    updatedFields.add("Back width")
                }
            }
        }
        
        // If matches were found, show a success message
        if (matchFound) {
            val updatedFieldsStr = updatedFields.joinToString(", ")
            Toast.makeText(this, "Updated fields: $updatedFieldsStr", Toast.LENGTH_SHORT).show()
        } else {
            // If no matches were found, show a helpful message
            Toast.makeText(this, "No measurements detected. Try saying 'sleeve length is 57 cm'", Toast.LENGTH_LONG).show()
        }

        // Print all matches to the log for debugging (you can remove this in production)
        val debugMatches = pattern.findAll(lowerCaseText)
        debugMatches.forEach { match ->
            android.util.Log.d("SpeechRecognition", "Match: ${match.groupValues}")
        }
    }

    // Helper function to capitalize first letter of each word
    private fun String.capitalize(): String {
        return this.split(" ").joinToString(" ") { word ->
            if (word.isNotEmpty()) word[0].uppercase() + word.substring(1) else ""
        }
    }

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
        Greeting(name = "Laura")
        InputFields(
            field1 = field1,
            field2 = field2,
            field3 = field3,
            onField1Change = onField1Change,
            onField2Change = onField2Change,
            onField3Change = onField3Change
        )
        // Fila para los botones de micrófono y papelera
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Botón de micrófono
            IconButton(onClick = onMicClick) {
                Icon(Icons.Default.Mic, contentDescription = "Speech to text")
            }
            // Botón de papelera
            IconButton(onClick = onClearClick) {
                Icon(Icons.Default.Delete, contentDescription = "Clear all fields")
            }
        }
    }
}


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
        OutlinedTextField(
            value = field1,
            onValueChange = onField1Change,
            label = { Text("Sleeve width") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = field2,
            onValueChange = onField2Change,
            label = { Text("Sleeve length") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = field3,
            onValueChange = onField3Change,
            label = { Text("Back width") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}



