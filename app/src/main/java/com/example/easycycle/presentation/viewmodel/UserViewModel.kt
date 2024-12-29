package com.example.easycycle.presentation.viewmodel

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easycycle.data.Enum.ErrorType
import com.example.easycycle.data.model.AppErrorException
import com.example.easycycle.data.model.Schedule
import com.example.easycycle.data.model.ResultState
import com.example.easycycle.data.model.User
import com.example.easycycle.data.remote.Profile
import com.example.easycycle.domain.usecases.StudentUseCases
import com.example.easycycle.logErrorOnLogcat
import com.example.easycycle.logInformationOnLogcat
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val studentUseCases: StudentUseCases,
    private val firebaseAuth: FirebaseAuth,
): ViewModel(){

    //To return or cancel the schedule
    private val _returnOrCancelSchedule = MutableStateFlow<ResultState<Schedule>>(ResultState.Loading(false))
    val returnOrCancelSchedule: StateFlow<ResultState<Schedule>> = _returnOrCancelSchedule
    fun updateReturnOrCancelSchedule(value:ResultState<Schedule>){
        _returnOrCancelSchedule.value = value
    }

    private var _userDataState = MutableStateFlow<ResultState<User>>(ResultState.Loading(false))
    var userDataState:StateFlow<ResultState<User>> = _userDataState
    fun updateUserDataState(value:ResultState<User>){
        _userDataState.value = value
    }

    private var _scheduleDataState = MutableStateFlow<ResultState<Schedule>>(ResultState.Loading(false))
    var scheduleDataState : StateFlow<ResultState<Schedule>> = _scheduleDataState
    fun updateScheduleDataState(value:ResultState<Schedule>){
        _scheduleDataState.value = value
    }

    private var _extendedFare = MutableStateFlow<Long>(0)
    var extendedFare : StateFlow<Long> = _extendedFare
    fun updateExtendedFare(value:Long){
        _extendedFare.value = value
    }

    private val _createScheduleState = MutableStateFlow<ResultState<Schedule>>(ResultState.Loading())
    val createScheduleState: StateFlow<ResultState<Schedule>> = _createScheduleState
    fun updateCreateScheduleState(value:ResultState<Schedule>){
        _createScheduleState.value = value
    }

    fun fetchStudentDetails(context:Context,registrationNumber:String, onComplete:(ResultState<Profile>)->Unit, onError:(ResultState<Profile>)->Unit){
        viewModelScope.launch(Dispatchers.IO) {
            try {
                logInformationOnLogcat("Profile","Fetching Student Details")
                studentUseCases.fetchStudentData(registrationNumber){
                    val data = (it as ResultState.Success).data!!
                    onComplete(ResultState.Success(Profile(
                        registrationNumber = data.registrationNumber,
                        name = data.name,
                        email = data.email,
                        branch = data.branch,
                        imageURL = data.imageURL,
                        phone = data.phone,
                        loginTimeStamp = data.registrationTimeStamp,
                        userType = "user"
                    )))
                }
            }
            catch (e:AppErrorException){
                logErrorOnLogcat("Profile",e)
                onError(ResultState.Error(e))
                when(e.type){
                    ErrorType.DATA_FETCHED_IS_NULL -> Toast.makeText(context,"Data Fetched Is Null",Toast.LENGTH_SHORT).show()
                    ErrorType.DATA_NOT_FOUND -> {
                        //TODO HERE LOG OUT THE USER
                        Toast.makeText(context, "Your Data Not Found", Toast.LENGTH_SHORT).show()
                    }
                    ErrorType.UNEXPECTED_ERROR -> Toast.makeText(context, "Some Unexpected Error Occurred", Toast.LENGTH_SHORT).show()
                    else -> {}
                }
            }
        }
    }

    fun fetchUserDetails(userUid:String, context : Context){
        viewModelScope.launch(Dispatchers.IO) {
            try {
                logInformationOnLogcat("UserDetails","Fetching User Details")
                studentUseCases.fetchUserdata(userUid){value->
                    _userDataState.value = value
                }
            }
            catch (e:AppErrorException){
                logErrorOnLogcat("UserDetails",e)
                when(e.type){
                    ErrorType.DATA_NOT_FOUND -> {
                        withContext(Dispatchers.Main){
                            Toast.makeText(context,"User Not Found",Toast.LENGTH_SHORT).show()
                        }
                    }
                    ErrorType.UNEXPECTED_ERROR ->{
                        withContext(Dispatchers.Main){
                            Toast.makeText(context,"Some Internal Error Occurred",Toast.LENGTH_SHORT).show()
                        }
                    }
                    else ->{}
                }
                _userDataState.value = ResultState.Error(e)
            }
        }
    }

    fun fetchSchedule(context:Context, scheduleUid:String){
        try {
            logInformationOnLogcat("Schedule","Fetching Schedule")
            viewModelScope.launch(Dispatchers.IO) {
                studentUseCases.fetchSchedule(scheduleUid){
                    updateScheduleDataState(it)
                }
            }
        }
        catch (e:AppErrorException){
            logErrorOnLogcat("Schedule",e)
            when(e.type){
                ErrorType.DATA_FETCHED_IS_NULL -> {
                    Toast.makeText(context,"Error While Fetching Data",Toast.LENGTH_SHORT).show()
                    updateScheduleDataState(ResultState.Error(e))
                }
                ErrorType.DATA_NOT_FOUND ->{
                    //TODO MOVE TO ERROR SCREEN
                    Toast.makeText(context,"No Booked Schedule",Toast.LENGTH_SHORT).show()
                    updateScheduleDataState(ResultState.Loading(false))
                }
                else -> {
                    //Move To HomeScreen
                    Toast.makeText(context,"Session Expired",Toast.LENGTH_SHORT).show()
                    updateScheduleDataState(ResultState.Error(e))
                }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun createSchedule(context:Context,schedule: Schedule,onComplete:(ResultState<Schedule>)->Unit){
        viewModelScope.launch(Dispatchers.IO) {
            try {
                logInformationOnLogcat("Schedule","Creating Schedule")
                studentUseCases.createSchedule(
                    schedule.userUid,
                    schedule,
                    onComplete = { scheduleUid->
                        onComplete(ResultState.Success(schedule.copy(
                            scheduleUid = scheduleUid
                        )))
                        //fetchSchedule(context,scheduleUid)
                        //Once the schedule is booked, ensure userData is updated to reflect the scheduleId
                        when(val state = _userDataState.value){
                            is ResultState.Success ->{
                                updateUserDataState(ResultState.Success(state.data!!.copy(scheduleId = schedule.scheduleUid)))
                            }
                            else -> {}
                        }
                        // Update ScheduleDataState so that when moving to the Home Screen, system tells the screen to fetch the schedule if any
                        //Using this way we do not need to explicitly-fetch the schedule or call the function
                        updateScheduleDataState(ResultState.Loading(false))
                    }
                )
            }
            catch (e:AppErrorException){
                logErrorOnLogcat("bookSchedule",e)
                Toast.makeText(context,"Some Unexpected Error Occurred",Toast.LENGTH_SHORT).show()
                _createScheduleState.value = ResultState.Error(e)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun returnOrCancelRide(schedule:Schedule,resetReserveCycleState:()->Unit){

        viewModelScope.launch(Dispatchers.IO) {
            try {
                logInformationOnLogcat("Schedule","Returning Or Cancelling Schedule")
                studentUseCases.returnOrCancelRide(
                    schedule,
                    onComplete = {
                        //Reset scheduleDataState to when navigating to home screen it tells the screen to fetch the schedule detials
                        _scheduleDataState.value = ResultState.Loading(false)
                        //Update the scheduleId in userDataState so that it shows that no schedule is booked for now
                        when(val state = _userDataState.value)
                        {
                             is ResultState.Success -> {
                                 updateUserDataState(ResultState.Success(state.data!!.copy(scheduleId = "")))
                             }
                            else -> {}
                        }
                        resetReserveCycleState()
                        //This Is Necessary in case if user Returns the cycle and immediately wishes to book another ride
                        _createScheduleState.value = ResultState.Loading(false)
                        //To mark that return is successful
                        _returnOrCancelSchedule.value = ResultState.Success(null)

                        //All these steps ensures the data is updated acc to the current state
                    }
                )
            }
            catch (e:AppErrorException){
                logErrorOnLogcat("returnOrCancelRide",e)
                _returnOrCancelSchedule.value = ResultState.Error(e)
            }
        }
    }

}