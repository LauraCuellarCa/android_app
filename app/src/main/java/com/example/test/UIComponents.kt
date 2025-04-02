package com.example.test

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.sp

@Composable
fun MainContent(
    modifier: Modifier = Modifier,
    formTitle: String,
    keyValues: List<String>,
    fields: List<String>,
    debugText: String = "",
    partialDebugText: String = "",
    onFieldChange: (Int, String) -> Unit,
    onMicClick: () -> Unit,
    isListening: Boolean,
    showStopMessage: Boolean,
    onClearClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Botón de retroceso
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
        
        // Title bar with logo, divider, and form title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Inditex logo (small)
            Image(
                painter = painterResource(id = R.drawable.inditex_logo),
                contentDescription = "Inditex Logo",
                modifier = Modifier
                    .width(80.dp)
                    .height(24.dp),
                contentScale = ContentScale.Fit
            )
            
            // Vertical divider
            Box(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .height(24.dp)
                    .width(1.dp)
                    .background(Color.LightGray)
            )
            
            // Form title
            Text(
                text = formTitle.uppercase(),
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Color.Black
            )
        }
        
        Divider(color = Color.LightGray, thickness = 1.dp)
        Spacer(modifier = Modifier.height(16.dp))

        // Campos de entrada
        keyValues.forEachIndexed { index, keyValue ->
            CustomUnderlinedTextField(
                label = keyValue,
                value = fields.getOrElse(index) { "" },
                onValueChange = { newValue -> onFieldChange(index, newValue) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sección de reconocimiento de voz
        VoiceRecognitionSection(
            debugText = debugText,
            partialDebugText = partialDebugText,
            onMicClick = onMicClick,
            isListening = isListening,
            showStopMessage = showStopMessage,
            onClearClick = onClearClick
        )
    }
}

@Composable
fun VoiceRecognitionSection(
    debugText: String,
    partialDebugText: String,
    onMicClick: () -> Unit,
    isListening: Boolean,
    showStopMessage: Boolean,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mensaje de "stop" detectado
        if (showStopMessage) {
            Text(
                text = "Micrófono detenido por comando de voz",
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                textAlign = TextAlign.Center
            )
        }

        // Mostrar texto parcial (en tiempo real)
        if (partialDebugText.isNotEmpty()) {
            Text(
                text = "Escuchando: $partialDebugText",
                color = Color(0xFF4CAF50), // Verde
                style = MaterialTheme.typography.bodySmall.copy(
                    fontStyle = FontStyle.Italic
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
        }

        // Mostrar texto final reconocido
        if (debugText.isNotEmpty()) {
            Text(
                text = "Reconocido: $debugText",
                color = Color.Blue,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
        }

        // Controles de voz
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Botón de micrófono con estado
            IconButton(
                onClick = onMicClick,
                modifier = Modifier.size(56.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = if (isListening) "Detener reconocimiento" else "Iniciar reconocimiento",
                        tint = if (isListening) Color.Red else Color(0xFF6200EE),
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = if (isListening) "Detener" else "Grabar",
                        fontSize = 12.sp,
                        color = if (isListening) Color.Red else Color(0xFF6200EE)
                    )
                }
            }

            // Botón de limpiar
            IconButton(
                onClick = onClearClick,
                modifier = Modifier.size(56.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Limpiar campos",
                        tint = Color.Gray,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "Limpiar",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
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
    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        // Row with label and input field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Label on the left
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black,
                modifier = Modifier.weight(0.6f)
            )
            
            // Input field on the right
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    color = Color.Black,
                    fontSize = 18.sp,
                    textAlign = TextAlign.End
                ),
                singleLine = true,
                modifier = Modifier
                    .weight(0.4f)
            )
        }
        
        // Full-width divider line between rows
        Divider(
            color = Color.LightGray,
            thickness = 1.dp
        )
    }
}