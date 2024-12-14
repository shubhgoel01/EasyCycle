package com.example.easycycle.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun Component_Button(title:String,onClick:()->Unit,modifier:Modifier=Modifier) {
    Button(
        onClick = onClick,
        colors= ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color.Black

        ),
        elevation = ButtonDefaults.buttonElevation(5.dp),
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),  // Rounded corners for the button,
        border = BorderStroke(1.dp, Color.Black)
    ) {
        Text(text = title)
    }
}