package com.example.easycycle

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.easycycle.data.model.Student
import com.example.easycycle.presentation.navigation.myApp
import com.example.easycycle.presentation.ui.SignInScreen
import com.example.easycycle.presentation.viewmodel.AdminViewModel
import com.example.easycycle.ui.theme.EasyCycleTheme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint      //For HILT
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EasyCycleTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    //Call function here
                    myApp()

                    //var adminViewModel = AdminViewModel()
                    //adminViewModel.addStudent(applicationContext,student=Student("Shubh Goel","04shubhgoel@gmail.com","2340064","MCA",phone="8076949994"))
                }
            }
        }
    }
}
