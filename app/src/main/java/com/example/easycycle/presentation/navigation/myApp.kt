package com.example.easycycle.presentation.navigation

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.easycycle.data.model.ResultState
import com.example.easycycle.logMessageOnLogcat
import com.example.easycycle.presentation.ui.AllCyclesScreen
import com.example.easycycle.presentation.ui.BookingScreen
import com.example.easycycle.presentation.ui.LoadingComposable
import com.example.easycycle.presentation.ui.QRCodeScannerScreen
import com.example.easycycle.presentation.ui.SignInScreen
import com.example.easycycle.presentation.ui.components.BookingFAB
import com.example.easycycle.presentation.ui.components.Component_tDialogBox
import com.example.easycycle.presentation.ui.components.ViewTopAppBar
import com.example.easycycle.presentation.ui.components.drawerContent
import com.example.easycycle.presentation.ui.eachScheduleDetailScreen
import com.example.easycycle.presentation.ui.errorPage
import com.example.easycycle.presentation.ui.homeScreen
import com.example.easycycle.presentation.viewmodel.CycleViewModel
import com.example.easycycle.presentation.viewmodel.SharedViewModel
import com.example.easycycle.presentation.viewmodel.UserViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun myApp(
    navController: NavController,
    startDestination: String,
    sharedViewModel: SharedViewModel,
    userViewModel: UserViewModel,
    cycleViewModel: CycleViewModel,
    snackbarHostState : SnackbarHostState
) {
    val userViewModelLoadingShow = userViewModel.userViewModelLoadingShow.collectAsState()
    val cycleViewModelLoadingShow = cycleViewModel.cycleViewModelLoadingShow.collectAsState()

    val scaffoldState= rememberScaffoldState()
    val scope= rememberCoroutineScope()

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    //Floating Action Button1
    var fabExpanded by remember { mutableStateOf(false) }
    val onFabClick = {
        Log.d("My App","Floating Action Button clicked")
        if(sharedViewModel.reservedCycleUid.value == null)
            fabExpanded = !fabExpanded
        else {
            Log.d("My App","Continuing Booking")
            navigateToBookingScreen(userViewModel,sharedViewModel,cycleViewModel,navController,
                rentNow = false,
                rentLater = true
            )
        }
    }
    val onOptionClick: (String) -> Unit = { value ->
        when(value){
            "Manual"->{
                navigateToBookingScreen(userViewModel,sharedViewModel,cycleViewModel,navController,rentNow = false, rentLater = true)
            }
            "Scan QR"->{
                navigateToQRScannerScreen(navController,userViewModel,sharedViewModel,cycleViewModel)
            }
            "Quick"->{
                navigateToBookingScreen(userViewModel,sharedViewModel,cycleViewModel,navController,rentNow = false, rentLater = true)
            }
        }
        fabExpanded = false
    }

    val userDataState = userViewModel.userDataState.collectAsState()

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
        cycleViewModel.updateReserveAvailableCycleState(ResultState.Loading(false))
        sharedViewModel.updateReservedCycleUid(null)

        Component_tDialogBox(
            heading = "Timer Ran-Out",
            body = dialogMessage,
            onDismissRequest = {sharedViewModel.updateShowDialog1(false)}
        )
    }

    Box(modifier = Modifier.fillMaxSize()){
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp) // Optional padding
                .zIndex(1f)
        )
        Scaffold(
            topBar = {
                if( currentRoute !=  Routes.SignInScreen.route)
                    ViewTopAppBar("EasyCycle",scope,scaffoldState, sharedViewModel = sharedViewModel)
            },
            drawerContent = {
                if( currentRoute !=  Routes.LoadingScreen.route)
                    drawerContent(scope,scaffoldState,navController)
            },
            scaffoldState=scaffoldState,
            backgroundColor = Color.Transparent,
            floatingActionButton = {
                when (val state = userDataState.value){
                    is ResultState.Success ->{
                        if(state.data!!.scheduleId == "" && currentRoute == Routes.UserHome.route)
                            BookingFAB(fabExpanded,onFabClick,onOptionClick)
                            //BookingFAB(fabExpanded,onFabClick,onOptionClick,onFabClick2, onConfirmFab2)
                    }
                    else -> {}
                }
            }
            )
        {
            Navigation(navController,startDestination,it,userViewModel,sharedViewModel,cycleViewModel)
        }

        if( userViewModelLoadingShow.value || cycleViewModelLoadingShow.value ){
            LoadingComposable(true)
        }
    }

    //snackBarWithAction(snackbarHostState,scope)

}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Navigation(navController: NavController,
               startDestination:String,
               pd:PaddingValues,
               userViewModel: UserViewModel,
               sharedViewModel: SharedViewModel,
               cycleViewModel: CycleViewModel,
) {
    NavHost(
        navController = navController as NavHostController,
        startDestination = startDestination,
        modifier = Modifier.padding(pd)
    ) {

        composable("SignInScreen") {
            logMessageOnLogcat("navigation","Navigating to SignInScreen")
            SignInScreen(sharedViewModel){value->
                sharedViewModel.updateUserType(value)
            }
        }
        composable(Routes.UserHome.route) {
            logMessageOnLogcat("navigation","Navigating to UserHomeScreen")

            homeScreen(navController,userViewModel,sharedViewModel,cycleViewModel)
        }
        composable(Routes.ErrorScreen.route) {
            logMessageOnLogcat("navigation","Navigating to ErrorScreen")

            errorPage(message = "Pass Error on myApp")
        }
        composable(Routes.LoadingScreen.route) {
            logMessageOnLogcat("navigation","Navigating to LoadingScreen")

            LoadingComposable(true)
        }
        composable(
            Routes.BookingScreen.route,
            arguments = listOf(
                navArgument("rentNow") { type = NavType.BoolType },
                navArgument("rentLater") { type = NavType.BoolType }
            )) {backStackEntry->
            logMessageOnLogcat("navigation","Navigating to BookingScreen")

            val rentNow= backStackEntry.arguments?.getBoolean("rentNow") ?: false
            val rentLater = backStackEntry.arguments?.getBoolean("rentLater") ?: true

            BookingScreen(
                rentNow = rentNow,
                rentLater = rentLater,
                sharedViewModel = sharedViewModel,
                cycleViewModel = cycleViewModel,
                userViewModel = userViewModel,
                navController = navController
            )
        }
        composable(Routes.AllCycleScreen.route) {
            logMessageOnLogcat("navigation","Navigating to AllCyclesScreen")
            AllCyclesScreen(cycleViewModel,sharedViewModel,navController){
                //TODO Implement Click Event If Needed
            }
        }
        composable(route = Routes.EachScheduleDetailScreen.route) {
            logMessageOnLogcat("navigation","Navigating to EachScheduleDetailScreen")
            eachScheduleDetailScreen(userViewModel,navController,sharedViewModel,cycleViewModel)
        }
        composable(route = Routes.QrScannerScreen.route) {
            logMessageOnLogcat("navigation","Navigating to QrScannerScreen")
            QRCodeScannerScreen(navController,userViewModel,sharedViewModel,cycleViewModel)
        }
    }
}

