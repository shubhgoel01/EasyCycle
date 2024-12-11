package com.example.easycycle.presentation.viewmodel

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.easycycle.data.model.Schedule
import com.example.easycycle.data.model.SchedulesDataState
import com.example.easycycle.data.model.Student
import com.example.easycycle.data.model.StudentDataState
import com.example.easycycle.data.model.userDataState
import com.example.easycycle.data.remote.StudentFirebaseService
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

    private var _studentDataState = MutableStateFlow(StudentDataState())
    var studentDataState: StateFlow<StudentDataState> = _studentDataState

    private var _userDataState = MutableStateFlow(userDataState())
    var userDataState:StateFlow<userDataState> = _userDataState

    private var _scheduleDataState = MutableStateFlow(SchedulesDataState())
    var scheduleDataState : StateFlow<SchedulesDataState> = _scheduleDataState

    private var _extendedFare = MutableStateFlow<Long>(0)
    var extendedFare : StateFlow<Long> = _extendedFare


    fun updateExtendedFare(value:Long){
        _extendedFare.value = value
    }
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


    fun updateScheduleDataState(newScheduleDataState:SchedulesDataState){
        Log.d("Schedule","Inside updateScheduleDataState")

        _scheduleDataState.value = _scheduleDataState.value.copy(
            isLoading = newScheduleDataState.isLoading,
            error = newScheduleDataState.error,
            schedule = newScheduleDataState.schedule,
            errorMessage =  newScheduleDataState.errorMessage
        )
        Log.d("Schedule Updated Value",_scheduleDataState.value.toString())
    }
    fun fetchSchedule(userUid:String){
        Log.d("schedule","Inside fetchSchedule userViewModel")
        viewModelScope.launch(Dispatchers.IO) {
            studentUseCases.fetchSchedule(userUid){
                updateScheduleDataState(it)
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun createSchedule(userUid:String, schedule: Schedule){
        viewModelScope.launch(Dispatchers.IO) {
            studentUseCases.createSchedule(userUid,schedule){
                _scheduleDataState.value = _scheduleDataState.value.copy(
                    isLoading = false,
                    schedule = schedule
                )
            }
        }
    }
}