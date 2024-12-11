package com.example.easycycle.presentation.navigation

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.FabPosition
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
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.easycycle.data.Enum.ScheduleState
import com.example.easycycle.data.model.Cycle
import com.example.easycycle.data.model.Schedule
import com.example.easycycle.data.model.ScheduleStatus
import com.example.easycycle.data.model.SchedulesDataState
import com.example.easycycle.data.model.Student
import com.example.easycycle.data.model.StudentDataState
import com.example.easycycle.data.model.userDataState
import com.example.easycycle.presentation.ui.BookingPage
import com.example.easycycle.presentation.ui.LoadingPage
import com.example.easycycle.presentation.ui.SignInScreen
import com.example.easycycle.presentation.ui.components.BookingFAB
import com.example.easycycle.presentation.ui.components.ViewTopAppBar
import com.example.easycycle.presentation.ui.components.drawerContent
import com.example.easycycle.presentation.ui.errorPage
import com.example.easycycle.presentation.ui.homeScreen
import com.example.easycycle.presentation.viewmodel.AdminViewModel
import com.example.easycycle.presentation.viewmodel.CycleViewModel
import com.example.easycycle.presentation.viewmodel.SharedViewModel
import com.example.easycycle.presentation.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun myApp(
    userViewModel: UserViewModel = hiltViewModel(),
    adminViewModel: AdminViewModel = hiltViewModel(),
    sharedViewModel: SharedViewModel = hiltViewModel(),
    cycleViewModel: CycleViewModel = hiltViewModel()
) {
/*
    SimpleTimeInputDialog(
        onConfirm = { hour, minute ->
            println("Selected time: $hour:$minute")
        },
        onDismiss = {
            println("Dialog dismissed")
        }
    )

 */
    //BookingPage(true,false)
    //cycleViewModel.addCycle(Cycle(cycleId = "2"))
    //sharedViewModel.signOut()

    val scaffoldState= rememberScaffoldState()
    val scope= rememberCoroutineScope()
    val navController = rememberNavController()
    val snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }

    val user = sharedViewModel.currUser.collectAsState()
    val usertype = sharedViewModel.userType.collectAsState()

    Log.d("UserType",usertype.value)

    var startDestination:String by remember { mutableStateOf(Routes.SignInScreen.route) }

    val studentDataState = userViewModel.studentDataState.collectAsState()
    val userDataState = userViewModel.userDataState.collectAsState()

    var fabExpanded by remember { mutableStateOf(false) }
    val onFabClick = {
        fabExpanded = !fabExpanded
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
    }
    val onFabClick2 = {

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

            val tempSchedule = Schedule(
                userUid = user.value!!.uid,
                startTime = System.currentTimeMillis() + 1 * 60 * 1000,
                estimateTime = 2 * 60 * 1000,
                Status = ScheduleStatus(
                    status = ScheduleState.BOOKED,
                )
            )
            Log.d("Schedule","Creating New Schedule")
            //userViewModel.createSchedule(user.value!!.uid,tempSchedule)
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
                    sharedViewModel.currUser.value?.let { userViewModel.fetchSchedule(it.uid) }
                }
                else{
                    userViewModel.updateScheduleDataState(SchedulesDataState(
                        isLoading = false,
                        error = false
                    ))
                }
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

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {ViewTopAppBar("EasyCycle",scope,scaffoldState)},
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
                homeScreen(studentDataState.value.student,navController,userViewModel,sharedViewModel)
            }
            composable(Routes.ErrorScreen.route) {
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
                val rentNow= backStackEntry.arguments?.getBoolean("rentNow") ?: true
                val rentLater = backStackEntry.arguments?.getBoolean("rentLater") ?: false
                BookingPage(rentNow = rentNow , rentLater = rentLater)
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
}
