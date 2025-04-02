package com.example.test

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SelectionScreen(
    onBackClick: () -> Unit,
    onValidacionMuestraClick: () -> Unit,
    onDisenoPatronesClick: () -> Unit,
    onDatosProveedoresClick: () -> Unit,
    onInventarioAlmacenClick: () -> Unit,
    onAsistenteTiendaClick: () -> Unit,
    onAltaClienteClick: () -> Unit
) {
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
            // Back button at the top left with larger touch area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp, bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .clickable { onBackClick() }
                        .padding(vertical = 12.dp, horizontal = 8.dp)
                        .height(48.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Volver",
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Title with letter spacing
            Text(
                text = "Escoge tu Formulario",
                fontSize = 32.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 1.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Form options list as a scrollable LazyColumn
            val formOptions = listOf(
                Pair("Validacion de Muestra", onValidacionMuestraClick),
                Pair("Diseño de Patrones", onDisenoPatronesClick),
                Pair("Datos Proveedores", onDatosProveedoresClick),
                Pair("Inventario de Almacen", onInventarioAlmacenClick),
                Pair("Asistente de Tienda", onAsistenteTiendaClick),
                Pair("Alta de Cliente", onAltaClienteClick)
            )
            
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(formOptions) { (title, onClick) ->
                    FormListItem(
                        title = title,
                        onClick = onClick
                    )
                }
            }

            // INDITEX TECH logo at the bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "INDITEX TECH",
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun FormListItem(
    title: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Seleccionar",
                tint = Color.Black
            )
        }
        
        // Divider line between items
        Divider(
            color = Color.LightGray,
            thickness = 1.dp
        )
    }
}