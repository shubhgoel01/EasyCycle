package com.example.easycycle

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.easycycle.presentation.navigation.Routes
import com.example.easycycle.presentation.navigation.myApp
import com.example.easycycle.presentation.navigation.navigateToHomeScreen
import com.example.easycycle.presentation.viewmodel.AdminViewModel
import com.example.easycycle.presentation.viewmodel.CycleViewModel
import com.example.easycycle.presentation.viewmodel.SharedViewModel
import com.example.easycycle.presentation.viewmodel.UserViewModel
import com.example.easycycle.ui.theme.EasyCycleTheme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint  // For HILT
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    private lateinit var navController: NavController

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            navController = rememberNavController()

            val sharedViewModel: SharedViewModel = hiltViewModel()
            val userViewModel: UserViewModel = hiltViewModel()
            val adminViewModel: AdminViewModel = hiltViewModel()
            val cycleViewModel: CycleViewModel = hiltViewModel()

            EasyCycleTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val user = sharedViewModel.currUser.collectAsState()

                    LaunchedEffect(user.value) {
                        if (user.value != null) {
                            navigateToHomeScreen(navController,userViewModel,sharedViewModel)
                        }
                    }

                    val startDestination = if (user.value == null) {
                        Routes.SignInScreen.route
                    } else {
                        null
                    }


                    // Passing all the necessary parameters to myApp
                    myApp(
                        navController = navController,
                        startDestination = startDestination ?: Routes.SignInScreen.route,
                        sharedViewModel = sharedViewModel,
                        userViewModel = userViewModel,
                        cycleViewModel = cycleViewModel
                    )
                }
            }
        }
    }
}

//{
//    val user = sharedViewModel.currUser.collectAsState()
//    val startDestination = if (user.value == null) {
//        Routes.SignInScreen.route
//    } else {
//        Routes.UserHome.route
//    }
//
//    // Passing all the necessary parameters to myApp
//    myApp(
//        navController = navController,
//        startDestination = startDestination,
//        sharedViewModel = sharedViewModel,
//        userViewModel = userViewModel,
//        cycleViewModel = cycleViewModel
//    )
//}
