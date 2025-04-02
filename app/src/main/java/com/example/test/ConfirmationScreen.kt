package com.example.test

import android.view.ViewGroup
import android.widget.FrameLayout
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable

@Composable
fun ConfirmationScreen(
    formTitle: String,
    onBackToSelectionClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Lottie success animation using AndroidView
            AndroidView(
                factory = { context ->
                    // Create the LottieAnimationView programmatically
                    val animationView = LottieAnimationView(context)
                    
                    // Set layout parameters
                    animationView.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    
                    // Configure animation
                    animationView.setAnimation(R.raw.success_animation)
                    animationView.repeatCount = LottieDrawable.INFINITE
                    animationView.playAnimation()
                    
                    // Return the view
                    animationView
                },
                modifier = Modifier
                    .height(200.dp)
                    .fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Success title
            Text(
                text = "¡FORMULARIO GUARDADO!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Form name
            Text(
                text = formTitle.uppercase(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Success message
            Text(
                text = "Los datos han sido guardados exitosamente",
                fontSize = 16.sp,
                color = Color.DarkGray,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Back to selection button
            Button(
                onClick = onBackToSelectionClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black
                ),
                shape = RoundedCornerShape(0.dp)
            ) {
                Text(
                    text = "VOLVER A FORMULARIOS",
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Inditex Tech logo at the bottom
            Image(
                painter = painterResource(id = R.drawable.inditex_tech_logo),
                contentDescription = "Inditex Tech Logo",
                modifier = Modifier
                    .width(150.dp)
                    .height(20.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
} 