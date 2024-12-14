package com.example.easycycle.presentation.ui

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.easycycle.R
import com.example.easycycle.data.Enum.Location
import com.example.easycycle.data.model.Cycle
import com.example.easycycle.data.model.allCycleDataState
import com.example.easycycle.presentation.viewmodel.CycleViewModel
import com.example.easycycle.presentation.viewmodel.SharedViewModel
import java.nio.file.WatchEvent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AllCyclesScreen(
    cycleViewModel: CycleViewModel,
    sharedViewModel: SharedViewModel,
    navController: NavController,
    onClick:(cycleId:String)->Unit
) {
    val allCycleDataState by cycleViewModel.getAllCycleDataState.collectAsState()

    // DisposableEffect to add/remove the Firebase listener
    DisposableEffect(Unit) {
        Log.d("allCyclesScreen", "Inside DisposableEffect")
        cycleViewModel.getAllCyclesAndAddListener(Location.NILGIRI)
        sharedViewModel.startLiveIcon()

        onDispose {
            Log.d("AllCyclesScreen","Inside onDispose")
            cycleViewModel.getAllCyclesRemoveListener(Location.NILGIRI)
            sharedViewModel.stopLiveIcon()
            //TODO decide if while navigating away do we need to clear allCycleDataSTate as in case of while Booking the cycle
        }
    }

    when {
        allCycleDataState.isLoading -> {
            Log.d("AllCyclesScreen","Loading")
            LoadingPage()
        }
        allCycleDataState.error -> {
            //ErrorScreen(message = allCycleDataState.errorMessage) // Replace with your ErrorScreen Composable
            Log.d("AllCyclesScreen","ErrorOccurred here${allCycleDataState.error}")
        }
        else -> {
            Log.d("AllCyclesScreen","Inside Else Block")
            val bookedCycles = allCycleDataState.list.filter { it.booked }.sortedBy { it.cycleStatus.estimatedNextAvailableTime }
            val availableCycles = allCycleDataState.list.filter { !it.booked }.sortedByDescending { it.underProcess }

            LazyColumn(modifier = Modifier.fillMaxSize().padding(top=20.dp)) {
                // Header for Available Cycles
                if (availableCycles.isNotEmpty()) {
                    item {
                        Text(
                            text = "Available Cycles",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    items(availableCycles) { cycle ->
                        CycleItem(cycle, sharedViewModel,navController)
                    }
                }

                // Header for Booked Cycles
                if (bookedCycles.isNotEmpty()) {
                    item {
                        Text(
                            text = "Booked Cycles",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    items(bookedCycles) { cycle ->
                        CycleItem(cycle, sharedViewModel,navController)
                    }
                }
            }
        }
    }
}

@Composable
fun CycleItem(cycle: Cycle, sharedViewModel: SharedViewModel,navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
            .border(
                width = 1.dp, // Thickness of the border
                color = when {
                    cycle.underProcess -> Color.Transparent // Yellow border for "Under Process"
                    cycle.booked -> Color.Transparent // Red border for "Booked"
                    else -> Color.LightGray // Green border for "Available"
                },
                shape = RoundedCornerShape(8.dp) // Rounded corners for the border
            )
            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        // Cycle Image

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(cycle.imageURL ?: R.drawable.defaultdycle)
                    .placeholder(R.drawable.defaultdycle)
                    .error(R.drawable.defaultdycle)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
            )


        Spacer(modifier = Modifier.width(16.dp))

        // Cycle Details
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Cycle ID: ${cycle.cycleId}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Location: ${cycle.location}", style = MaterialTheme.typography.bodySmall)

            if (cycle.underProcess) {
                Text(
                    text = "Status: Under Process",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Yellow
                )
                Text(
                    text = "Available in ~5 minutes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (cycle.booked) {
                val estimatedTime = cycle.cycleStatus.estimatedNextAvailableTime
                val formattedTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(estimatedTime))
                Text(
                    text = "Status: Booked",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Red
                )
                Text(
                    text = "Available by: $formattedTime",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "Status: Available",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Green
                )
            }
        }

        // Status Dot
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(
                    when {
                        cycle.underProcess -> Color.Yellow
                        cycle.booked -> Color.Red
                        else -> Color.Green
                    }
                )
        )
    }
}

