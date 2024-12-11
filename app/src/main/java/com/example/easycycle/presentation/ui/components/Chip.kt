package com.example.easycycle.presentation.ui.components

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ChipColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistChipExample(
    label: String,
    icon: Painter? = null,
    modifier: Modifier = Modifier,
    color: Color = Color.Transparent,
    action: (() -> Unit)? = null
) {
    AssistChip(
        onClick = {
            action?.invoke()
        },
        label = {
            Text(text = label)
        },
        leadingIcon = {
            icon?.let {
                Icon(
                    painter = it,
                    contentDescription = null, // No description if not needed
                    modifier = Modifier.size(AssistChipDefaults.IconSize)
                )
            }
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = color,
            labelColor = MaterialTheme.colorScheme.onSurface,
            leadingIconContentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = AssistChipDefaults.assistChipElevation(elevation = 2.dp),
        modifier = modifier
    )
}
