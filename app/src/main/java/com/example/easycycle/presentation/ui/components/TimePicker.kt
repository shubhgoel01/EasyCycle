package com.example.easycycle.presentation.ui.components

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.util.*

@Composable
fun SimpleTimeInputDialog(
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val currentTime = Calendar.getInstance()
    val initialHour = currentTime.get(Calendar.HOUR_OF_DAY)
    val initialMinute = currentTime.get(Calendar.MINUTE)

    var selectedHour by remember { mutableStateOf(initialHour) }
    var selectedMinute by remember { mutableStateOf(initialMinute) }

    var hourError by remember { mutableStateOf(false) }
    var minuteError by remember { mutableStateOf(false) }

    val isInputValid = { hour: Int, minute: Int ->
        !(hour == 0 && minute == 0) && hour in 0..23 && minute in 0..59
    }
    val greaterThanCurrent = { hour: Int, minute: Int ->
        val currentTotalMinutes = initialHour * 60 + initialMinute
        val enteredTotalMinutes = hour * 60 + minute

        enteredTotalMinutes >= currentTotalMinutes
    }
    val showError = remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 8.dp,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(0.85f) // Adjust dialog width for better appearance
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Set Time",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Hour input
                    OutlinedTextField(
                        value = if (hourError) "" else selectedHour.toString(),
                        onValueChange = { value ->
                            val intValue = value.toIntOrNull()
                            selectedHour = intValue ?: selectedHour
                            hourError = intValue == null || !isInputValid(selectedHour, selectedMinute)
                            showError.value = false
                        },
                        label = { Text("Hour") },
                        isError = hourError,
                        singleLine = true,
                        modifier = Modifier.width(100.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = ":",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Minute input
                    OutlinedTextField(
                        value = if (minuteError) "" else selectedMinute.toString(),
                        onValueChange = { value ->
                            val intValue = value.toIntOrNull()
                            selectedMinute = intValue ?: selectedMinute
                            minuteError = intValue == null || !isInputValid(selectedHour, selectedMinute)
                            showError.value = false
                        },
                        label = { Text("Minute") },
                        isError = minuteError,
                        singleLine = true,
                        modifier = Modifier.width(100.dp)
                    )
                }
                if(showError.value){
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "Cannot be less than current time",color = Color.Red)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = { onDismiss() }) {
                        Text("Cancel", color = MaterialTheme.colorScheme.error)
                    }
                    TextButton(
                        onClick = {
                            if (isInputValid(selectedHour, selectedMinute)) {
                                if(greaterThanCurrent(selectedHour, selectedMinute))
                                    onConfirm(selectedHour, selectedMinute)
                                else showError.value = true
                            }
                            else {
                                hourError = true
                                minuteError = true
                            }
                        },
                        enabled = isInputValid(selectedHour, selectedMinute)
                    ) {
                        Text("OK", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}


@Composable
@Preview(showBackground = true)
fun SimpleTimeInputDialogPreview() {
    MaterialTheme {
        SimpleTimeInputDialog(
            onConfirm = { hour, minute ->
                println("Selected time: $hour:$minute")
            },
            onDismiss = {
                println("Dialog dismissed")
            }
        )
    }
}

