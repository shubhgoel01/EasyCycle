package com.example.easycycle.presentation.ui

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.easycycle.R
import com.example.easycycle.calculateTimeElapsed
import com.example.easycycle.data.Enum.ScheduleState
import com.example.easycycle.data.model.Schedule
import com.example.easycycle.data.model.ResultState
import com.example.easycycle.data.remote.Profile
import com.example.easycycle.formatTimestamp
import com.example.easycycle.presentation.navigation.navigateToEachScheduleDetailScreen
import com.example.easycycle.presentation.navigation.navigateToErrorScreen
import com.example.easycycle.presentation.ui.components.Component_tDialogBox
import com.example.easycycle.presentation.viewmodel.CycleViewModel
import com.example.easycycle.presentation.viewmodel.SharedViewModel
import com.example.easycycle.presentation.viewmodel.UserViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


//Fetch UserDetails and ScheduleDetails
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun homeScreen(
    navController: NavController,
    userViewModel: UserViewModel,
    sharedViewModel: SharedViewModel,
    cycleViewModel: CycleViewModel
) {
    Log.d("homeScreen","Screen Called")

    val userDataState = userViewModel.userDataState.collectAsState()
    val scheduleDataState = userViewModel.scheduleDataState.collectAsState()
    val profileDataState = sharedViewModel.profileDataState.collectAsState()

    val context = LocalContext.current

    LaunchedEffect(userDataState.value){
        when (val state = userDataState.value){
            is ResultState.Loading ->{
                //FETCH USER DETAILS
                if(state.isLoading) {
                    Log.d("Home","Fetching user details")
                    userViewModel.fetchUserDetails(sharedViewModel.currUser.value!!.uid, context)
                }
                else navigateToErrorScreen(navController,true,"home 2")
            }
            is ResultState.Success ->{
                //FETCH PROFILE DETAILS
                when(val state2 = profileDataState.value){
                    is ResultState.Loading ->{
                        if(state2.isLoading){
                            userViewModel.fetchStudentDetails(
                                context,
                                state.data!!.registrationNumber,
                                onComplete = {
                                    sharedViewModel.insertProfile((it as ResultState.Success).data!!)
                                    sharedViewModel.updateProfileDataState(it)
                                },
                                onError = {
                                    sharedViewModel.updateProfileDataState(it)
                                }
                            )
                        }
                    }
                    else ->{}
                }
                //FETCH SCHEDULE DETAILS
                when(val state2 = scheduleDataState.value){
                    is ResultState.Loading ->{
                        if(state2.isLoading && state.data!!.scheduleId!="") {
                            userViewModel.fetchSchedule(context, state.data.scheduleId)
                        }
                        else if(state2.isLoading)
                            userViewModel.updateScheduleDataState(ResultState.Loading(false))
                    }
                    else-> {}
                }
            }
            else -> {}
        }
    }

    LaunchedEffect(userDataState.value){
        when(val state = userDataState.value){
            is ResultState.Success ->{
                val userData = state.data!!
                if(userData.timerStartTime!=null && userData.timerStartTime!! +5*60*1000 > System.currentTimeMillis() && userData.scheduleId=="" && userData.cycleId!=""){
                    sharedViewModel.updateReservedCycleUid(userData.cycleId)
                    sharedViewModel.startTimer(userData.timerStartTime!! + 5 * 60 * 1000 - System.currentTimeMillis()){
                        cycleViewModel.updateReserveAvailableCycleState(ResultState.Success(userData.cycleId))
                    }
                }
            }
            else -> {}
        }
    }

    when(profileDataState.value){
        is ResultState.Success ->{
            val student = (sharedViewModel.profileDataState.value as ResultState.Success).data!!
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                ) {
                    Text(text = "Profile", fontWeight = FontWeight.Bold)
                    profile(student)
                    Spacer(modifier = Modifier.height(15.dp))
                    Text(text = "Schedules", fontWeight = FontWeight.Bold)

                    Schedules(scheduleDataState.value,navController, userViewModel)

                }
            }
        }
        is ResultState.Loading-> LoadingPage()
        is ResultState.Error -> navigateToErrorScreen(navController,true,"Home 1")
    }

}

@Composable
fun profile(student: Profile) {
    val imageUrl = student.imageURL.ifEmpty { "default" }
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
    scheduleResultState : ResultState<Schedule>,
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
            when(scheduleResultState) {
                is ResultState.Loading -> {
                    if (scheduleResultState.isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f)), // Semi-transparent overlay
                            contentAlignment = Alignment.Center
                        ) {
                            LoadingPage() // Loading indicator
                        }
                    } else {
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
                }
                is ResultState.Error ->{
                    errorPage(message = "Some Error Occurred")
                }

                is ResultState.Success->{
                    ScheduleScreen(scheduleResultState.data!!,userViewModel,navController)
                }
            }
        }
    }
}

@SuppressLint("SuspiciousIndentation")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ScheduleScreen(
    schedule: Schedule,
    userViewModel: UserViewModel,
    navController: NavController
) {

    var showDialog1 by remember { mutableStateOf(false) }
    if(showDialog1){
        Component_tDialogBox(
            heading = "Fare Information",
            body = "When the cycle is not returned according to the scheduled time, the ride is automatically extended, " +
                    "and the fare will be charged at a standard rate of \$2 per hour, with no additional penalty.",
            onDismissRequest = { showDialog1 = false }
        )
    }

    var elapsedTime by remember { mutableStateOf("") }
    DisposableEffect(schedule.startTime) {
        var job: Job? = null
        if(schedule.status.status == ScheduleState.OVERTIME)
        {
            elapsedTime = calculateTimeElapsed(schedule.startTime, schedule.estimateTime)
            job = CoroutineScope(Dispatchers.Main).launch {
                while (true) {
                    delay(60*1000)  // Update every minute
                    elapsedTime = calculateTimeElapsed(schedule.startTime, schedule.estimateTime)
                }
            }
        }
        onDispose {
            job?.cancel()  // Cancel the coroutine when leaving the screen
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = {
                navigateToEachScheduleDetailScreen(userViewModel,navController)
            }),
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
            val statusColor = when (schedule.status.status) {
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
                        text = when (schedule.status.status) {
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
                    if (schedule.status.status == ScheduleState.OVERTIME) {
                        IconButton(
                            onClick = {
                                showDialog1=true
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
                when (schedule.status.status) {
                    ScheduleState.BOOKED -> {
                        // Show Start Time for Booked Status
                        Text(
                            text = "Start Time: ${formatTimestamp(schedule.startTime)}",
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
                            text = "Time Elapsed: $elapsedTime",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Estimated Cost: ₹${userViewModel.extendedFare.value}",
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



