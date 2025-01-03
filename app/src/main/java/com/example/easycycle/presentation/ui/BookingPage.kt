package com.example.easycycle.presentation.ui

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.easycycle.calculateEstimatedCost
import com.example.easycycle.data.Enum.Location
import com.example.easycycle.data.model.Schedule
import com.example.easycycle.data.model.ResultState
import com.example.easycycle.presentation.navigation.navigateToAllCycleScreen
import com.example.easycycle.presentation.navigation.navigateToErrorScreen
import com.example.easycycle.presentation.navigation.navigateToHomeScreen
import com.example.easycycle.presentation.ui.components.AssistChipExample
import com.example.easycycle.presentation.ui.components.ComponentDropdown
import com.example.easycycle.presentation.ui.components.Component_Button
import com.example.easycycle.presentation.ui.components.Component_tDialogBox
import com.example.easycycle.presentation.ui.components.TimeInputDialog
import com.example.easycycle.presentation.ui.components.displayTime
import com.example.easycycle.presentation.viewmodel.CycleViewModel
import com.example.easycycle.presentation.viewmodel.SharedViewModel
import com.example.easycycle.presentation.viewmodel.UserViewModel
import java.util.Calendar


@SuppressLint("SuspiciousIndentation")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BookingScreen(rentNow: Boolean, rentLater: Boolean,sharedViewModel: SharedViewModel,cycleViewModel: CycleViewModel,userViewModel:UserViewModel,navController: NavController) {
    val context = LocalContext.current
    val reserveCycleState = cycleViewModel.reserveAvailableCycleState.collectAsState()

    LaunchedEffect(reserveCycleState.value){
        when (val state = reserveCycleState.value){
            is ResultState.Loading -> {
                if(state.isLoading && !sharedViewModel.showDialog1.value && !sharedViewModel.showDialog2.value){
                    cycleViewModel.reserveAvailableCycle(
                        context,
                        onCancel = { sharedViewModel.updateShowDialog2(true) },
                        onComplete = {
                            sharedViewModel.updateReservedCycleUid(it)
                            sharedViewModel.startTimer(5*60*1000)
                        })
                }
            }
            else -> {}
        }
    }

    val showDialog2 = sharedViewModel.showDialog2.collectAsState()   //Used when no cycle is available

    if (showDialog2.value) {
        Component_tDialogBox(
            heading = "No cycle available",
            body = "We are sorry but no cycles are available",
            onDismissRequest = {sharedViewModel.updateShowDialog2(false)},
            composable = { demo(userViewModel,cycleViewModel,sharedViewModel,navController) }
        )
    }


    val createSchedulesState = userViewModel.createScheduleState.collectAsState()

    LaunchedEffect(createSchedulesState.value){
        if(createSchedulesState.value is ResultState.Success)
            navigateToHomeScreen(navController,userViewModel,sharedViewModel)
        else if (createSchedulesState.value is ResultState.Error)
            navigateToErrorScreen(navController, message = "Booking Page 1")
    }

    LaunchedEffect(reserveCycleState.value){
        if(reserveCycleState.value is ResultState.Error)
            navigateToErrorScreen(navController, message = "Booking Page 2")
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Column(
            modifier = Modifier.padding(top=20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Book Cycle",
                style = MaterialTheme.typography.bodySmall,
                //color = MaterialTheme.colors.error
            )

            when(val state = reserveCycleState.value){
                is  ResultState.Loading ->{
                   if(!state.isLoading){
                       Text(
                           text = "No cycles available",
                           style = MaterialTheme.typography.bodySmall,
                           // now show dialog over here
                       )
                   }
                }
                is ResultState.Success ->{
                    Text(
                        text = "Cycle Reserved, Book Fast",
                        style = MaterialTheme.typography.bodySmall
                        //color = MaterialTheme.colors.onSurface
                    )
                }
                else ->{}
            }

            BookingPage(
                rentNow,
                rentLater,
                enabled = (reserveCycleState.value is ResultState.Success) && ((userViewModel.createScheduleState.value is ResultState.Loading) && !(userViewModel.createScheduleState.value as ResultState.Loading).isLoading),
                onBook = {startLocation,endLocation,StartTime,estimateScheduleTime->
                    userViewModel.updateCreateScheduleState(ResultState.Loading(true))
                    if(sharedViewModel.currUser.value!=null)
                        userViewModel.createSchedule(
                            context,
                            Schedule(
                            userUid = sharedViewModel.currUser.value!!.uid,
                            cycleUid = sharedViewModel.reservedCycleUid.value!!,
                            estimateTime = estimateScheduleTime,
                            startTime = StartTime,
                            //TODO Handle Locations
                        )){
                            userViewModel.updateCreateScheduleState(it)
                        }
                }
            )
        }
    }
}
@Composable
fun BookingPage(rentNow: Boolean,
                rentLater: Boolean,
                enabled:Boolean,
                onBook:(String,String,Long,Long)->Unit
) {
    val context = LocalContext.current
    // Pickup Location
    var selectedOptionLocation by remember { mutableStateOf(Location.NILGIRI.toString()) }
    val inputChangeLocation: (String) -> Unit = { value -> selectedOptionLocation = value }
    val listLocation = listOf(Location.NILGIRI.toString())
    var expandedLocation by remember { mutableStateOf(false) }
    val onExpandChangeLocation: () -> Unit = { expandedLocation = !expandedLocation }

    // Drop-off Location
    var selectedOptionLocation2 by remember { mutableStateOf(Location.NILGIRI.toString()) }
    val inputChangeLocation2: (String) -> Unit = { value -> selectedOptionLocation2 = value }
    var expandedLocation2 by remember { mutableStateOf(false) }
    val onExpandChangeLocation2: () -> Unit = { expandedLocation2 = !expandedLocation2 }

    var showTimeInputDialog1 by remember { mutableStateOf(false) }
    var hour1 by remember { mutableStateOf<Int?>(null) }
    var minute1 by remember { mutableStateOf<Int?>(null) }
    val onConfirmTimePicker1 :(hour:Int,minute:Int)->Unit = {h,m->
        showTimeInputDialog1 = false
        hour1 = h
        minute1 = m
    }
    val onDismissTimeInputDialog1 = {
        showTimeInputDialog1 = false
    }
    if(showTimeInputDialog1){
        TimeInputDialog(hour1,minute1,onConfirmTimePicker1,onDismissTimeInputDialog1,true)
    }

    var showTimeInputDialog2 by remember { mutableStateOf(false) }
    var hour2 by remember { mutableStateOf<Int?>(null) }
    var minute2 by remember { mutableStateOf<Int?>(null) }
    val onConfirmTimePicker2 :(hour:Int,minute:Int)->Unit = {h,m->
        showTimeInputDialog2 = false
        hour2 = h
        minute2 = m
    }
    val onDismissTimeInputDialog2 = {
        showTimeInputDialog2 = false
    }
    if(showTimeInputDialog2){
        TimeInputDialog(
            if(hour2==null) 0 else hour2,
            if(minute2==null) 0 else minute2,
            onConfirmTimePicker2,
            onDismissTimeInputDialog2,
            false)
    }

    var estimateCost by remember { mutableStateOf<Int?>(null) }
    if(hour2!=null && minute2!=null){
        estimateCost = calculateEstimatedCost((hour2!! *60+ minute2!!).toLong()*60*100).toInt()
    }


    val onButtonClick: () -> Unit = {
        Log.d("Button", "Pressed")
        if (hour1 == null || minute1 == null) {
            Toast.makeText(context, "Select Start Time", Toast.LENGTH_SHORT).show()
        } else if (hour2 == null || minute2 == null) {
            Toast.makeText(context, "Select Estimate Schedule Time", Toast.LENGTH_SHORT).show()
        } else {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour1!!)  // Set the start time hour
                set(Calendar.MINUTE, minute1!!)     // Set the start time minute
                set(Calendar.SECOND, 0)             // Reset seconds to 0
                set(Calendar.MILLISECOND, 0)        // Reset milliseconds to 0
            }

            val st = calendar.timeInMillis  // This is the start time for today in milliseconds
            val et = (hour2!! * 60 + minute2!!).toLong() * 60 * 1000

            Toast.makeText(context, "Processing Your Request", Toast.LENGTH_SHORT).show()
            onBook(selectedOptionLocation, selectedOptionLocation2, st, et)
        }
    }


    Box {
        Surface(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(vertical = 20.dp) // Center the Surface with reduced width
                .wrapContentHeight()
                .padding(start = 10.dp, end = 10.dp)
                .fillMaxWidth(),
            tonalElevation = 6.dp,
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                //verticalArrangement = Arrangement.spacedBy(16.dp)
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
                Spacer(modifier = Modifier.height(15.dp))

                // Pickup Location
                Text(
                    text = "Pickup Location",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(7.dp))
                ComponentDropdown(
                    selectedOption = selectedOptionLocation,
                    list = listLocation,
                    onOptionChange = inputChangeLocation,
                    onExpandedChange = onExpandChangeLocation,
                    expanded = expandedLocation,
                    label = "Select Location",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(7.dp))

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
                Spacer(modifier = Modifier.height(7.dp))
                // Drop-off Location
                Text(
                    text = "Drop-off Location",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(7.dp))
                ComponentDropdown(
                    selectedOption = selectedOptionLocation2,
                    list = listLocation,
                    onOptionChange = inputChangeLocation2,
                    onExpandedChange = onExpandChangeLocation2,
                    expanded = expandedLocation2,
                    label = "Select Location",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(15.dp))
                // Start Time
                Text(
                    text = "Start Time",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                //Spacer(modifier = Modifier.height(7.dp))
                //Time Displayer
                displayTime(hour1,minute1){
                    showTimeInputDialog1 = true
                }
                Spacer(modifier = Modifier.height(15.dp))

                Text(
                    text = "Estimate schedule Time",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                //Spacer(modifier = Modifier.height(7.dp))
                displayTime(hour2,minute2){
                    showTimeInputDialog2 = true
                }
                Spacer(modifier = Modifier.height(15.dp))
                Row {
                    Text(
                        text = "Estimate Fare : ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if(estimateCost!=null) "$$estimateCost" else "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Green
                    )
                }
                Spacer(modifier = Modifier.height(7.dp))

                // Book Now Button
                Component_Button(
                    title = "Pay and Book Now",
                    onClick = onButtonClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    enabled = enabled
                )
            }
        }
    }
}


