package com.example.easycycle.data.remote

import android.util.Log
import com.example.easycycle.data.model.AllAdmins
import com.example.easycycle.domain.usecases.StudentUseCases
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AdminFirebaseService @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase
) {
    private var database = firebaseDatabase.reference
    private val adminRef=database.child("AllAdmins")

    suspend fun fetchAdmin(admin: AllAdmins): AllAdmins  //by registration number or email
    {
        val currAdmin: AllAdmins
        var snapshot: DataSnapshot

        try {
            snapshot = if(admin.adminId!="")
                adminRef.orderByChild("adminId").equalTo(admin.adminId).get().await()
            else adminRef.orderByChild("email").equalTo(admin.email).get().await()

            snapshot=snapshot.children.first()
            currAdmin=snapshot.getValue(AllAdmins::class.java)!!
        }
        catch (e:Exception)
        {
            throw e
        }
        return currAdmin
    }

    suspend fun adminExist(admin:AllAdmins):Boolean   //Return true if user already exist otherwise false , if error occurs then re-throws the error
    {
        val snapshot: DataSnapshot
        try {
            snapshot = if(admin.adminId!="")
                adminRef.orderByChild("adminId").equalTo(admin.adminId).get().await()
            else adminRef.orderByChild("email").equalTo(admin.email).get().await()

            if(snapshot.exists())
                return true
        }
        catch (e:Exception){
            throw e
        }
        Log.d("Login","Admin does not exist")
        return false
    }


    suspend fun getEmailFromAdminID(adminId:String):String{
        try {
            var snapshot=adminRef.orderByChild("adminId").equalTo(adminId).get().await()
            snapshot=snapshot.children.first()
            return snapshot.child("email").getValue(String::class.java)!!
        }
        catch (e:Exception)
        {
            throw e
        }
    }
}