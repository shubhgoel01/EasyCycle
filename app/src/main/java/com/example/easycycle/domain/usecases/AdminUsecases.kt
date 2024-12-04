package com.example.easycycle.domain.usecases

import com.example.easycycle.data.model.AllAdmins
import com.example.easycycle.data.remote.AdminFirebaseService
import com.example.easycycle.data.remote.SharedFirebaseService
import javax.inject.Inject

class AdminUseCases @Inject constructor(
    private val adminDatabase: AdminFirebaseService,
    private val sharedDatabase : SharedFirebaseService
)
{
    suspend fun login(loginMethod:String,adminId:String,password:String):Boolean
    {
        var flag:Boolean=false
        if(loginMethod=="Email")
            flag = adminDatabase.adminExist(AllAdmins(email=adminId))
        else    flag = adminDatabase.adminExist(AllAdmins(adminId = adminId))

        if(flag == false)
                return flag

        var email=adminId
        if(loginMethod!="Email")
            email = adminDatabase.getEmailFromAdminID(adminId)
        flag = sharedDatabase.signIn(email,password)

        return flag
    }
}

fun isValidEmail(email: String): Boolean {
    val emailPattern = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    val regex = Regex(emailPattern)
    return regex.matches(email)
}