fun navigateToHomeScreen(navController: NavController , userViewModel: UserViewModel , sharedViewModel: SharedViewModel){

    Log.d("HomeScreen","Inside Function Call To navigation")
    // Give Access To FetchProfileData if previously error was occurred
    if(sharedViewModel.profileDataState.value is ResultState.Error)
        sharedViewModel.updateProfileDataState(ResultState.Loading(true))

    //Give Access to fetch user ScheduleData if it is null, every time when moving to home screen
    when(val state = userViewModel.scheduleDataState.value){
        is ResultState.Loading ->{
            if(!state.isLoading)
                userViewModel.updateScheduleDataState(ResultState.Loading(true))
        }
        else ->{}
    }

    //Give Access To Fetch UserData if it is first time
    when(val state = userViewModel.userDataState.value){
        is ResultState.Loading ->{
            if(!state.isLoading)
                userViewModel.updateUserDataState(ResultState.Loading(true))
        }
        else -> {}
    }

    navController.navigate(Routes.UserHome.route) {
        popUpTo(Routes.UserHome.route) { inclusive = true } // Ensure QR scanner is removed
        launchSingleTop = true
    }
}

fun navigateToLoginScreen(navController: NavController){

    //TODO when logging out, remember to clear the roomStorage

    navController.navigate(Routes.SignInScreen.route){
        launchSingleTop = true
        popUpTo(navController.graph.startDestinationId) { inclusive = true }
    }
}

