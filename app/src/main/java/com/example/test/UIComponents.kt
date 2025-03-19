package com.example.test

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign

@Composable
fun MainContent(
    modifier: Modifier = Modifier,
    keyValues: List<String>,  // Lista de key_values
    fields: List<String>,  // Lista de campos dinámicos
    debugText: String = "",  // Texto para depuración
    onFieldChange: (Int, String) -> Unit,  // Función para cambiar el valor de un campo específico
    onMicClick: () -> Unit,
    onClearClick: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Formulario de Medidas")

        // Crear los campos dinámicamente
        keyValues.forEachIndexed { index, keyValue ->
            OutlinedTextField(
                value = fields.getOrElse(index) { "" },
                onValueChange = { newValue -> onFieldChange(index, newValue) },
                label = { Text(keyValue) },  // Usamos el key_value como label
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Agregar texto de depuración para mostrar lo que se reconoció
        if (debugText.isNotEmpty()) {
            Text(
                text = debugText,
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp)
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = onMicClick) {
                Icon(Icons.Default.Mic, contentDescription = "Speech to text")
            }
            IconButton(onClick = onClearClick) {
                Icon(Icons.Default.Delete, contentDescription = "Clear all fields")
            }
        }
    }
}
