package com.example.easycycle.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Component_textField(value:String, onOptionChange: (String) -> Unit, modifier:Modifier=Modifier, visualTransformation: VisualTransformation = VisualTransformation.None,label:String, readOnly:Boolean=false) {
    TextField(
        value = value,
        onValueChange = onOptionChange,
        placeholder = {
            Text(text=label)
        },
        modifier = Modifier.
            fillMaxWidth().
            background(color=Color.White,RoundedCornerShape(8.dp)).
            padding(horizontal = 8.dp, vertical = 4.dp).
            then(modifier),
        singleLine = true,
        visualTransformation=visualTransformation,
        readOnly = readOnly,
        enabled = !readOnly,
        colors = TextFieldDefaults.textFieldColors(
            focusedTextColor = Color.Black, // Text color inside the TextField
            unfocusedTextColor = Color.Gray,
            containerColor = Color.White, // Background color of the input area
            focusedIndicatorColor = Color.Black, // Focused indicator color
            unfocusedIndicatorColor = Color.Gray, // Unfocused indicator color
            cursorColor = Color.Black, // Cursor color
        )
    )
}