fun navigateToBookingScreen(userViewModel: UserViewModel, sharedViewModel: SharedViewModel, cycleViewModel: CycleViewModel,navController: NavController ,rentNow:Boolean,rentLater:Boolean){

    // Before navigating to BookingScreen ensure noCycleAvailable dialog is reset
    sharedViewModel.updateShowDialog2(false)

    //If previously, while booking the schedule error occurred, means no schedule was booked, then allow again to book schedule
    if(userViewModel.createScheduleState.value is ResultState.Error)
        userViewModel.updateCreateScheduleState(ResultState.Loading(false))

    //If no cycle is reserved, then allow to reserve a cycle
    when(val state = cycleViewModel.reserveAvailableCycleState.value){
        is ResultState.Loading ->{
            if(!state.isLoading)
                cycleViewModel.updateReserveAvailableCycleState(ResultState.Loading(true))
        }
        else -> {}
    }

    navController.navigate(Routes.BookingScreen.createRoute(rentNow,rentLater)){
        launchSingleTop = true
    }
}
fun navigateToEachScheduleDetailScreen(userViewModel: UserViewModel,navController:NavController){

    // Every Time we move to this screen, Allow to return and cancel the current schedule
    userViewModel.updateReturnOrCancelSchedule(ResultState.Loading(false))

    navController.navigate(Routes.EachScheduleDetailScreen.route){
        launchSingleTop = true
    }
}
fun navigateToAllCycleScreen(cycleViewModel: CycleViewModel,navController:NavController){

    // Always reset and allow to fetch allCycles data
    cycleViewModel.updateGetAllCycleDataState(ResultState.Loading(true))
    navController.navigate(Routes.AllCycleScreen.route){
        launchSingleTop = true
    }
}

fun navigateToLoadingScreen(navController: NavController){

    //NO RESOURCES NEEDED
    navController.navigate(Routes.LoadingScreen.route){
        launchSingleTop = true
    }
}

fun navigateToQRScannerScreen(navController: NavController,userViewModel: UserViewModel,sharedViewModel: SharedViewModel,cycleViewModel: CycleViewModel){
    Log.d("QRSCANNER","Inside Function Call To navigation")

    when(val state = cycleViewModel.reserveAvailableCycleState.value){
        is ResultState.Loading ->{
            if(!state.isLoading)
                cycleViewModel.updateReserveAvailableCycleState(ResultState.Loading(true))
        }
        else -> {}
    }

    navController.navigate(Routes.QrScannerScreen.route){
        popUpTo(Routes.UserHome.route) { inclusive = false }
        launchSingleTop = true
    }
}

fun navigateToErrorScreen(navController:NavController, clearStack:Boolean = false, message:String){

    Log.e("ERROR",message)
    //NO resource Needed
    navController.navigate(Routes.ErrorScreen.route){
        launchSingleTop = true
        //Either pop all the screens, or only last screen where error Occurred
        if (clearStack) popUpTo(navController.graph.startDestinationId) { inclusive = true }
        else navController.popBackStack()

        //NOTE : Popping only last screen may create multiple scenarios of inconsistency, like when booking schedule so check this
    }
}


sealed class Routes(val route: String) {
    data object UserHome : Routes("UserHome")
    data object SignInScreen : Routes("SignInScreen")
    data object ErrorScreen : Routes("ErrorScreen")
    data object LoadingScreen : Routes("LoadingScreen")
    data object BookingScreen : Routes("BookingScreen/{rentNow}/{rentLater}"){
        fun createRoute(rentNow: Boolean, rentLater: Boolean) = "BookingScreen/$rentNow/$rentLater"
    }
    data object EachScheduleDetailScreen : Routes("EachScheduleDetailScreen")
    data object AllCycleScreen : Routes("AllCycleScreen")
    data object QrScannerScreen : Routes("QrScannerScreen")
}





