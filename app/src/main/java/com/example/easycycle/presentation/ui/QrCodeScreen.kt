package com.example.easycycle.presentation.ui

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.easycycle.CustomScannerActivity
import com.example.easycycle.data.model.ResultState
import com.example.easycycle.logErrorOnLogcat
import com.example.easycycle.presentation.navigation.navigateToBookingScreen
import com.example.easycycle.presentation.navigation.navigateToErrorScreen
import com.example.easycycle.presentation.navigation.navigateToHomeScreen
import com.example.easycycle.presentation.viewmodel.CycleViewModel
import com.example.easycycle.presentation.viewmodel.SharedViewModel
import com.example.easycycle.presentation.viewmodel.UserViewModel
import com.google.zxing.integration.android.IntentIntegrator


@Composable
fun QRCodeScannerScreen(
    navController: NavController,
    userViewModel: UserViewModel,
    sharedViewModel: SharedViewModel,
    cycleViewModel: CycleViewModel,
) {
    val activity = LocalContext.current as Activity
    var scannerLaunched by remember { mutableStateOf(false) } // Add this flag
    val scannedCode = remember { mutableStateOf<String?>(null) }
    val reserveCycleState = cycleViewModel.reserveAvailableCycleState.collectAsState()
    val context = LocalContext.current

    val qrCodeLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val scanResult = if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            IntentIntegrator.parseActivityResult(result.resultCode, intent)?.contents
        } else {
            null
        }

        scannedCode.value = scanResult ?: ""
    }

    DisposableEffect(Unit) {
        onDispose {
            when (val state = cycleViewModel.reserveAvailableCycleState.value) {
                is ResultState.Loading -> {
                    if (state.isLoading)
                        cycleViewModel.updateReserveAvailableCycleState(ResultState.Loading(false))
                }

                else -> {}
            }
        }
    }

    LaunchedEffect(scannerLaunched) {
        if (!scannerLaunched) {
            scannerLaunched = true // Ensure scanner launches only once
            val intentIntegrator = IntentIntegrator(activity)
            intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
            intentIntegrator.setPrompt("Scan a QR Code")
            intentIntegrator.setBeepEnabled(true)
            intentIntegrator.setOrientationLocked(false)
            intentIntegrator.captureActivity = CustomScannerActivity::class.java
            qrCodeLauncher.launch(intentIntegrator.createScanIntent())
        }
    }

    //TODO Check if it is also called if state does not change but value of the state changes eg. isLoading is updated from true to false. NOT SURE ABOUT THIS
    LaunchedEffect(reserveCycleState.value) {
        when (val state = reserveCycleState.value) {
            is ResultState.Loading -> {
                if(!state.isLoading){
                    navigateToHomeScreen(navController,userViewModel,sharedViewModel)
                    // TODO ShowSnackBar that cycle is already booked or under process
                }
            }
            is ResultState.Success -> navigateToBookingScreen(userViewModel,sharedViewModel,cycleViewModel,navController,rentNow = true,rentLater = false)
            is ResultState.Error -> {
                logErrorOnLogcat("WRScanner",state.error)
                navigateToErrorScreen(navController,clearStack = false, message = "")
                // TODO Also Show Message that some error unexpected occurred
            }
        }
    }

    //TODO Also validate the scanned value that it is a valid cycleId And That It is not booked
    // For now it only checks for status
    LaunchedEffect(scannedCode.value) {
        when (val state = reserveCycleState.value) {
            is ResultState.Loading -> {
                if (state.isLoading && scannedCode.value != null && scannedCode.value != "") {
                    cycleViewModel.checkAvailableAndBookCycle(
                        cycleUid = scannedCode.value!!,
                        context = context,
                        onCancel = { sharedViewModel.updateShowDialog2(true) },
                        onComplete = {
                            sharedViewModel.updateReservedCycleUid(scannedCode.value)
                            sharedViewModel.startTimer(5 * 60 * 1000)
                        })
                } else if (state.isLoading && scannedCode.value == "") {
                    navigateToHomeScreen(navController, userViewModel, sharedViewModel)
                    // Means No QR-Code was scanned
                }
            }

            else -> {}
        }
    }
}