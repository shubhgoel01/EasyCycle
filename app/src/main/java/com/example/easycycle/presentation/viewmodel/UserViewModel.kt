package com.example.easycycle.presentation.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easycycle.data.model.Schedule
import com.example.easycycle.data.model.FetchSchedulesDataState
import com.example.easycycle.data.model.ResultState
import com.example.easycycle.data.model.StudentDataState
import com.example.easycycle.data.model.userDataState
import com.example.easycycle.domain.usecases.StudentUseCases
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val studentUseCases: StudentUseCases,
    private val firebaseAuth: FirebaseAuth,
): ViewModel(){

    //To return or cancel the schedule
    private val _returnOrCancelSchedule = MutableStateFlow<ResultState<Schedule>>(ResultState.Loading(false))
    val returnOrCancelSchedule: StateFlow<ResultState<Schedule>> = _returnOrCancelSchedule
    fun updateReturnOrCancelSchedule(it:ResultState<Schedule>){
        Log.d("userViewModel","inside updateReturnOrCancelSchedule")
        _returnOrCancelSchedule.value = it
    }

    private var _studentDataState = MutableStateFlow(StudentDataState())
    var studentDataState: StateFlow<StudentDataState> = _studentDataState
    fun updateStudentDataState(updatedUserDataState : StudentDataState){
        Log.d("Student","Updating student details")
        _studentDataState.value=_studentDataState.value.copy(
            isLoading =updatedUserDataState.isLoading,
            error = updatedUserDataState.error,
            student=updatedUserDataState.student,
            errorMessage =updatedUserDataState.errorMessage
        )
        Log.d("User","Updated student details ${_studentDataState.value}")
    }

    private var _userDataState = MutableStateFlow(userDataState())
    var userDataState:StateFlow<userDataState> = _userDataState

    private var _scheduleDataState = MutableStateFlow(FetchSchedulesDataState())
    var scheduleDataState : StateFlow<FetchSchedulesDataState> = _scheduleDataState
    fun updateScheduleDataState(newScheduleDataState:FetchSchedulesDataState){
        Log.d("updateScheduleDataState","ScheduleDataState Updated")

        _scheduleDataState.value = _scheduleDataState.value.copy(
            isLoading = newScheduleDataState.isLoading,
            error = newScheduleDataState.error,
            schedule = newScheduleDataState.schedule,
            errorMessage =  newScheduleDataState.errorMessage
        )
        Log.d("Schedule Updated Value",_scheduleDataState.value.toString())
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

    fun fetchStudentDetails(registrationNumber:String){
        viewModelScope.launch(Dispatchers.IO) {

            studentUseCases.fetchStudentData(registrationNumber){
                updateStudentDataState(it)
            }
        }
    }

    fun fetchUserDetails(userUid:String){
        Log.d("User","FetchUserDetails ViewModel")
        viewModelScope.launch(Dispatchers.IO) {
            studentUseCases.fetchUserdata(userUid){updatedUserDataState->
                Log.d("User","Updating user details")
                _userDataState.value = _userDataState.value.copy(
                    isLoading = updatedUserDataState.isLoading,
                    error=updatedUserDataState.error,
                    user=updatedUserDataState.user,
                    errorMessage = updatedUserDataState.errorMessage
                )
                Log.d("User","Updated user details ${userDataState.value.user}")
                Log.d("User","Updated user details ${userDataState.value.error}")
                Log.d("User","Updated user details ${userDataState.value.isLoading}")
            }
        }
    }



    fun fetchSchedule(scheduleUid:String){
        Log.d("schedule","Inside fetchSchedule userViewModel")
        viewModelScope.launch(Dispatchers.IO) {
            //First clear any previous data to avoid false information
            updateScheduleDataState(FetchSchedulesDataState())
            studentUseCases.fetchSchedule(scheduleUid){
                updateScheduleDataState(it)
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun createSchedule(schedule: Schedule,onComplete:(ResultState<Schedule>)->Unit){
        //Clear Previous ScheduleDataState to ensure that always updated data is shown
        _scheduleDataState.value = FetchSchedulesDataState()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                studentUseCases.createSchedule(
                    schedule.userUid,
                    schedule,
                    onComplete = { scheduleUid->
                        onComplete(ResultState.Success(schedule.copy(
                            scheduleUid = scheduleUid
                        )))
                        fetchSchedule(scheduleUid)
                    }
                )
            }
            catch (e:Exception){
                onComplete(ResultState.Error("Some Error Occurred While creating schedule $e"))
                Log.d("createSchedule","UserViewModel Some error Occurred somewhere")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun returnOrCancelRide(schedule:Schedule){

        //Now if success then simply move to the home screen
        _returnOrCancelSchedule.value = ResultState.Loading(true)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                studentUseCases.returnOrCancelRide(
                    schedule,
                    onComplete = {
                        Log.d("returnOrCancelRide","OnComplete Called")
                        _scheduleDataState.value = FetchSchedulesDataState(isLoading = false)
                        _returnOrCancelSchedule.value = ResultState.Success(null)
                        Log.d("returnOrCancelRide","updated _returnOrCancelSchedule")
                    }
                )
            }
            catch (e:Exception){
                Log.d("returnOrCancelRide","Could Not complete the process")
                Log.d("returnOrCancelRide","$e")
                _returnOrCancelSchedule.value = ResultState.Error(e.toString())
            }
        }
    }
}