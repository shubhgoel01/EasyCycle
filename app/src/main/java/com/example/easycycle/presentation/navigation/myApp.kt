package com.example.easycycle.presentation.navigation

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material.rememberScaffoldState
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.easycycle.data.model.Cycle
import com.example.easycycle.data.model.Student
import com.example.easycycle.data.model.StudentDataState
import com.example.easycycle.presentation.ui.LoadingPage
import com.example.easycycle.presentation.ui.SignInScreen
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

@Composable
fun myApp(
    userViewModel: UserViewModel = hiltViewModel(),
    adminViewModel: AdminViewModel = hiltViewModel(),
    sharedViewModel: SharedViewModel = hiltViewModel(),
    cycleViewModel: CycleViewModel = hiltViewModel()
) {

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

    LaunchedEffect(user.value){
        Log.d("MyApp","Inside User Launched Effect")
        Log.d("User Launched",user.value.toString())

        if(user.value!=null && usertype.value == ""){
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
            }
        }
    }

    LaunchedEffect(studentDataState.value) {
        Log.d("MyApp","Inside StudentDataState Launched Effect")
        if (!studentDataState.value.isLoading) {
            startDestination = if (studentDataState.value.error) {
                Routes.ErrorScreen.route
            } else {
                Routes.UserHome.route
            }
            navController.navigate(startDestination)
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
        backgroundColor = Color.Transparent
    )
    {
        NavHost(navController = navController, startDestination = startDestination, modifier = Modifier.padding(it)) {
            //Add Composable here or all navigation screens
            composable("SignInScreen") {
                SignInScreen(snackbarHostState , sharedViewModel){value->
                    sharedViewModel.updateUserType(value)
                }
            }
            composable("UserHome") {
                homeScreen(studentDataState.value.student)
            }
            composable("ErrorScreen") {
                errorPage("Please pass the parameter in the MYAPP")
            }
            composable("LoadingScreen") {
                LoadingPage()
            }
        }
    }
}

sealed class Routes(val route: String) {
    object UserHome : Routes("UserHome")
    object SignInScreen : Routes("SignInScreen")
    object ErrorScreen : Routes("ErrorScreen")
    object LoadingScreen : Routes("LoadingScreen")

}
