package com.example.easycycle.presentation.ui

import android.app.TimePickerDialog
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.easycycle.data.Enum.Location
import com.example.easycycle.presentation.ui.components.AssistChipExample
import com.example.easycycle.presentation.ui.components.ComponentDropdown
import com.example.easycycle.presentation.ui.components.Component_Button
import com.example.easycycle.presentation.ui.components.SimpleTimeInputDialog
import com.example.easycycle.presentation.ui.components.displayTime

@Composable
fun BookingPage(rentNow: Boolean, rentLater: Boolean) {
    // Pickup Location
    var selectedOptionLocation by remember { mutableStateOf("") }
    val inputChangeLocation: (String) -> Unit = { value -> selectedOptionLocation = value }
    val listLocation = listOf(Location.NILGIRI.toString())
    var expandedLocation by remember { mutableStateOf(false) }
    val onExpandChangeLocation: () -> Unit = { expandedLocation = !expandedLocation }

    // Drop-off Location
    var selectedOptionLocation2 by remember { mutableStateOf("") }
    val inputChangeLocation2: (String) -> Unit = { value -> selectedOptionLocation2 = value }
    var expandedLocation2 by remember { mutableStateOf(false) }
    val onExpandChangeLocation2: () -> Unit = { expandedLocation2 = !expandedLocation2 }

    // Time selection state
    var selectedHour by remember { mutableStateOf(0) }
    var selectedMinute by remember { mutableStateOf(0) }

    var showTimeInputDialog by remember { mutableStateOf(false) }
    var hour1 by remember { mutableStateOf<Int?>(null) }
    var minute1 by remember { mutableStateOf<Int?>(null) }
    val onConfirmTimePicker1 :(hour:Int,minute:Int)->Unit = {h,m->
        showTimeInputDialog = false
        hour1 = h
        minute1 = m
    }

    var hour2 by remember { mutableStateOf<Int?>(null) }
    var minute2 by remember { mutableStateOf<Int?>(null) }
    val onConfirmTimePicker2 :(hour:Int,minute:Int)->Unit = {h,m->
        showTimeInputDialog = false
        hour2 = h
        minute2 = m
    }
    val onDismissTimePicker = {
        showTimeInputDialog = false
    }

    var onClickDisplayTime = {
        showTimeInputDialog = true
    }
    if(showTimeInputDialog){
        SimpleTimeInputDialog(onConfirmTimePicker1,onDismissTimePicker)
    }

    val onButtonClick: () -> Unit = {
        // Booking action logic here
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp) // Outer padding for the whole page
    ) {
        Surface(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp) // Center the Surface with reduced width
                .wrapContentHeight(),
            tonalElevation = 6.dp,
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Assist Chips for rental options
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (rentNow) AssistChipExample(label = "Rent Now")
                    if (rentLater) AssistChipExample(label = "Rent Later")
                    AssistChipExample(label = "Return Later")
                }

                // Pickup Location
                Text(
                    text = "Pickup Location",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                ComponentDropdown(
                    selectedOption = selectedOptionLocation,
                    list = listLocation,
                    onOptionChange = inputChangeLocation,
                    onExpandedChange = onExpandChangeLocation,
                    expanded = expandedLocation,
                    label = "Select Location",
                    modifier = Modifier.fillMaxWidth()
                )

                // Divider with Icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Divider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = "Cycle Icon",
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Divider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                // Drop-off Location
                Text(
                    text = "Drop-off Location",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                ComponentDropdown(
                    selectedOption = selectedOptionLocation2,
                    list = listLocation,
                    onOptionChange = inputChangeLocation2,
                    onExpandedChange = onExpandChangeLocation2,
                    expanded = expandedLocation2,
                    label = "Select Location",
                    modifier = Modifier.fillMaxWidth()
                )

                // Start Time
                Text(
                    text = "Start Time",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                //Time Displayer
                displayTime(hour1,minute1,onClickDisplayTime)

                Text(
                    text = "Estimate schedule Time",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                displayTime(hour2,minute2,onClickDisplayTime)
                Row {
                    Text(
                        text = "Estimate Fare : ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$20",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Green
                    )
                }

                // Book Now Button
                Component_Button(
                    title = "Pay and Book Now",
                    onClick = onButtonClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }
        }
    }
}




