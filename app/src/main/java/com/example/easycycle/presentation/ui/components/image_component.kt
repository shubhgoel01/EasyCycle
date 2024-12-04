package com.example.easycycle.presentation.ui.components

import android.media.Image
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource

@Composable
fun component_image(
    image: Int,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    Box(contentAlignment = Alignment.Center) { // Center the image inside the box
        Image(
            painter = painterResource(image),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop, // Crop the image to fill the bounds without distortion
            modifier = modifier
        )
    }
}