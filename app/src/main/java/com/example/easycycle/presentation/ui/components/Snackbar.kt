package com.example.easycycle.presentation.ui.components

import android.util.Log
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun snackBarWithAction(
    snackbarHostState : SnackbarHostState,
    appCoroutineScope : CoroutineScope,
    action:(()->Composable)? = null
) {
    Log.d("Snackbar","Function Called")
    appCoroutineScope.launch {
        snackbarHostState.showSnackbar(
            message = "Hello from Snackbar!",
            actionLabel = "Dismiss",
            duration = SnackbarDuration.Indefinite,
        )
    }
}