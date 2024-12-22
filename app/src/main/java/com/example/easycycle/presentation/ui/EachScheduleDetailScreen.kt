package com.example.easycycle.presentation.ui

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.easycycle.R
import com.example.easycycle.calculateEstimatedCost
import com.example.easycycle.calculateTimeElapsed
import com.example.easycycle.data.Enum.ScheduleState
import com.example.easycycle.data.model.BeforeTime
import com.example.easycycle.data.model.Delay
import com.example.easycycle.data.model.Schedule
import com.example.easycycle.formatTimestamp
import com.example.easycycle.presentation.viewmodel.UserViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun eachScheduleDetailScreen(
    schedule: Schedule, //Not needed remove this later
    userViewModel: UserViewModel,
    navController: NavController
) {
    Log.d("eachScheduleDetailScreen", "Calling function")

    val schedulesDataState = userViewModel.scheduleDataState.collectAsState()
    LaunchedEffect(schedulesDataState.value){
        //Do Nothing
    }

    val schedule = schedulesDataState.value.schedule
    if(schedule == null){
        //Navigate to error screen showing that error occurred

    }


    val imageUrl = "" // Replace with actual cycle image URL
    val cycleId = schedule!!.cycleUid// Replace with actual cycle ID from your data model
    val location = schedule.startDestination // Replace with actual location
    val totalFare = "1"

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        tonalElevation = 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(16.dp) // Adds consistent spacing
        ) {
            // Cycle Image
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .placeholder(R.drawable.defaultdycle)
                    .error(R.drawable.defaultdycle)
                    .build(),
                contentDescription = "Cycle Image",
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .align(Alignment.CenterHorizontally)
            )

            // Cycle Details (Aligned in Two Columns)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp) // Spacing between rows
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Cycle ID:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f) // Ensures consistent width
                    )
                    Text(
                        text = cycleId,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Location:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = location.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Start Time:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = formatTimestamp(schedule!!.startTime),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Estimated Return:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = formatTimestamp(schedule!!.startTime + schedule!!.estimateTime),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                }

                if(schedule.status.status.equals(ScheduleState.OVERTIME)){

                    //Extra elapsed time section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Extra Time Elapsed:",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = calculateTimeElapsed(schedule!!.startTime, schedule.estimateTime),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.End
                        )
                    }

                    // Fare and Info Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left Section: Extra Fare with Info Button
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Extra Fare:",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            // Show Info Icon only when status is OVERTIME
                            if (schedule.status.status == ScheduleState.OVERTIME) {
                                IconButton(
                                    onClick = {
                                        //showBottomSheet1 = true
                                    },
                                    modifier = Modifier.size(24.dp) // Smaller button for better alignment
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Info",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        // Right Section: Total Fare
                        Text(
                            text = "$${totalFare}",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.End
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = when (schedule.status.status) {
                    ScheduleState.ONGOING -> "Status: In Progress"
                    ScheduleState.OVERTIME -> "Status: Overtime. Extra time added. No extra penalty."
                    else -> "Status: Booked"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = when (schedule.status.status) {
                    ScheduleState.ONGOING -> Color.Green
                    ScheduleState.OVERTIME -> Color.Red
                    else -> MaterialTheme.colorScheme.onBackground
                },
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Cancel/Delete Schedule Button
            Button(
                onClick = {

                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = when (schedule.status.status) {
                        ScheduleState.BOOKED -> "Cancel Schedule"
                        else -> "Return Cycle"
                    }
                )
            }
        }
    }
}

fun onClick(schedule:Schedule){
    if(schedule.status.status == ScheduleState.OVERTIME) {
        schedule.status.delay = Delay(
            isDelay = true,
            penalty = calculateEstimatedCost(System.currentTimeMillis() - (schedule.startTime + schedule.estimateTime)),
        )
        schedule.status.status = ScheduleState.COMPLETED
    }
    else if(schedule.status.status == ScheduleState.ONGOING) {
        schedule.status.beforeTime = BeforeTime(
            remainingTime = schedule.startTime+schedule.estimateTime-System.currentTimeMillis()
        )
        schedule.status.status = ScheduleState.COMPLETED
    }
    else if(schedule.status.status == ScheduleState.BOOKED){
        schedule.status.status = ScheduleState.CANCELLED
    }

    schedule.status.statusTime = System.currentTimeMillis()

    //First Update user
    //Update cycleField Booked = false according to me no need to update other fields like, scheduleUid, nextAvailableTime in cycle as these will be used only if the cycle is booked
    //Once this is done Remove the scheduleListener and set schedule to null and move to home screen, and set the worker
    //In worker - Create a new node in ScheduleHistory, update cycleBookingHistory, deleteSchedule from Schedule Node, update user BookingHistory


}