//Composable that is passed to showDialog
@Composable
fun demo(userViewModel: UserViewModel,cycleViewModel: CycleViewModel,sharedViewModel: SharedViewModel, navController: NavController) {
    Box(
        modifier = Modifier
            .padding(16.dp) // Added padding for the outer box
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start // Align text to start
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "View All Cycles",
                    color = Color.Blue,
                    style = MaterialTheme.typography.bodySmall.copy(
                        textDecoration = TextDecoration.Underline
                    ), // Updated font size
                    modifier = Modifier.clickable {
                        sharedViewModel.updateShowDialog2(false)
                        navigateToAllCycleScreen(cycleViewModel,navController)
                    }
                )
                Text(
                    text = " to check their availability.",
                    color = Color.Black,
                    style = MaterialTheme.typography.bodySmall // Updated font size
                )
            }
            Spacer(modifier = Modifier.height(8.dp)) // Added spacing between rows
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Go to",
                    color = Color.Black,
                    style = MaterialTheme.typography.bodySmall // Updated font size
                )
                Text(
                    text = " Home",
                    color = Color.Blue,
                    style = MaterialTheme.typography.bodySmall.copy(
                        textDecoration = TextDecoration.Underline
                    ),
                    modifier = Modifier.clickable {
                        sharedViewModel.updateShowDialog2(false)
                        navigateToHomeScreen(navController, userViewModel,sharedViewModel)
                    }
                )
                Text(
                    text = " screen.",
                    color = Color.Black,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap anywhere outside this box to stay on this screen.",
                color = Color.Black,
                style = MaterialTheme.typography.bodySmall, // Updated font size
                modifier = Modifier.padding(top = 8.dp) // Added padding for separation
            )
        }
    }
}
