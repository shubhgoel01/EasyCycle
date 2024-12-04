package com.example.easycycle.data.remote

import android.util.Log
import com.example.easycycle.data.model.Activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
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

    suspend fun signIn(email:String,password:String):Boolean
    {
        Log.d("SignIn","Trying to sign-in")
        Log.d("Auth value before login",auth.currentUser.toString())
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Log.d("SignIn","Successful")
            Log.d("Auth value",auth.currentUser.toString())
            auth.currentUser?.let { Log.d("Auth value", it.uid) }
            //reloadCurrentUser()

            true
        } catch (e: FirebaseAuthInvalidUserException) {
            Log.e("SignIn", "with email $email not found", e)
            throw e
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Log.e("SignIn", "Invalid password for email $email", e)
            throw e
        } catch (e: Exception) {
            Log.e("SignIn", "Sign-in failed due to unexpected error: ${e.message}", e)
            throw e
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
            Log.d("setUserRole","Successfully set user role")
        }
        catch (e:Exception){
            Log.d("setUserRole","Error occurred while setting user role $e")
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

}