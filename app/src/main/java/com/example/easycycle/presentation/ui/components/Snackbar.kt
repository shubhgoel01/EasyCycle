package com.example.easycycle.presentation.ui.components

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.easycycle.presentation.viewmodel.AdminViewModel
import com.example.easycycle.presentation.viewmodel.SharedViewModel
import kotlinx.coroutines.launch

@Composable
fun ComponentSnackbar(
    message: String,
    actionLabel: String? = null,
    duration: SnackbarDuration = SnackbarDuration.Indefinite,
    onActionClick: (() -> Unit)? = null,
    snackbarHostState: SnackbarHostState
) {
    Log.d("Snackbar","SnackBar Displayed")
    val scope = rememberCoroutineScope()

    scope.launch {
        Log.d("Snackbar","Launched")
        val result = snackbarHostState.showSnackbar(
            message = message,
            actionLabel = actionLabel,
            duration = duration
        )
        Log.d("Snackbar","Result: ${result.toString()}")
        when (result) {
            SnackbarResult.ActionPerformed -> {
                Log.d("Snackbar","Completed")
                onActionClick?.invoke()
            }
            SnackbarResult.Dismissed -> {
                Log.d("Snackbar","Cancelled")
            }
            else ->{
                Log.d("Snackbar",result.toString())
            }
        }
    }
}



