package com.example.easycycle.data.remote

import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.easycycle.data.model.Student
import com.example.easycycle.data.model.StudentDataState
import com.example.easycycle.data.model.User
import com.example.easycycle.data.model.userDataState
import com.example.easycycle.presentation.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


class StudentFirebaseService @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase,
    private val auth: FirebaseAuth,
    private val firebaseStorage: FirebaseStorage,
){
    private var database = firebaseDatabase.reference
    private val studentsRef=database.child("Students")
    private val userRef = database.child("User")

    suspend fun addStudent(student:Student)
    {
        val studentRef = studentsRef.child(student.registrationNumber)
        try {
            studentRef.setValue(student).await()
            Log.d("Admin", "Student added successfully")
        } catch (e: Exception) {
            Log.d("Admin", "Failed to add student data: ${e.message}")
            throw e
        }
    }


    suspend fun studentExist(student:Student):Boolean   //Return true if user already exist otherwise false , if error occurs then re-throws the error
    {
        Log.d("Login","Checking If student Exist")
        var snapshot: DataSnapshot? =null
        try {
             if(student.registrationNumber!="")
                snapshot = studentsRef.orderByChild("registrationNumber").equalTo(student.registrationNumber).get().await()
            else
                snapshot = studentsRef.orderByChild("email").equalTo(student.email).get().await()

            if(snapshot!=null && snapshot.exists())
            {
                Log.d("Login","Student exists - Checked Successfully")
                return true
            }
        }
        catch (e:Exception){
            Log.d("Login","Error Occurred")
            Log.d("Login","Student exists - Checking Failed")
            throw e
        }
        Log.d("Login","Student does not exist")
        return false
    }


    suspend fun fetchStudentData(registrationNumber:String,onComplete:(updatedStudentDataState:StudentDataState)->Unit)  //by registration number or email
    {
        val currStudent: Student?
        var updatedStudentDataState = StudentDataState()

        try {
            var snapshot = studentsRef.child(registrationNumber).get().await()
            currStudent= snapshot.getValue(Student::class.java)

            if(currStudent!=null){
                updatedStudentDataState=updatedStudentDataState.copy(
                    isLoading = false,
                    error = false,
                    student = currStudent
                )
                if(!currStudent.isRegistered){
                    val snapshot2 = studentsRef.child(registrationNumber).child("isRegistered")
                    try {
                        snapshot2.setValue(true).await()
                    }
                    catch (e:Exception){
                        Log.d("Student","error while setting isRegistered field")
                        throw e
                    }
                }
            }

            else updatedStudentDataState=updatedStudentDataState.copy(
                isLoading = false,
                error = true,
               errorMessage = "Student Data Is Empty"
            )
            Log.d("Student","Details fetched successfully")
        }
        catch (e:Exception){
            updatedStudentDataState=updatedStudentDataState.copy(
                isLoading = false,
                error = true,
                errorMessage = "Error Occurred while fetching student data $e"
            )
            Log.d("Student","error while fetching data")
        }
        onComplete(updatedStudentDataState)
    }

    suspend fun getEmailFromStudentRegistration(registrationNumber:String):Student{
        Log.d("studentFirebaseService","Inside getEmailFromStudentRegistration")
        val student = Student()
        try {
            var snapshot=studentsRef.orderByChild("registrationNumber").equalTo(registrationNumber).get().await()
            snapshot=snapshot.children.first()
            student.email =  snapshot.child("email").getValue(String::class.java)!!
            student.isRegistered = snapshot.child("isRegistered").getValue(Boolean::class.java)!!

            return student
        }
        catch (e:Exception)
        {
            Log.d("studentFirebaseService","Error Occurred")
            throw e
        }
    }

    suspend fun register(email: String, password: String): String {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid
            // Registration successful
            Log.d("Register", "New User registered with UID: $uid")

            if (uid == null) {
                Log.d("Register", "UID returned by Firebase Authentication is null")
                throw Exception("UID returned by Firebase Authentication is null")
            }

            uid
        } catch (e: FirebaseAuthWeakPasswordException) {
            Log.e("Register", "Weak password: ${e.reason}", e)
            throw e
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Log.e("Register", "Invalid email format: ${e.message}", e)
            throw e
        } catch (e: FirebaseAuthUserCollisionException) {
            Log.e("Register", "Email already in use: ${e.message}", e)
            throw e
        } catch (e: Exception) {
            Log.e("Register", "Unexpected error occurred during registration", e)
            throw e
        }
    }


    suspend fun fetchUserDetails(userUid:String , onComplete:(userDataState:userDataState)->Unit){
        Log.d("User","FetchUserDetails fetchUserDetails")
        var userDataState: userDataState
        try {
            val snapshot = userRef.child(userUid).get().await()
            if(!snapshot.exists())
            {
                userDataState=userDataState(
                    isLoading = false,
                    error = true,
                    errorMessage = "User does not exist in database"
                )
            }
            val user: User? = snapshot.getValue(User::class.java)
            userDataState = if(user == null) {
                userDataState(
                    isLoading = false,
                    error = true,
                    errorMessage = "User is null in database"
                )
            } else{
                userDataState(
                    isLoading = false,
                    error = false,
                    user = user
                )
            }
            Log.d("User","Details fetched successfully $user")
        }
        catch (e:Exception)
        {
            userDataState=userDataState(
                isLoading = false,
                error = true,
                errorMessage = "Error Occurred during fetching user details $e"
            )
            Log.d("User","error while fetching details")
        }
        onComplete(userDataState)
    }

    suspend fun getUserRegistrationNumberFromEmail(email:String):Student{
        Log.d("studentFirebaseService","Inside getUserRegistrationNumberFromEmail")
        val student = Student()
        try {
            var snapshot=studentsRef.orderByChild("email").equalTo(email).get().await()
            snapshot=snapshot.children.first()

            Log.d("getUserRegistrationNumberFromEmail","User registrationNumber fetched successfully")
            student.registrationNumber= snapshot.child("registrationNumber").getValue(String::class.java)!!
            student.isRegistered= snapshot.child("isRegistered").getValue(Boolean::class.java)!!
            return student
        }
        catch (e:Exception)
        {
            Log.d("getUserRegistrationNumberFromEmail","error occurred")
            throw e
        }
    }

    suspend fun addUser(user:User,userUid:String){
        val snapshot = userRef.child(userUid)
        try {
            snapshot.setValue(user).await()
            Log.e("addUser","new user added successfully")
        }
        catch (e:Exception){
            Log.e("addUser","Error while adding new user")
            throw e
        }
    }
}