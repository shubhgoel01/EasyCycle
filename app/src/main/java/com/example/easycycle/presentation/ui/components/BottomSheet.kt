package com.example.easycycle.presentation.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun BottomSheet(
    sheetState : ModalBottomSheetState,
    //isVisible: Boolean,
    onDismiss: () -> Unit,
    heading: String? = null,
    bodyPoints: List<String>? = null,
    bodyParagraph: String? = null,
    extraContent: (@Composable (() -> Unit))? = null,
) {
    LaunchedEffect(sheetState.currentValue){
        Log.d("BottomSheet","Inside launchedEffect")
        if(!sheetState.isVisible){
            Log.d("BottomSheet","Inside launchedEffect if")
            onDismiss()
        }
    }

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetBackgroundColor=MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .wrapContentSize()
            .background(color = MaterialTheme.colorScheme.surface),
        scrimColor = Color.Transparent,
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 24.dp,
                        top = 10.dp,
                        end = 24.dp,
                        bottom = 10.dp
                    ) // More padding for a spacious look
                    .background(
                        MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(16.dp)
                    ),
                horizontalAlignment = Alignment.Start // Align content to the start for better alignment
            ) {
                // Container for heading and cross icon button aligned to the top
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp) // Adds space below the heading
                ) {
                    // Heading - Display if not null with red color, aligned to the center
                    heading?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Color.Red),
                            modifier = Modifier
                                .align(Alignment.Center) // Center the heading horizontally
                        )
                    }

                    // Cross icon button at the top-right corner
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Bottom Sheet",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Body points - Display if not null and align left
                bodyPoints?.let {
                    it.forEach { point ->
                        Text(
                            text = "• $point",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .fillMaxWidth(), // Ensures the text takes the full width
                            textAlign = TextAlign.Start // Align text to the start
                        )
                    }
                }

                // Body paragraph - Display if not null with justified text
                bodyParagraph?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .fillMaxWidth(), // Ensures the paragraph takes the full width
                        textAlign = TextAlign.Justify // Justifies the paragraph text
                    )
                }

                // Additional content from the parameter, if provided
                extraContent?.let {
                    it() // This composable will be rendered here if not null
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    ) {
        // Semi-transparent background overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        )
    }
}

