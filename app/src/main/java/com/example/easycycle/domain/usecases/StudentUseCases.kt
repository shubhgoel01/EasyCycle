package com.example.easycycle.domain.usecases

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.easycycle.data.model.Student
import com.example.easycycle.data.model.StudentDataState
import com.example.easycycle.data.model.User
import com.example.easycycle.data.model.userDataState
import com.example.easycycle.data.remote.SharedFirebaseService
import com.example.easycycle.data.remote.StudentFirebaseService
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import javax.inject.Inject

class StudentUseCases @Inject constructor(
    private val studentDatabase: StudentFirebaseService,
    private val sharedDatabase : SharedFirebaseService
) {
    suspend fun login (studentLoginData:Student,password:String,onComplete:()->Unit):Boolean
    {
        Log.d("Login","Inside StudentUseCases")
        var flag = false
        flag = studentDatabase.studentExist(studentLoginData)

        if(!flag)
        {
            Log.d("LogIn","Student is Not authorized to use app")
            return flag
        }

        Log.d("LogIn","Student is authorized to use app")

        if(studentLoginData.email == "")
        {
            val tempStudent = studentDatabase.getEmailFromStudentRegistration(studentLoginData.registrationNumber)
            studentLoginData.email = tempStudent.email
            studentLoginData.isRegistered = tempStudent.isRegistered
        }

        if(studentLoginData.registrationNumber=="")
        {
            val tempStudent:Student = studentDatabase.getUserRegistrationNumberFromEmail(studentLoginData.email)
            studentLoginData.registrationNumber= tempStudent.registrationNumber
            studentLoginData.isRegistered = tempStudent.isRegistered
        }

        if(studentLoginData.isRegistered == true){
            try {
                sharedDatabase.signIn(studentLoginData.email,password)
                onComplete()
                Log.d("LoginIn","UserLogged In successfully")
            }
            catch (e:Exception)
            {
                throw e
            }
        }
        else {
            try {
                val userUid = studentDatabase.register(studentLoginData.email, password)
                studentDatabase.addUser(User(registrationNumber = studentLoginData.registrationNumber),userUid!!)

                sharedDatabase.setUserRole(userUid,"User")
                onComplete()
                Log.d("Registration","Successfully created new user and added all fields in database")
            }
            catch (e:Exception){
                throw e
            }
        }

        return flag
    }

    suspend fun addStudentExecute(context: Context, student: Student){
        //Check Student Details Are Valid Or Not
        if(
            student.name=="" ||
            student.email=="" ||
            student.branch=="" ||
            student.registrationNumber=="" ||
            student.phone=="" ||
            student.phone.length!=10 ||
            !isValidEmail(student.email))
        {
            Toast.makeText(context,"Incomplete user details or invalid email", Toast.LENGTH_SHORT).show()
        }
        if(!studentDatabase.studentExist(student)){
            studentDatabase.addStudent(student)
        }
        else{
            Toast.makeText(context, "R.No. or email already exist", Toast.LENGTH_SHORT).show()
        }
    }

    suspend fun fetchStudentData(registrationNumber:String,onComplete:(updatedStudentDataState:StudentDataState)->Unit){
        studentDatabase.fetchStudentData(registrationNumber,onComplete)
    }

    suspend fun fetchUserdata(userUid:String , onComplete:(updatedUserDataState : userDataState)->Unit){
        Log.d("User","FetchUserDetails UseCases")
        studentDatabase.fetchUserDetails(userUid, onComplete)
    }
}