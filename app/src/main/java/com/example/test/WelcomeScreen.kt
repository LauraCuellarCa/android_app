package com.example.test

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
fun WelcomeScreen(
    onContinueClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            // Inditex logo (smaller)
            Image(
                painter = painterResource(id = R.drawable.inditex_logo),
                contentDescription = "Inditex Logo",
                modifier = Modifier
                    .width(180.dp)
                    .height(50.dp),
                contentScale = ContentScale.Fit
            )
            
            Spacer(modifier = Modifier.height(60.dp))
            
            // ZIA Logo
            Image(
                painter = painterResource(id = R.drawable.zia_logo),
                contentDescription = "ZIA Logo",
                modifier = Modifier
                    .width(120.dp)
                    .height(120.dp),
                contentScale = ContentScale.Fit
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Original app title
            Text(
                text = "Asistente de Voz",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Description text
            Text(
                text = "Manos libres, voz activa",
                fontSize = 16.sp,
                color = Color.DarkGray,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Start button
            Button(
                onClick = onContinueClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black
                ),
                shape = RoundedCornerShape(0.dp) // Square corners
            ) {
                Text(
                    text = "EMPEZAR",
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Inditex Tech Logo
            Image(
                painter = painterResource(id = R.drawable.inditex_tech_logo),
                contentDescription = "Inditex Tech Logo",
                modifier = Modifier
                    .width(150.dp)
                    .height(20.dp)
                    .padding(bottom = 20.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
} 