package com.example.easycycle.presentation.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun displayTime(hour:Int?,minute:Int?,onClick:()->Unit) {
    // Hour and Minute Selectors
    Row(
        horizontalArrangement = Arrangement.spacedBy(9.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .padding(8.dp)
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(8.dp)
                )
                .padding(16.dp)
                .width(15.dp)
                .height(15.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = if(hour == null) "" else hour.toString(),style = MaterialTheme.typography.bodySmall)
        }
        Text(text = ":")
        Box(
            modifier = Modifier
                .padding(8.dp)
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(8.dp)
                )
                .padding(16.dp)
                .width(15.dp)
                .height(15.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = if(minute == null) "" else minute.toString(),style = MaterialTheme.typography.bodySmall)
        }
    }
}