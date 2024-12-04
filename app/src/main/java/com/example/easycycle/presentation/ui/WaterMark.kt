package com.example.easycycle.presentation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import com.example.easycycle.R

@Composable
fun waterMark(modifier: Modifier=Modifier) {
    Box(modifier = modifier)
    {
        Column(modifier = Modifier  // Adjust size as needed
            .align(Alignment.BottomEnd)
            .wrapContentSize()
            .padding(bottom = 10.dp, end = 10.dp)
        )
        {
            Image(
                painter = painterResource(R.drawable.waterark),
                contentDescription = "LOGO",
                modifier = Modifier
                    .size(50.dp)  // Adjust size as needed
                //.align(Alignment.BottomEnd)
            )
            Text(
                text = "@realBeast",
                modifier = Modifier,
                //.align(Alignment.BottomEnd), // Aligns the text to the bottom end
                color = Color.Gray // You can customize the color
            )
        }
    }
}