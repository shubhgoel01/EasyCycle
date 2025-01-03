package com.example.easycycle.presentation.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easycycle.data.Enum.ErrorType
import com.example.easycycle.data.model.AppErrorException
import com.example.easycycle.data.model.ResultState
import com.example.easycycle.data.model.Student
import com.example.easycycle.data.remote.Profile
import com.example.easycycle.data.remote.ProfileRepository
import com.example.easycycle.data.remote.SharedFirebaseService
import com.example.easycycle.domain.usecases.AdminUseCases
import com.example.easycycle.domain.usecases.StudentUseCases
import com.example.easycycle.logErrorOnLogcat
import com.example.easycycle.logInformationOnLogcat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(
    private val profileRepo : ProfileRepository,
    private val studentUseCase: StudentUseCases,
    private val adminUseCases: AdminUseCases,
    private val firebaseAuth: FirebaseAuth,
    private val sharedDatabase: SharedFirebaseService
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

//For showing "Live" Button on topAppBar
    private var liveIconJob: Job? = null
    private val _showLiveIcon: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val showLiveIcon: StateFlow<Boolean> = _showLiveIcon
    private val _showLiveIconMessage: MutableStateFlow<String> = MutableStateFlow("")
    val showLiveIconMessage: StateFlow<String> = _showLiveIconMessage

//Implementing room database
    private val _profileDataState = MutableStateFlow<ResultState<Profile>>(ResultState.Loading(false))
    val profileDataState: StateFlow<ResultState<Profile>> = _profileDataState
    fun updateProfileDataState(value:ResultState<Profile>){
        _profileDataState.value = value
    }
    fun insertProfile(profile: Profile) = viewModelScope.launch {
        profileRepo.insertProfile(profile)
    }
    fun clearProfile() = viewModelScope.launch {
        profileRepo.clearProfile()
    }

    init {
        logInformationOnLogcat("Profile","Init - Trying to fetch profile data")
        if(_profileDataState.value is ResultState.Loading && !(_profileDataState.value as ResultState.Loading).isLoading)
            getProfile()
    }

    private fun getProfile(){
        _profileDataState.value = ResultState.Loading(true)
        //TODO Here Commented this, because this is causing re-fetching navigating to home screen, solve this later
        viewModelScope.launch(Dispatchers.IO) {
            try {
                logInformationOnLogcat("getProfile","Fetching Profile Data")
                val data = profileRepo.getProfile()
                if(data == null){
                    val e = AppErrorException(ErrorType.DATA_NOT_FOUND,"getProfile SharedViewModel","Data in room database is null")
                    logErrorOnLogcat("Profile",e)
                    _profileDataState.value = ResultState.Error(e)
                }
                else {
                    logInformationOnLogcat("Profile","Successfully fetched Profile Data")
                    _profileDataState.value = ResultState.Success(data)
                    _userType.value = data.userType
                }
            }
            catch (e:Exception){
                val error = AppErrorException(ErrorType.UNEXPECTED_ERROR,"getProfile SharedViewModel",e.message.toString())
                logErrorOnLogcat("Profile",error)
                _profileDataState.value = ResultState.Error(AppErrorException(ErrorType.UNEXPECTED_ERROR,"getProfile SharedViewModel","$e"))
            }
        }
    }


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
        logInformationOnLogcat("FirebaseAuth","Adding listener")
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
        //TODO add check condition if userType == "user"
        if (loginMethod == "Email")
            studentLoginData.email = userId
        else studentLoginData.registrationNumber = userId

        viewModelScope.launch(Dispatchers.IO) {
            try {
                logInformationOnLogcat("Login","Trying to login")
                studentUseCase.login(studentLoginData, password) { registrationNumber->
                    updateCurrentUser()
                }
            }
            catch (error : AppErrorException){
                logErrorOnLogcat("Login",error)
                when(error.type){
                    ErrorType.STUDENT_NOT_AUTHORIZED -> Toast.makeText(context,"You Are Not Authorized To Use This App",Toast.LENGTH_SHORT).show()
                    ErrorType.WEAK_PASSWORD_ERROR -> Toast.makeText(context,"Weak Password, Set Some Strong Password",Toast.LENGTH_SHORT).show()
                    ErrorType.WRONG_EMAIL_PASSWORD -> Toast.makeText(context,"Wrong Email Or Password",Toast.LENGTH_SHORT).show()
                    else -> Toast.makeText(context,"Some Internal error occurred",Toast.LENGTH_SHORT).show()
                }
            }
        }
        //TODO If userState is "admin"
    }

    fun getUserRole(userUid: String, onComplete: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            logInformationOnLogcat("Login","Getting User Role")
            val role = sharedDatabase.getUserRole(userUid)
            onComplete(role)
        }
    }

    fun signOut() {
        logInformationOnLogcat("Sign-out","Logging Out")
        firebaseAuth.signOut()
        _currUser.value = null  // Explicitly set the current user to null after sign-out
        _userType.value = ""    // Clear the user type after sign-out
    }


    fun reloadCurrentUser() {
        viewModelScope.launch {
            logInformationOnLogcat("Login","Reloading User")
            sharedDatabase.reloadCurrentUser()
        }
    }

    fun startTimer(durationMillis: Long) {

        logInformationOnLogcat("Timer","Starting Timer")
        remainingMillis = durationMillis
        //remainingMillis = 50*1000
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
                    logInformationOnLogcat("Timer","Timer Completed")
                }
                delay(1000)
                remainingMillis -= 1000
            }
            _isTimerRunning.value = false
            _remainingTime.value = null
            //_reservedCycleUid.value = null  MOVED THIS TO myApp in Dialog if
            //onComplete()
        }
    }
    fun endTimer() {
        logInformationOnLogcat("Timer","Timer Stopped")
        timerJob?.cancel()
        remainingMillis = 0
        _remainingTime.value = "00:00"
        _isTimerRunning.value = false
    }

    fun startLiveIcon() {
        logInformationOnLogcat("Live Icon","Starting")
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
        logInformationOnLogcat("Live Icon","Stopping")
        _showLiveIcon.value = false
        liveIconJob?.cancel() // Cancel the running Job
    }


    override fun onCleared() {
        super.onCleared()
        logInformationOnLogcat("FirebaseAuth","Stopping listener")
        firebaseAuth.removeAuthStateListener { auth ->
            _currUser.value = auth.currentUser
        }
    }
}


