package com.example.easycycle.presentation.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easycycle.data.model.Student
import com.example.easycycle.data.remote.SharedFirebaseService
import com.example.easycycle.domain.usecases.AdminUseCases
import com.example.easycycle.domain.usecases.StudentUseCases
import com.example.easycycle.presentation.ui.components.ComponentSnackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(
    private val studentUseCase : StudentUseCases,
    private val adminUseCases: AdminUseCases,
    private val firebaseAuth:FirebaseAuth,
    private val sharedDatabase : SharedFirebaseService
):ViewModel() {

    private val _currUser = MutableStateFlow(firebaseAuth.currentUser)  //There is a flow in this, the _currUser do not update
    // automatically when user changes, hence we need to manually change the _currUser . This can be changed to autoUpdate
    val currUser: StateFlow<FirebaseUser?> = _currUser

    private val _userType = MutableStateFlow("")
    val userType: StateFlow<String> = _userType

    private var studentLoginData = Student()

    fun updateUserType(updatedUserType:String){
        _userType.value = updatedUserType
        Log.d("User Type updayed", _userType.value)
    }

    private fun updateCurrentUser(){
        _currUser.value = firebaseAuth.currentUser
    }

    fun login(userType:String,loginMethod:String,userId:String,password:String , context : Context){
        var flag:Boolean=false
        Log.d("Login","InsideSharedViewModel")
        if(userType == "User")
        {
            if(loginMethod == "Email")
                studentLoginData.email=userId
            else studentLoginData.registrationNumber=userId
            viewModelScope.launch {
                try {
                    flag = studentUseCase.login(studentLoginData,password){
                        updateCurrentUser()
                    }
                }
                catch (e: FirebaseAuthInvalidCredentialsException){
                    Toast.makeText(context,"Invalid Password",Toast.LENGTH_SHORT).show()
                }
                catch (e: Exception) {
                    Toast.makeText(context,"Unexpected Error Occurred",Toast.LENGTH_SHORT).show()
                }
            }
        }
        else
        {

        }
    }

    fun getUserRole(userUid:String,onComplete:(String)->Unit){
        viewModelScope.launch {
            val role = sharedDatabase.getUserRole(userUid)
            onComplete(role)
        }
    }

    fun signOut(){
        firebaseAuth.signOut()
    }

    fun reloadCurrentUser()
    {
        viewModelScope.launch {
            sharedDatabase.reloadCurrentUser()
        }
    }
}

