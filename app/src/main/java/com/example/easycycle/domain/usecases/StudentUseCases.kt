package com.example.easycycle.domain.usecases

import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.easycycle.data.model.Schedule
import com.example.easycycle.data.model.ResultState
import com.example.easycycle.data.model.Student
import com.example.easycycle.data.model.User
import com.example.easycycle.data.remote.CycleFirebaseService
import com.example.easycycle.data.remote.SharedFirebaseService
import com.example.easycycle.data.remote.StudentFirebaseService
import com.example.easycycle.isValidEmail
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class StudentUseCases @Inject constructor(
    private val studentDatabase: StudentFirebaseService,
    private val sharedDatabase : SharedFirebaseService,
    private val cycleDatabase : CycleFirebaseService,
    @ApplicationContext private val context: Context
) {
    suspend fun login (studentLoginData:Student,password:String,onComplete:(String)->Unit)
    {
        val flag : Boolean = studentDatabase.studentExist(studentLoginData)
        // If flag is false, the  automatically throws error

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

        if(studentLoginData.isRegistered){
            sharedDatabase.signIn(studentLoginData.email,password)
            onComplete(studentLoginData.registrationNumber)
        }
        else {
            val userUid = studentDatabase.register(studentLoginData.email, password)
            studentDatabase.addUser(User(registrationNumber = studentLoginData.registrationNumber),userUid)

            sharedDatabase.setUserRole(userUid,"User")
            onComplete(studentLoginData.registrationNumber)
        }
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

    suspend fun fetchStudentData(registrationNumber:String,onComplete:(updatedStudentDataState: ResultState<Student>)->Unit){
        studentDatabase.fetchStudentData(registrationNumber,onComplete)
    }

    suspend fun fetchUserdata(userUid:String , onComplete:(updatedUserDataState : ResultState.Success<User>)->Unit){
        studentDatabase.fetchUserDetails(userUid, onComplete)
    }


//Can be simplified I can directly pass schedule id in some cases like in myApp
    suspend fun fetchSchedule(scheduleUid: String, onComplete: (ResultState<Schedule>) -> Unit) {
        Log.d("studentUseCases", "Fetching Schedules Data")
        studentDatabase.fetchSchedule(scheduleUid){
            onComplete(ResultState.Success(it))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun createSchedule(userUid:String, schedule: Schedule, onComplete:(String)->Unit){

        val info : Pair<String, Long>?
        info = studentDatabase.createSchedule(schedule)
        studentDatabase.bookSchedule(userUid,info.first)
        cycleDatabase.bookSchedule(info.first,schedule.cycleUid,schedule.estimateTime+schedule.startTime)
        studentDatabase.scheduleStartCheck(userUid,info.first,info.second,context, schedule.cycleUid)

        onComplete(info.first)
    }

    suspend fun startTimer(userUid:String,cycleUid:String){
        try {
            Log.d("startTimer","Callong function")
            studentDatabase.startTimer(userUid,cycleUid)
        }catch (e:Exception){
            Log.d("startTimer StudentUseCase","Error Occurred $e")
            throw e
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun returnOrCancelRide(schedule:Schedule,onComplete:()->Unit){
        studentDatabase.updateUserScheduleId(schedule.userUid,"")
        cycleDatabase.updateCycleBooked(schedule.cycleUid,false)
        studentDatabase.removeScheduleListener(schedule.scheduleUid)
        studentDatabase.returnOrCancelScheduleWorkerStart(schedule,context)

        onComplete()
    }
}