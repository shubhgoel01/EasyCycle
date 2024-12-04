package com.example.easycycle.presentation.viewmodel

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easycycle.data.model.Student
import com.example.easycycle.data.model.StudentDataState
import com.example.easycycle.data.model.userDataState
import com.example.easycycle.data.remote.StudentFirebaseService
import com.example.easycycle.domain.usecases.StudentUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val studentUseCases: StudentUseCases
): ViewModel(){

    private var _studentDataState = MutableStateFlow(StudentDataState())
    var studentDataState: StateFlow<StudentDataState> = _studentDataState

    private var _userDataState = MutableStateFlow(userDataState())
    var userDataState:StateFlow<userDataState> = _userDataState

    fun updateStudentDataState(updatedUserDataState : StudentDataState){
        Log.d("Student","Updating student details")
        _studentDataState.value=_studentDataState.value.copy(
            isLoading =updatedUserDataState.isLoading,
            error = updatedUserDataState.error,
            student=updatedUserDataState.student,
            errorMessage =updatedUserDataState.errorMessage
        )
        Log.d("User","Updated student details ${studentDataState.value}")
    }

    fun fetchStudentDetails(registrationNumber:String){
        viewModelScope.launch {

            studentUseCases.fetchStudentData(registrationNumber){
                updateStudentDataState(it)
            }
        }
    }

    fun fetchUserDetails(userUid:String){
        Log.d("User","FetchUserDetails ViewModel")
        viewModelScope.launch {
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
}