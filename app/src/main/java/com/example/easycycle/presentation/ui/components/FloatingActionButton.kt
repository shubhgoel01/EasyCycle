package com.example.easycycle.presentation.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.easycycle.R


@Composable
fun BookingFAB(expanded:Boolean,onClick1:()->Unit,onOptionClick: (String) -> Unit, onClick2:()->Unit,onConfirm2:(String)->Unit) {
    val rotationAngle by animateFloatAsState(targetValue = if (expanded) 45f else 0f) // Animate rotation
    val optionsAlpha by animateFloatAsState(targetValue = if (expanded) 1f else 0f) // Animate options fade-in/out

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        // Expanded options with animation
        if (expanded) {
            Column(
                modifier = Modifier
                    .padding(bottom = 140.dp, end = 16.dp)
                    .alpha(optionsAlpha), // Fade in/out
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End
            ) {
                BookingOptionButton("Manual", onOptionClick, Icons.Default.Edit)
                BookingOptionButton("Scan QR", onOptionClick,  icon = painterResource(id = R.drawable.qrscanner))
                BookingOptionButton("Quick", onOptionClick, icon = painterResource(id = R.drawable.quick))
            }
        }

        FloatingActionButton(
            onClick = { onClick2() },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.qrscanner),
                contentDescription = if (expanded) "Close options" else "Open options",
            )
        }
        // FAB with rotation animation
        FloatingActionButton(
            onClick = { onClick1() },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .padding(bottom = 70.dp)
                .align(Alignment.BottomEnd)
                .rotate(rotationAngle)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = if (expanded) "Close options" else "Open options",
            )
        }
    }
}


//Using Function Overloading for vector assets and icons
@Composable
fun BookingOptionButton(label: String, onClick: (String) -> Unit, icon: ImageVector) {
    Button(
        onClick = { onClick(label) },
        modifier = Modifier.sizeIn(minWidth = 140.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label)
    }
}

@Composable
fun BookingOptionButton(label: String, onClick: (String) -> Unit, icon: Painter) {
    Button(
        onClick = { onClick(label) },
        modifier = Modifier.sizeIn(minWidth = 140.dp)
    ) {
        Icon(painter = icon, contentDescription = null, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label)
    }
}


