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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.example.test.ui.theme.TestTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    private var activeFieldIndex by mutableIntStateOf(-1)
    private var field1 by mutableStateOf("")
    private var field2 by mutableStateOf("")
    private var field3 by mutableStateOf("")

    private val speechRecognitionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val data = result.data
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = results?.get(0) ?: ""

            when (activeFieldIndex) {
                0 -> field1 = spokenText
                1 -> field2 = spokenText
                2 -> field3 = spokenText
            }
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
        }

        try {
            speechRecognitionLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Speech recognition not supported on this device", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkPermissionAndStartSpeechRecognition(fieldIndex: Int) {
        activeFieldIndex = fieldIndex

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            == PermissionChecker.PERMISSION_GRANTED) {
            startSpeechRecognition()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
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
                        onMicClick = { fieldIndex -> checkPermissionAndStartSpeechRecognition(fieldIndex) }
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
    onMicClick: (Int) -> Unit
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
            onField3Change = onField3Change,
            onMicClick = onMicClick
        )
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
    onMicClick: (Int) -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = field1,
            onValueChange = onField1Change,
            label = { Text("Field 1") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { onMicClick(0) }) {
                    Icon(Icons.Default.Mic, contentDescription = "Speech to text for field 1")
                }
            }
        )

        OutlinedTextField(
            value = field2,
            onValueChange = onField2Change,
            label = { Text("Field 2") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { onMicClick(1) }) {
                    Icon(Icons.Default.Mic, contentDescription = "Speech to text for field 2")
                }
            }
        )

        OutlinedTextField(
            value = field3,
            onValueChange = onField3Change,
            label = { Text("Field 3") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { onMicClick(2) }) {
                    Icon(Icons.Default.Mic, contentDescription = "Speech to text for field 3")
                }
            }
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



