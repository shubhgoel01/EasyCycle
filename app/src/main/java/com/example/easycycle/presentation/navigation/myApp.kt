package com.example.easycycle.presentation.navigation

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.easycycle.data.Enum.ScheduleState
import com.example.easycycle.data.model.Schedule
import com.example.easycycle.data.model.ScheduleStatus
import com.example.easycycle.data.model.FetchSchedulesDataState
import com.example.easycycle.data.model.StudentDataState
import com.example.easycycle.data.model.bookCycle
import com.example.easycycle.presentation.ui.AllCyclesScreen
import com.example.easycycle.presentation.ui.BookingScreen
import com.example.easycycle.presentation.ui.LoadingPage
import com.example.easycycle.presentation.ui.SignInScreen
import com.example.easycycle.presentation.ui.components.BookingFAB
import com.example.easycycle.presentation.ui.components.Component_tDialogBox
import com.example.easycycle.presentation.ui.components.ViewTopAppBar
import com.example.easycycle.presentation.ui.components.drawerContent
import com.example.easycycle.presentation.ui.eachScheduleDetailScreen
import com.example.easycycle.presentation.ui.errorPage
import com.example.easycycle.presentation.ui.homeScreen
import com.example.easycycle.presentation.viewmodel.AdminViewModel
import com.example.easycycle.presentation.viewmodel.CycleViewModel
import com.example.easycycle.presentation.viewmodel.SharedViewModel
import com.example.easycycle.presentation.viewmodel.UserViewModel
import com.google.gson.Gson

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun myApp(
    userViewModel: UserViewModel = hiltViewModel(),
    adminViewModel: AdminViewModel = hiltViewModel(),
    sharedViewModel: SharedViewModel = hiltViewModel(),
    cycleViewModel: CycleViewModel = hiltViewModel()
) {

    //AllCyclesScreen(cycleViewModel,sharedViewModel)
    //BookingScreen(true,false,userViewModel,cycleViewModel)
    //cycleViewModel.addCycle(Cycle(cycleId = "1", booked = false))
    //sharedViewModel.signOut()

    val scaffoldState= rememberScaffoldState()
    val scope= rememberCoroutineScope()


    //To define NavController for navigation
    val navController = rememberNavController()
    // Observe the current back stack entry
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route    //can be used to determine on which screen currently user is, used to show screen specific data/dialog/message

    val snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }

    val user = sharedViewModel.currUser.collectAsState()
    val usertype = sharedViewModel.userType.collectAsState()

    Log.d("UserType",usertype.value)

    var startDestination:String by remember { mutableStateOf(Routes.SignInScreen.route) }

    val studentDataState = userViewModel.studentDataState.collectAsState()
    val userDataState = userViewModel.userDataState.collectAsState()

    var fabExpanded by remember { mutableStateOf(false) }
    val onFabClick = {
        Log.d("My App","Floating Action Button clicked")
        if(sharedViewModel.reservedCycleUid.value == null)
            fabExpanded = !fabExpanded
        else {
            Log.d("My App","Continuing Booking")
            navController.navigate(Routes.BookingScreen.createRoute(false,true))
        }
    }
    val onOptionClick: (String) -> Unit = { value ->
        when(value){
            "Manual"->{
                navController.navigate(Routes.BookingScreen.createRoute(false,true))
            }
            "Scan QR"->{
                navController.navigate(Routes.BookingScreen.createRoute(true,false))
            }
            "Quick"->{
                navController.navigate(Routes.BookingScreen.createRoute(true,false))
            }
        }
        fabExpanded = false
    }
    val onFabClick2 = {
        //TODO
    }
    val onConfirmFab2: (String) -> Unit = { value ->
        println("Option clicked: $value")
    }

    LaunchedEffect(user.value){
        Log.d("MyApp","Inside User Launched Effect")
        Log.d("User Launched",user.value.toString())

        if(user.value!=null && usertype.value == ""){
            sharedViewModel.reloadCurrentUser()
            sharedViewModel.getUserRole(user.value!!.uid){
                sharedViewModel.updateUserType(it)
            }
        }
    }



    LaunchedEffect(user.value , usertype.value){
        Log.d("MyApp","Inside User and usertype Launched Effect")
        Log.d("User and usertype launched","${user.value.toString()} ${usertype.value}")
        if(user.value!=null && usertype.value=="User"){
            Log.d("User","calling fetchUserDetails")
             userViewModel.fetchUserDetails(user.value!!.uid)
        }
    }

    LaunchedEffect(userDataState.value){
        Log.d("MyApp","Inside UserDataState Launched Effect")
        Log.d("UserDataState Launched",userDataState.value.toString())
        if(!userDataState.value.isLoading){
            if(userDataState.value.error){
                userViewModel.updateStudentDataState(
                    StudentDataState(
                        isLoading = false,
                        error = true,
                        errorMessage = userDataState.value.errorMessage
                    )
                )
            }
            else
            {
                Log.d("User","calling fetchStudentDetails")
                userViewModel.fetchStudentDetails(userDataState.value.user.registrationNumber)
                if(userDataState.value.user.scheduleId!=""){
                    Log.d("User","calling fetchScheduleDetails")
                    //sharedViewModel.currUser.value?.let { userViewModel.fetchSchedule(it.uid) }
                    sharedViewModel.currUser.value?.let { userViewModel.fetchSchedule(userDataState.value.user.scheduleId) }
                }
                else{
                    userViewModel.updateScheduleDataState(FetchSchedulesDataState(
                        isLoading = false,
                        error = false
                    ))
                }
                //Checking the userTimer
                if( userDataState.value.user.timerStartTime!=null && userDataState.value.user.timerStartTime!! + 5*60*1000 > System.currentTimeMillis() && userDataState.value.user.scheduleId=="") {
                    Log.d("myApp","Inside If")
                    if(userDataState.value.user.cycleId != ""){
                        sharedViewModel.updateReservedCycleUid(userDataState.value.user.cycleId)
                        Log.d("myApp","calling startTimer")
                        sharedViewModel.startTimer(userDataState.value.user.timerStartTime!! + 5 * 60 * 1000 - System.currentTimeMillis()){
                            cycleViewModel.updateReserveAvailableCycleState(bookCycle(
                                isLoading = false,
                                cycle = null
                            ))
                        }
                    }
                    else{
                        Log.d("myApp","user.timerStartTime is set but user.cycleUid is empty")
                    }
                }
                else
                    Log.d("Timer","timerStartTime is null or completed")
            }
        }
    }

    LaunchedEffect(studentDataState.value , user.value) {
        Log.d("MyApp","Inside StudentDataState Launched Effect")
        if (!studentDataState.value.isLoading) {
            startDestination = if (studentDataState.value.error) {
                Routes.ErrorScreen.route
            } else {
                Routes.UserHome.route
            }
            startDestination = Routes.UserHome.route
        }
        else if(user.value!=null) startDestination = Routes.LoadingScreen.route
    }


    //Moved from booking page to here, now where-ever the user is, i.e. on any screen the dialog will be visible
    val showDialog1 = sharedViewModel.showDialog1.collectAsState()  //Used when timer is expired
    if(showDialog1.value)
    {
        val dialogMessage = when (currentRoute) {
            Routes.UserHome.route -> "The timer has expired. Please restart the booking process to book a ride."
            Routes.BookingScreen.route -> "The timer has expired. Please return to the home screen to restart the booking process."
            Routes.SignInScreen.route -> "Your session expired. Please sign in again."
            else -> "An error occurred. Please try again."
        }
        if(currentRoute == Routes.UserHome.route){
            cycleViewModel.updateReserveAvailableCycleState(bookCycle())
            //Reset availableCycleDataState
        }
        Component_tDialogBox(
            heading = "Timer Ran-Out",
            body = dialogMessage,
            onDismissRequest = {sharedViewModel.updateShowDialog1(false)}
        )
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                //modifier = Modifier.align(Alignment.TopCenter)
            )
        },
        topBar = {
            ViewTopAppBar("EasyCycle",scope,scaffoldState, sharedViewModel = sharedViewModel)
        },
        drawerContent = {
            drawerContent(scope,scaffoldState,navController)
        },
        scaffoldState=scaffoldState,
        backgroundColor = Color.Transparent,
        floatingActionButton = {
            BookingFAB(fabExpanded,onFabClick,onOptionClick,onFabClick2, onConfirmFab2)
        }
    )
    { it ->
        NavHost(navController = navController, startDestination = startDestination, modifier = Modifier.padding(it)) {
            //Add Composable here or all navigation screens
            composable("SignInScreen") {
                SignInScreen(snackbarHostState , sharedViewModel){value->
                    sharedViewModel.updateUserType(value)
                }
            }
            composable(Routes.UserHome.route) {
                Log.d("myApp","Navigating to home screen")
                homeScreen(studentDataState.value.student,navController,userViewModel,sharedViewModel,snackbarHostState)
            }
            composable(Routes.ErrorScreen.route) {
                Log.d("myApp","Navigating to error screen")
                errorPage("Please pass the parameter in the MYAPP")
            }
            composable(Routes.LoadingScreen.route) {
                LoadingPage()
            }
            composable(
                Routes.BookingScreen.route,
                arguments = listOf(
                    navArgument("rentNow") { type = NavType.BoolType },
                    navArgument("rentLater") { type = NavType.BoolType }
            )) {backStackEntry->
                val rentNow= backStackEntry.arguments?.getBoolean("rentNow") ?: false
                val rentLater = backStackEntry.arguments?.getBoolean("rentLater") ?: true
                BookingScreen(rentNow = rentNow , rentLater = rentLater,sharedViewModel,cycleViewModel,userViewModel,snackbarHostState, navController)
            }
            composable(Routes.AllCycleScreen.route) {
                AllCyclesScreen(cycleViewModel,sharedViewModel,navController){
                    //TODO
                }
            }
            composable(route = Routes.EachScheduleDetailScreen.route) {
                Log.d("myApp","Navigating to EachScheduleDetailScreen")
                eachScheduleDetailScreen(userViewModel, navController)
            }
        }
    }
 }

sealed class Routes(val route: String) {
    object UserHome : Routes("UserHome")
    object SignInScreen : Routes("SignInScreen")
    object ErrorScreen : Routes("ErrorScreen")
    object LoadingScreen : Routes("LoadingScreen")
    object BookingScreen : Routes("BookingScreen/{rentNow}/{rentLater}"){
        fun createRoute(rentNow: Boolean, rentLater: Boolean) = "BookingScreen/$rentNow/$rentLater"
    }
    object EachScheduleDetailScreen : Routes("EachScheduleDetailScreen")
    object AllCycleScreen : Routes("AllCycleScreen")
}


