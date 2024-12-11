package com.example.easycycle.presentation.ui

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.easycycle.data.model.Student
import com.example.easycycle.R
import com.example.easycycle.data.Enum.ScheduleState
import com.example.easycycle.data.model.Schedule
import com.example.easycycle.data.model.SchedulesDataState
import com.example.easycycle.presentation.viewmodel.SharedViewModel
import com.example.easycycle.presentation.viewmodel.UserViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.ceil

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun homeScreen(
    student : Student,
    navController: NavController,
    userViewModel: UserViewModel,
    sharedViewModel: SharedViewModel
) {
    val user = sharedViewModel.currUser.collectAsState()

    val schedulesDataState = userViewModel.scheduleDataState.collectAsState()

    Log.d("Home","Entered Home Screen")
    val imageUrl = student.imageURL.ifEmpty { "default" }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            Text(text = "Profile", fontWeight = FontWeight.Bold)
            profile(student,imageUrl)
            Spacer(modifier = Modifier.height(15.dp))
            Text(text = "Schedules", fontWeight = FontWeight.Bold)

            Schedules(schedulesDataState.value , navController, userViewModel)
        }
    }
}

@Composable
fun profile(student: Student,imageUrl:String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(10.dp),
        tonalElevation = 4.dp,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(10.dp)) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(if (imageUrl == "default") R.drawable.person else imageUrl)
                    .placeholder(R.drawable.person)
                    .error(R.drawable.person)
                    .build(),
                contentDescription = "Student Image",
                modifier = Modifier
                    .size(100.dp)
                    .padding(end = 10.dp)
            )

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = student.name,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(bottom = 5.dp)
                )
                Text(
                    text = student.registrationNumber,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(bottom = 5.dp)
                )
                Text(text = student.branch, fontSize = 20.sp)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Schedules(
    schedulesDataState : SchedulesDataState,
    navController: NavController,
    userViewModel: UserViewModel
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            //.weight(1f) // Adjust weight if needed
            .padding(10.dp),
        //tonalElevation = 4.dp,
        shape = RoundedCornerShape(24.dp)
    ){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ){
            when {
                schedulesDataState.isLoading -> {
                    LoadingPage()
                }

                schedulesDataState.error -> {
                    error(schedulesDataState.errorMessage)
                }
                schedulesDataState.schedule == null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(50.dp),
                        //verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "No Schedules",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "NO SCHEDULES FOR THE DAY",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                else->{
                    ScheduleScreen(schedulesDataState.schedule!!,userViewModel,navController)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ScheduleScreen(
    schedule: Schedule,
    userViewModel: UserViewModel,
    navController: NavController
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                // TODO
                navController.currentBackStackEntry?.savedStateHandle?.set("schedule", schedule)
                navController.navigate("schedule_detail")
            },
        tonalElevation = 4.dp,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status color indicator
            val statusColor = when (schedule.Status.status) {
                ScheduleState.ONGOING -> Color.Green
                ScheduleState.BOOKED -> Color.Blue
                ScheduleState.OVERTIME -> Color.Red
                else -> Color.Black
            }

            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                // Display the status as title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = when (schedule.Status.status) {
                            ScheduleState.BOOKED -> "Booked Schedule"
                            ScheduleState.ONGOING -> "In Progress"
                            ScheduleState.OVERTIME -> "Extended Time"
                            ScheduleState.COMPLETED -> "Completed Schedule"
                            ScheduleState.CANCELLED -> "Cancelled Schedule"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color=statusColor,
                        modifier = Modifier.weight(1f) // Push IconButton to the end
                    )
                    if (schedule.Status.status == ScheduleState.OVERTIME) {
                        IconButton(
                            onClick = {
                                // TODO
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Info",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp)) // Add spacing

                Divider(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Display information based on status
                when (schedule.Status.status) {
                    ScheduleState.BOOKED -> {
                        // Show Start Time for Booked Status
                        Text(
                            text = "Start Time: ${
                                java.time.Instant.ofEpochMilli(schedule.startTime)
                                    .atZone(java.time.ZoneId.systemDefault())
                                    .toLocalDateTime()
                                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                            }",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    ScheduleState.OVERTIME -> {
                        // Show Start Time and an info icon for Overtime
                        Text(
                            text = "Enjoy your extended ride! No penalties apply.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Time Elapsed: ${calculateTimeElapsed(schedule.startTime,schedule.estimateTime)}", // Function to calculate elapsed time
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Estimated Cost: ₹${userViewModel.extendedFare}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    ScheduleState.ONGOING -> {
                        // Display different information for Ongoing Status
                        Text(
                            text = "Estimated End Time: ${
                                java.time.Instant.ofEpochMilli(schedule.startTime + schedule.estimateTime)
                                    .atZone(java.time.ZoneId.systemDefault())
                                    .toLocalDateTime()
                                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                            }",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    else -> {
                        // Optional: Handle other statuses if needed
                    }
                }
            }
        }
    }
}

fun calculateTimeElapsed(startTime: Long, estimatedTime: Long): String {
    // Calculate the actual elapsed time beyond the estimated time
    val elapsedMillis = System.currentTimeMillis() - (startTime + estimatedTime)
    val minutes = elapsedMillis / (1000 * 60)
    return "$minutes minutes" // Return the elapsed time in minutes
}

fun calculateEstimatedCost(startTime: Long, estimatedTime: Long): Long {
    // Calculate the actual elapsed time beyond the estimated time in milliseconds
    val elapsedMillis = System.currentTimeMillis() - (startTime + estimatedTime)

    // Convert elapsed time to minutes
    val elapsedMinutes = elapsedMillis / (1000 * 60)

    // If there is no extra time, the cost is 0
    if (elapsedMinutes <= 0) return 0

    // Calculate the upper limit (round up to the next hour)
    val upperLimitInHours = ceil(elapsedMinutes / 60.0).toInt()

    // Calculate the cost (2 rupees per hour)
    val cost = upperLimitInHours * 2

    return cost.toLong()
}


@RequiresApi(Build.VERSION_CODES.O)
fun formatTimestamp(timestamp: Long): String {
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")
    return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).format(formatter)
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun eachScheduleDetailScreen(schedule: Schedule,
                             //userViewModel: UserViewModel,
                             navController: NavController
) {
    Log.d("eachScheduleDetailScreen", "Calling function")

    var deleteScheduleStatus by remember { mutableStateOf(false) }

    val imageUrl = "" // Replace with actual cycle image URL
    val cycleId = "1" // Replace with actual cycle ID from your data model
    val location = "NILGIRI" // Replace with actual location
    val totalFare= "1"

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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp) // Adds spacing between items
        ) {
            // Display Cycle Image
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl) // Placeholder image
                    .placeholder(R.drawable.defaultdycle) // Placeholder image
                    .error(R.drawable.defaultdycle) // Error image
                    .build(),
                contentDescription = "Cycle Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp)) // Rounds the corners of the image
            )

            // Display Cycle ID
            Text(
                text = "Cycle ID: $cycleId",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // Display Location

            Text(
                text = "Start Time: ${schedule.startTime}",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Extra Time Stamp : ",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Total Fare: $${totalFare}", // Placeholder for total fare
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // Conditional Display for Status
            when (schedule.Status.status) {
                ScheduleState.ONGOING -> {
                    Text(
                        text = "In Progress",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.Green
                    )
                }
                ScheduleState.OVERTIME -> {
                    Text(
                        text = "Overtime: Extra time added. No penalty.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.Red
                    )
                    IconButton(
                        onClick = {
                            // Navigate to overtime info

                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                else -> {

                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cancel/Delete Schedule Button
            Button(
                onClick = {
                    deleteScheduleStatus = true
                    // Handle schedule cancellation or deletion
                    // TODO: Implement delete schedule or return cycle functionality
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = when (schedule.Status.status) {
                        ScheduleState.BOOKED -> "Cancel Schedule"
                        else -> "Return Cycle"
                    }
                )
            }
        }
    }
}
