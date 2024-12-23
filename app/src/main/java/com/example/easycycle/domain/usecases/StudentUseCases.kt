package com.example.easycycle.domain.usecases

import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.easycycle.data.model.Schedule
import com.example.easycycle.data.model.FetchSchedulesDataState
import com.example.easycycle.data.model.Student
import com.example.easycycle.data.model.StudentDataState
import com.example.easycycle.data.model.User
import com.example.easycycle.data.model.userDataState
import com.example.easycycle.data.remote.CycleFirebaseService
import com.example.easycycle.data.remote.SharedFirebaseService
import com.example.easycycle.data.remote.StudentFirebaseService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class StudentUseCases @Inject constructor(
    private val studentDatabase: StudentFirebaseService,
    private val sharedDatabase : SharedFirebaseService,
    private val cycleDatabase : CycleFirebaseService,
    @ApplicationContext private val context: Context
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


//Can be simplified I can directly pass schedule id in some cases like in myApp
    suspend fun fetchSchedule(scheduleUid: String, onComplete: (FetchSchedulesDataState) -> Unit) {
        Log.d("studentUseCases", "Fetching Schedules Data")

        try {
            studentDatabase.fetchSchedule(scheduleUid){ schedule->
                if(schedule == null) {
                    Log.e("fetchSchedule","Student UseCases : Schedule Fetched is null or update got cancelled")
                    onComplete(
                        FetchSchedulesDataState(
                            isLoading = false,
                            error = true,
                            errorMessage = "Schedule Fetched Is Null"
                        )
                    )
                }
                else{
                    Log.d("fetchSchedule","StudentUseCases : Schedule Fetched Successfully")
                    Log.d("fetchSchedule","Schedule data : $schedule")
                    onComplete(
                        FetchSchedulesDataState(
                            isLoading = false,
                            schedule = schedule
                        )
                    )
                }
            }

        } catch (e: Exception) {
            Log.d("studentUseCases", "Error occurred when fetching schedule: ${e.message}")

            onComplete(FetchSchedulesDataState(
                isLoading = false,
                error = true,
                errorMessage = "An error occurred while fetching the schedule: ${e.message}"
            ))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun createSchedule(userUid:String, schedule: Schedule, onComplete:(String)->Unit){
        //TODO later move all these functions to a batchFunction
        var info : Pair<String, Long>? = null
        //Info contains two things {scheduleUid, scheduleStartTime}
        try {
            info = studentDatabase.createSchedule(userUid, schedule)
            studentDatabase.bookSchedule(userUid,info.first)
            cycleDatabase.bookSchedule(info.first,schedule.cycleUid,schedule.estimateTime+schedule.startTime)
            studentDatabase.scheduleStartCheck(userUid,info.first,info.second,context, schedule.cycleUid)
            Log.d("createSchedule","All functions completed successfully")
            //If any function fails then whole process should be reverted, hence better use batchFunctions something like transactions
            onComplete(info.first)
        }
        catch (e:Exception){
            Log.e("createSchedule","Failed to create schedule")
            throw e
        }
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
        //First Update user
        //Update cycleField Booked = false according to me no need to update other fields like, scheduleUid, nextAvailableTime in cycle as these will be used only if the cycle is booked
        //Once this is done Remove the scheduleListener and set schedule to null and move to home screen, and set the worker
        //In worker - Create a new node in ScheduleHistory, update cycleBookingHistory, deleteSchedule from Schedule Node, update user BookingHistory
        studentDatabase.updateUserScheduleId(schedule.userUid,"")
        cycleDatabase.updateCycleBooked(schedule.cycleUid,false)
        studentDatabase.removeScheduleListener(schedule.scheduleUid)
        studentDatabase.returnOrCancelScheduleWorkerStart(schedule,context)
        Log.d("returnOrCancelRide","All functions completed")
        onComplete()
    }
}