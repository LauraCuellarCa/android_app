package com.example.test

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SelectionScreen(
    onBackClick: () -> Unit,
    onValidacionMuestraClick: () -> Unit
) {
    val showNotAvailableMessage = remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Back button at the top left
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
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
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Title with letter spacing
            Text(
                text = "Escoge tu Formulario",
                fontSize = 32.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 1.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            
            // Form options list
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                FormOptionItem(
                    title = "Validacion de Muestra",
                    onClick = onValidacionMuestraClick
                )
                
                FormOptionItem(
                    title = "Diseño de Patrones",
                    onClick = { showNotAvailableMessage.value = true }
                )
                
                FormOptionItem(
                    title = "Datos Proveedores",
                    onClick = { showNotAvailableMessage.value = true }
                )
                
                FormOptionItem(
                    title = "Inventario de Almacen",
                    onClick = { showNotAvailableMessage.value = true }
                )
                
                FormOptionItem(
                    title = "Asistente de Tienda",
                    onClick = { showNotAvailableMessage.value = true }
                )
                
                FormOptionItem(
                    title = "Alta de Cliente",
                    onClick = { showNotAvailableMessage.value = true }
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // INDITEX TECH logo at the bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                // Replace with Image when available
                Text(
                    text = "INDITEX TECH",
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        // Not available message
        if (showNotAvailableMessage.value) {
            AlertDialog(
                onDismissRequest = { showNotAvailableMessage.value = false },
                title = { Text("Aviso") },
                text = { Text("Formulario no disponible") },
                confirmButton = {
                    Button(
                        onClick = { showNotAvailableMessage.value = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black
                        )
                    ) {
                        Text("Aceptar")
                    }
                }
            )
        }
    }
}

@Composable
fun FormOptionItem(
    title: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Seleccionar",
                tint = Color.White
            )
        }
    }
} 