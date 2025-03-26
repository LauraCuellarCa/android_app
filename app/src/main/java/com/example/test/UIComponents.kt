package com.example.test

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.alpha

@Composable
fun MainContent(
    modifier: Modifier = Modifier,
    keyValues: List<String>,  // Lista de key_values
    fields: List<String>,  // Lista de campos dinámicos
    debugText: String = "",  // Texto para debugging
    onFieldChange: (Int, String) -> Unit,  // Función para cambiar el valor de un campo específico
    onMicClick: () -> Unit,
    onClearClick: () -> Unit,
    onBackClick: () -> Unit // New callback for back button
) {
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Back button at the top left
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clickable { onBackClick() }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Volver",
                    color = Color.Black,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Crear los campos dinámicamente usando el nuevo componente
        keyValues.forEachIndexed { index, keyValue ->
            CustomUnderlinedTextField(
                label = keyValue,
                value = fields.getOrElse(index) { "" },
                onValueChange = { newValue -> onFieldChange(index, newValue) },
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Agregar texto de debugging para mostrar lo que se reconoció
        if (debugText.isNotEmpty()) {
            Text(
                text = debugText,
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
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

@Composable
fun CustomUnderlinedTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Label on the left side
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black,
            modifier = Modifier
                .weight(0.6f)
                .padding(end = 8.dp)
        )
        
        // Custom text field with just an underline
        Box(
            modifier = Modifier
                .weight(0.4f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Value
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = TextStyle(
                        color = Color.Black,
                        fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                        textAlign = TextAlign.End
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 8.dp)
                )
                
                // "cm" suffix always visible
                Text(
                    text = "cm",
                    color = Color.Black,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .padding(start = 4.dp, bottom = 8.dp)
                )
            }
            
            // Underline
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.LightGray)
                    .align(Alignment.BottomCenter)
            )
        }
    }
}
