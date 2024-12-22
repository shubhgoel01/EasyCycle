package com.example.easycycle.presentation.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easycycle.data.model.Student
import com.example.easycycle.data.remote.SharedFirebaseService
import com.example.easycycle.domain.usecases.AdminUseCases
import com.example.easycycle.domain.usecases.StudentUseCases
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(
    private val studentUseCase: StudentUseCases,
    private val adminUseCases: AdminUseCases,
    private val firebaseAuth: FirebaseAuth,
    private val sharedDatabase: SharedFirebaseService,
) : ViewModel() {

    private val _reservedCycleUid = MutableStateFlow<String?>(null)
    val reservedCycleUid: StateFlow<String?> = _reservedCycleUid
    fun updateReservedCycleUid(cycleUid:String?){
        _reservedCycleUid.value = cycleUid
    }


    private val _currUser = MutableStateFlow(firebaseAuth.currentUser)
    val currUser: StateFlow<FirebaseUser?> = _currUser

    private val _userType = MutableStateFlow("")
    val userType: StateFlow<String> = _userType

//Implementing Timer
    private var timerJob: Job? = null // Keeps track of the running timer
    private var remainingMillis = 0L // Tracks the remaining time in milliseconds
    private val _remainingTime: MutableStateFlow<String?> = MutableStateFlow(null)
    val remainingTime: StateFlow<String?> = _remainingTime
    private val _isTimerRunning = MutableStateFlow(false) // Tracks timer status
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning

//For showing "Live" Button on topAppBar
    private var liveIconJob: Job? = null
    private val _showLiveIcon: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val showLiveIcon: StateFlow<Boolean> = _showLiveIcon
    private val _showLiveIconMessage: MutableStateFlow<String> = MutableStateFlow("")
    val showLiveIconMessage: StateFlow<String> = _showLiveIconMessage


    private val _showDialog1 = MutableStateFlow(false) // used by booking page when timer is expired
    val showDialog1 :StateFlow<Boolean> = _showDialog1
    fun updateShowDialog1(value:Boolean){
        _showDialog1.value = value
    }

    private val _showDialog2 = MutableStateFlow(false) // used by booking page when no cycle is available
    val showDialog2 :StateFlow<Boolean> = _showDialog2
    fun updateShowDialog2(value:Boolean){
        _showDialog2.value = value
    }

    init {
        Log.d("SharedViewModel","Inside Init")
        // Listen for Firebase Auth state changes
        firebaseAuth.addAuthStateListener { auth ->
            _currUser.value = auth.currentUser
        }
    }

    private var studentLoginData = Student()

    fun updateUserType(updatedUserType: String) {
        _userType.value = updatedUserType
        Log.d("User Type updated", _userType.value)
    }

    private fun updateCurrentUser() {
        _currUser.value = firebaseAuth.currentUser
    }

    fun login(
        userType: String, loginMethod: String, userId: String, password: String, context: Context
    ) {
        var flag: Boolean = false
        Log.d("Login", "Inside SharedViewModel")
        if (userType == "User") {
            if (loginMethod == "Email")
                studentLoginData.email = userId
            else studentLoginData.registrationNumber = userId
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    flag = studentUseCase.login(studentLoginData, password) {
                        updateCurrentUser()
                    }
                } catch (e: FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(context, "Invalid Password", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Unexpected Error Occurred", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // Handle admin login logic here
        }
    }

    fun getUserRole(userUid: String, onComplete: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val role = sharedDatabase.getUserRole(userUid)
            onComplete(role)
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
        _currUser.value = null  // Explicitly set the current user to null after sign-out
        _userType.value = ""    // Clear the user type after sign-out
    }


    fun reloadCurrentUser() {
        viewModelScope.launch {
            sharedDatabase.reloadCurrentUser()
        }
    }

    fun startTimer(durationMillis: Long,onComplete:()->Unit) {

        Log.d("SharedViewModel","StartTimerCalled")
        remainingMillis = durationMillis
        _isTimerRunning.value = true

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (remainingMillis > 0) {
                val minutes = (remainingMillis / 60000).toInt()
                val seconds = ((remainingMillis % 60000) / 1000).toInt()
                _remainingTime.value = String.format("%02d:%02d", minutes, seconds)
                if(remainingMillis == 1000L)
                {
                    updateShowDialog1(true)
                    Log.d("Timer","One second remaining setting showDialog1 = true")
                }
                delay(1000)
                remainingMillis -= 1000
            }
            _isTimerRunning.value = false
            _remainingTime.value = null
            _reservedCycleUid.value = null
            onComplete()
        }
    }
    fun endTimer() {
        timerJob?.cancel()
        remainingMillis = 0
        _remainingTime.value = "00:00"
        _isTimerRunning.value = false
    }

    fun startLiveIcon() {
        Log.d("SharedViewModel", "startLiveIcon")
        _showLiveIcon.value = true

        // Cancel any existing job to avoid multiple jobs running simultaneously
        liveIconJob?.cancel()

        liveIconJob = viewModelScope.launch {
            while (_showLiveIcon.value) {
                _showLiveIconMessage.value = "LIVE" // Show "LIVE"
                delay(700) // Display for 500ms
                _showLiveIconMessage.value = "        " // Hide "LIVE"
                delay(700) // Hide for 500ms
            }
        }
    }
    fun stopLiveIcon() {
        Log.d("SharedViewModel", "stopLiveIcon")
        _showLiveIcon.value = false
        liveIconJob?.cancel() // Cancel the running Job
    }

    override fun onCleared() {
        super.onCleared()
        // Remove the auth listener to prevent memory leaks
        firebaseAuth.removeAuthStateListener { auth ->
            _currUser.value = auth.currentUser
        }
    }
}


