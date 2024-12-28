package com.example.easycycle.data.remote

import android.util.Log
import com.example.easycycle.data.Enum.ErrorType
import com.example.easycycle.data.model.Activity
import com.example.easycycle.data.model.AppErrorException
import com.example.easycycle.data.model.Schedule
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


//Functions like login, register, create new activity
class SharedFirebaseService @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase,
    private val auth: FirebaseAuth
){
    private var database = firebaseDatabase.reference
    val firebase=Firebase

    suspend fun newActivity(activity: Activity):String?
    {
        val newActivityRef=database.child("Activity").push()
        try {
            newActivityRef.setValue(activity).await()
        }
        catch (e:Exception)
        {
            throw e
        }
        return newActivityRef.key
    }

    suspend fun signIn(email:String,password:String)
    {
        try {
            auth.signInWithEmailAndPassword(email, password).await()
            auth.currentUser?.let { Log.d("Auth value", it.uid) }
        }
        catch (e: FirebaseAuthInvalidUserException) {
            Log.e("SignIn", "with email $email not found", e)
            throw AppErrorException( ErrorType.DATA_NOT_FOUND, "signIn SharedFirebaseService","$e" )
        }
        catch (e: FirebaseAuthInvalidCredentialsException) {
            throw AppErrorException( ErrorType.WRONG_EMAIL_PASSWORD, "signIn SharedFirebaseService","$e" )
        }
        catch (e: Exception) {
            throw AppErrorException( ErrorType.UNEXPECTED_ERROR, "signIn SharedFirebaseService","$e" )
        }
    }

    suspend fun getUserRole(userUid:String):String{
        var role:String?=null
        try {
            val snapshot = database.child("userToRoleMap").child(userUid).get().await()
            if(snapshot.exists()){
                role = snapshot.getValue(String::class.java)
            }
            else {
                Log.e("getUserRole","User role not found")
            }

            if(role == null)
                    throw Exception("User role not found or null")
        }
        catch (e:Exception){
            Log.d("getUserRole","Error occurred while getting user role $e")
            throw e
        }

        return role
    }

    suspend fun setUserRole(userUid:String,role:String){
        val userToRoleMapRef = database.child("userToRoleMap").child(userUid)
        try {
            userToRoleMapRef.setValue(role).await()
        }
        catch (e:Exception){
            throw AppErrorException(ErrorType.UNEXPECTED_ERROR,"setUserRole SharedFirebaseService","$e")
        }
    }

    suspend fun reloadCurrentUser() {
        val auth = FirebaseAuth.getInstance()
        try {
            auth.currentUser?.reload()?.await()
            Log.d("Authentication", "Reloaded user authentication successfully")
        } catch (e: Exception) {
            Log.e("Authentication", "Error occurred while reloading user authentication", e)
            throw e // Optionally re-throw or handle the error here
        }
    }

    suspend fun createNewScheduleHistory(schedule: Schedule){
        try{
            val ref = database.child("ScheduleHistory")
            val snapshot = ref.child(schedule.scheduleUid)
            snapshot.setValue(schedule).await()
            Log.d("worker2","Correctly Updated schedule history")
        }
        catch (e: FirebaseNetworkException) {
            throw e // Rethrow network-related exceptions for retry
        }
        catch (e:Exception){
            Log.e("worker2","Error Occurred when updating schedule history")
            Log.e("worker2","$e")
            throw e
        }
    }

    suspend fun removeSchedule(scheduleUid:String){
        val snapshot = database.child("Schedule").child(scheduleUid)
        snapshot.removeValue()
            .addOnSuccessListener {
                Log.d("Firebase", "Schedule deleted successfully")
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Failed to delete schedule", e)
                throw e
            }
    }

}