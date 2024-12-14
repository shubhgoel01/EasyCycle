package com.example.easycycle.presentation.ui

import android.util.Log
import android.widget.Toast
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
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.easycycle.calculateEstimatedCost
import com.example.easycycle.data.Enum.Location
import com.example.easycycle.data.model.Cycle
import com.example.easycycle.data.model.bookCycle
import com.example.easycycle.presentation.navigation.Routes
import com.example.easycycle.presentation.ui.components.AssistChipExample
import com.example.easycycle.presentation.ui.components.ComponentDropdown
import com.example.easycycle.presentation.ui.components.Component_Button
import com.example.easycycle.presentation.ui.components.Component_tDialogBox
import com.example.easycycle.presentation.ui.components.TimeInputDialog
import com.example.easycycle.presentation.ui.components.displayTime
import com.example.easycycle.presentation.viewmodel.CycleViewModel
import com.example.easycycle.presentation.viewmodel.SharedViewModel


@Composable
fun BookingScreen(rentNow: Boolean, rentLater: Boolean,sharedViewModel: SharedViewModel,cycleViewModel: CycleViewModel,snackbarHostState: SnackbarHostState,navController: NavController) {
    val context = LocalContext.current

    DisposableEffect(Unit){
        onDispose {
            Log.d("BookingScreen","Inside DisposableEffect")
            if(!cycleViewModel.reserveAvailableCycleState.value.isLoading && cycleViewModel.reserveAvailableCycleState.value.cycle==null){
                cycleViewModel.updateReserveAvailableCycleState(bookCycle())
            //If no cycle is reserved then on screen change clear the reserveAvailableScreen, otherwise wrong information may be
            // displayed when user again visits the screen
            }
        }
    }

    LaunchedEffect(Unit) {
        if(sharedViewModel.reservedCycleUid.value==null)
            cycleViewModel.reserveAvailableCycle()
        else{
            Toast.makeText(context,"Continuing Booking",Toast.LENGTH_SHORT).show()
            cycleViewModel.updateReserveAvailableCycleState(
                bookCycle(
                    isLoading = false,
                    error = false,
                    cycle = Cycle(cycleId = sharedViewModel.reservedCycleUid.value!!)
                ))
        }
    }

    val reserveCycleState = cycleViewModel.reserveAvailableCycleState.collectAsState()

    LaunchedEffect(reserveCycleState.value ){
        Log.d("Dialog","Inside LaunchEffect")
        if(!reserveCycleState.value.isLoading && reserveCycleState.value.cycle==null && !sharedViewModel.showDialog1.value && !sharedViewModel.showDialog2.value)
            sharedViewModel.updateShowDialog2(true)
    }

    val showDialog1 = sharedViewModel.showDialog1.collectAsState()   //Used when timer is expired
    val showDialog2 = sharedViewModel.showDialog2.collectAsState()   //Used when no cycle is available
    if(showDialog1.value)
    {
        val temp = "The timer has expired. Please return to the previous screen or home screen to restart the booking process."
        Component_tDialogBox(
            heading = "Timer Ran-Out",
            body = temp,
            onDismissRequest = {sharedViewModel.updateShowDialog1(false)}
        )
    }


    if (showDialog2.value) {
        Component_tDialogBox(
            heading = "No cycle available",
            body = "We are sorry but no cycles are available",
            onDismissRequest = {sharedViewModel.updateShowDialog2(false)},
            composable = { demo(sharedViewModel,navController) }
        )
    }

    LaunchedEffect(reserveCycleState.value){
        Log.d("BookingScreen","Inside Launched Effect")
        //CHECK IF HERE IT IS REQUIRED TO CHECK IF "reserveCycleState.value.cycle!=null"
        if(!reserveCycleState.value.isLoading && reserveCycleState.value.cycle!=null && !sharedViewModel.isTimerRunning.value && !sharedViewModel.showDialog1.value && !sharedViewModel.showDialog2.value) {
            sharedViewModel.updateReservedCycleUid(cycleViewModel.reserveAvailableCycleState.value.cycle!!.cycleId)
            Log.d("BookingPage","Calling Start Timer")
            sharedViewModel.startTimer(5 * 60 * 1000){
                cycleViewModel.updateReserveAvailableCycleState(bookCycle(
                    isLoading = false,
                    cycle = null
                ))
            }
        }
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
            // Title and Cycle Status
            if (reserveCycleState.value.isLoading) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                ) {
                    LoadingPage(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .fillMaxSize()
                    )
                }
            } else if (reserveCycleState.value.errorMessage.isNotEmpty()) {     //CREATE A SEPARATE IF CONDITION TO CHECK IF ERROR OCCURS
                Text(
                    text = "No cycles available",
                    style = MaterialTheme.typography.bodySmall,
                    //color = MaterialTheme.colors.error
                )
                //sharedViewModel.showDialog2.value = true   Moved this to launch effect, otherwise the dialog was not hiding
            } else {
                // Displaying available cycle time (as an example, this could be dynamic)
                Text(
                    text = "Cycle Reserved, Book Fast",
                    style = MaterialTheme.typography.bodySmall
                    //color = MaterialTheme.colors.onSurface
                )
            }
            BookingPage(rentNow,rentLater,snackbarHostState){startLocation,endLocation,StartTime,estimateSchedule->

            }
        }
    }
}
@Composable
fun BookingPage(rentNow: Boolean, rentLater: Boolean,snackbarHostState: SnackbarHostState,onBook:(String,String,Long,Long)->Unit) {

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
        Log.d("Button","Pressed")
        if(hour1==null || minute1==null)
        {

        }
        else if(hour2==null || minute2==null){

        }
        else{
            val st = (hour1!! *60+ minute1!!).toLong()*60*100
            val et = (hour2!! *60+ minute2!!).toLong()*60*100
            onBook(selectedOptionLocation,selectedOptionLocation2,st,et)
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
                        text = if(estimateCost!=null) "$$estimateCost".toString() else "",
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
                        .padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun demo(sharedViewModel: SharedViewModel, navController: NavController) {
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
                        navController.navigate(Routes.AllCycleScreen.route)
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
                        navController.navigate(Routes.UserHome.route)
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

/*
@Composable
fun BookingScreen(rentNow: Boolean, rentLater: Boolean, userViewModel: UserViewModel, cycleViewModel: CycleViewModel) {
    LaunchedEffect(Unit) {
        cycleViewModel.reserveAvailableCycle()
    }
    val reserveCycleState = cycleViewModel.reserveAvailableCycleState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text(text = "Book Cycle", style = MaterialTheme.typography.headlineSmall)
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (reserveCycleState.value.isLoading) {
                Box(modifier = Modifier.fillMaxSize()) {
                    LoadingPage(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                    )
                }
            } else if (reserveCycleState.value.errorMessage != null) {
                Text(
                    text = "Error: ${reserveCycleState.value.errorMessage}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                Text(text = "5:00", style = MaterialTheme.typography.headlineMedium)
            }
            BookingPage(rentNow,rentLater)
        }
    }
}

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

    var showTimeInputDialog1 by remember { mutableStateOf(false) }
    var hour1 by remember { mutableStateOf<Int?>(null) }
    var minute1 by remember { mutableStateOf<Int?>(null) }
    val onConfirmTimePicker1: (hour: Int, minute: Int) -> Unit = { h, m ->
        showTimeInputDialog1 = false
        hour1 = h
        minute1 = m
    }
    val onDismissTimeInputDialog1 = {
        showTimeInputDialog1 = false
    }
    if (showTimeInputDialog1) {
        TimeInputDialog(hour1, minute1, onConfirmTimePicker1, onDismissTimeInputDialog1, true)
    }

    var showTimeInputDialog2 by remember { mutableStateOf(false) }
    var hour2 by remember { mutableStateOf<Int?>(null) }
    var minute2 by remember { mutableStateOf<Int?>(null) }
    val onConfirmTimePicker2: (hour: Int, minute: Int) -> Unit = { h, m ->
        showTimeInputDialog2 = false
        hour2 = h
        minute2 = m
    }
    val onDismissTimeInputDialog2 = {
        showTimeInputDialog2 = false
    }
    if (showTimeInputDialog2) {
        TimeInputDialog(
            if (hour2 == null) 0 else hour2,
            if (minute2 == null) 0 else minute2,
            onConfirmTimePicker2,
            onDismissTimeInputDialog2,
            false
        )
    }

    var estimateCost by remember { mutableStateOf<Int?>(null) }
    if (hour2 != null && minute2 != null) {
        estimateCost = calculateEstimatedCost((hour2!! * 60 + minute2!!).toLong() * 60 * 100).toInt()
    }

    val onButtonClick: () -> Unit = {
        // TODO
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
                displayTime(hour1, minute1) {
                    showTimeInputDialog1 = true
                }

                Text(
                    text = "Estimate Schedule Time",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                displayTime(hour2, minute2) {
                    showTimeInputDialog2 = true
                }

                Row {
                    Text(
                        text = "Estimate Fare : ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (estimateCost != null) "$${estimateCost}" else "",
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

 */





