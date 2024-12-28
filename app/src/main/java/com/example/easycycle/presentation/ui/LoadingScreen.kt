package com.example.easycycle.presentation.ui

import android.util.Log
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun LoadingPage(message:String = "",modifier : Modifier = Modifier) {
    // Display loading circle
    Log.d("LoadingScreen","Called")
        CircularProgressIndicator(
            modifier = modifier,
            color = Color.White,  // Set the circle color to white
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeWidth = 6.dp
        )
}