package com.example.easycycle.presentation.ui

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import com.example.easycycle.data.model.ResultState
import com.example.easycycle.data.model.Schedule
import com.example.easycycle.formatTimestamp
import com.example.easycycle.presentation.navigation.navigateToErrorScreen
import com.example.easycycle.presentation.navigation.navigateToHomeScreen
import com.example.easycycle.presentation.viewmodel.SharedViewModel
import com.example.easycycle.presentation.viewmodel.UserViewModel


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun eachScheduleDetailScreen(
    userViewModel: UserViewModel,
    navController: NavController,
    sharedViewModel : SharedViewModel
) {
    Log.d("eachScheduleDetailScreen", "Calling function")

    val schedulesDataState = userViewModel.scheduleDataState.collectAsState()
    val returnOrCancelSchedule = userViewModel.returnOrCancelSchedule.collectAsState()

    if (returnOrCancelSchedule.value is ResultState.Success) {
        LaunchedEffect(Unit) {
            navigateToHomeScreen(navController,userViewModel, sharedViewModel)
        }
        return
    }

    lateinit var schedule : Schedule

    var enabled by remember { mutableStateOf(false) }
    //showLoading = returnOrCancelRide.value is ResultState.Loading && ResultState.Loading

    when(val state = returnOrCancelSchedule.value){
        is ResultState.Loading ->{
            if(state.isLoading){
                enabled = false
                val updatedSchedule = updateScheduleStatus(schedule)
                userViewModel.returnOrCancelRide(updatedSchedule)
                LoadingPage()
            }
            else enabled = true
        }
        else -> {}
    }

    when(val state = schedulesDataState.value){
        is ResultState.Loading -> {
            if(state.isLoading){
                LoadingPage()
            }
            else {
                navigateToErrorScreen(navController,true,"EachScheduleDetailScreen 1")
            }
        }
        is ResultState.Error -> {
            navigateToErrorScreen(navController,false,"EachScheduleDetailScreen 1")
            //TODO move to error screen/Toast or move to home screen
        }
        is ResultState.Success -> {
            schedule = state.data!!
            ScheduleDetailUI(
                schedule = schedule,
                onActionClick = { userViewModel.updateReturnOrCancelSchedule(ResultState.Loading(true)) },
                enabled
            )
        }
    }
}

private fun updateScheduleStatus(schedule: Schedule): Schedule {
    return schedule.copy(
        status = when (schedule.status.status) {
            ScheduleState.OVERTIME -> schedule.status.copy(
                delay = Delay(
                    isDelay = true,
                    penalty = calculateEstimatedCost(System.currentTimeMillis() - (schedule.startTime + schedule.estimateTime))
                ),
                status = ScheduleState.COMPLETED,
                statusTime = System.currentTimeMillis()
            )
            ScheduleState.ONGOING -> schedule.status.copy(
                beforeTime = BeforeTime(
                    remainingTime = schedule.startTime + schedule.estimateTime - System.currentTimeMillis()
                ),
                status = ScheduleState.COMPLETED,
                statusTime = System.currentTimeMillis()
            )
            ScheduleState.BOOKED -> schedule.status.copy(
                status = ScheduleState.CANCELLED,
                statusTime = System.currentTimeMillis()
            )
            else -> schedule.status // No changes for other states
        }
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ScheduleDetailUI(
    schedule: Schedule,
    onActionClick: () -> Unit,
    enabled: Boolean
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            tonalElevation = 4.dp,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data("") // Replace with actual image URL
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

                ScheduleDetails(schedule)

                Button(
                    onClick = {
                        Log.d("EachScheduleDetailScreen","Button Clicked")
                        onActionClick()
                    },
                    enabled = enabled,
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
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ScheduleDetails(schedule: Schedule) {
    val details = mutableListOf(
        "Cycle ID" to schedule.cycleUid,
        "Location" to schedule.startDestination.toString(),
        "Start Time" to formatTimestamp(schedule.startTime),
        "Estimated Return" to formatTimestamp(schedule.startTime + schedule.estimateTime)
    )

    if(schedule.status.status == ScheduleState.OVERTIME){
        details.add("Extra Time Elapsed:" to calculateTimeElapsed(schedule.startTime, schedule.estimateTime))
        details.add("Extra Fare:" to "1")
    }

    details.forEach { (label, value) ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "$label:", style = MaterialTheme.typography.bodyMedium)
            Text(text = value, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.End)
        }
    }
}
