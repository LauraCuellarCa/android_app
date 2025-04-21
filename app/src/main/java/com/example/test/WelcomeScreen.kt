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
            // Increase top space to push content lower
            Spacer(modifier = Modifier.weight(0.35f))
            
            // ZIA Logo (made much bigger)
            Image(
                painter = painterResource(id = R.drawable.zia_logo),
                contentDescription = "ZIA Logo",
                modifier = Modifier
                    .width(240.dp)
                    .height(240.dp),
                contentScale = ContentScale.Fit
            )
            
            // Reduced spacer between logo and text
            Spacer(modifier = Modifier.height(5.dp))
            
            // Description text
            Text(
                text = "Manos libres, voz activa",
                fontSize = 18.sp,
                color = Color.DarkGray,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Inditex logo (moved directly under the description)
            Image(
                painter = painterResource(id = R.drawable.inditex_logo),
                contentDescription = "Inditex Logo",
                modifier = Modifier
                    .width(120.dp)
                    .height(30.dp),
                contentScale = ContentScale.Fit
            )
            
            // Reduced weight here to balance with increased weight at top
            Spacer(modifier = Modifier.weight(0.35f))
            
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