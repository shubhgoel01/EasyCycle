package com.example.easycycle.data.remote

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.easycycle.Worker.CycleStateWorker
import com.example.easycycle.Worker.ReturnOrCancelWorker
import com.example.easycycle.data.Enum.ErrorType
import com.example.easycycle.data.Enum.ScheduleState
import com.example.easycycle.data.model.AppErrorException
import com.example.easycycle.data.model.ResultState
import com.example.easycycle.data.model.Schedule
import com.example.easycycle.data.model.Student
import com.example.easycycle.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import kotlinx.coroutines.tasks.await
import java.time.Duration
import java.util.concurrent.TimeUnit
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
        } catch (e: Exception) {
            Log.e("Admin", "Failed to add student data: ${e.message}")
            throw AppErrorException(ErrorType.UNEXPECTED_ERROR,"addStudent StudentFirebaseService","$e")
        }
    }


    suspend fun studentExist(student:Student):Boolean   //Return true if user already exist otherwise false , if error occurs then re-throws the error
    {
        val snapshot: DataSnapshot?
        try {
            snapshot = if(student.registrationNumber!="")
                studentsRef.orderByChild("registrationNumber").equalTo(student.registrationNumber).get().await()
            else
                studentsRef.orderByChild("email").equalTo(student.email).get().await()

            if(snapshot!=null && snapshot.exists())
                return true
            else throw AppErrorException(ErrorType.STUDENT_NOT_AUTHORIZED,"studentExist StudentFirebaseExist","User Not Found")
            // IF USER IS NOT AUTHORIZED TO USE APP, SIMPLY THROW ERROR
        }
        catch (e:Exception){
            Log.d("Login","Error Occurred $e")
            throw AppErrorException(ErrorType.UNEXPECTED_ERROR,"studentExist StudentFirebaseExist","$e")
        }
    }


    suspend fun fetchStudentData(registrationNumber:String,onComplete:(updatedStudentDataState: ResultState<Student>)->Unit)  //by registration number or email
    {
        val currStudent: Student?
        var updatedStudentDataState : ResultState<Student> = ResultState.Loading(true)

        try {
            val snapshot = studentsRef.child(registrationNumber).get().await()
            if(!snapshot.exists())
                throw AppErrorException(ErrorType.DATA_NOT_FOUND,"fetchStudentData StudentFirebaseService","Snapshot Does Not Exist")
            currStudent= snapshot.getValue(Student::class.java)

            if(currStudent!=null){
                updatedStudentDataState = ResultState.Success(currStudent)
                if(!currStudent.isRegistered){
                    val snapshot2 = studentsRef.child(registrationNumber).child("isRegistered")
                    try {
                        snapshot2.setValue(true).await()
                    }
                    catch (e:Exception){
                        Log.d("Student","error while setting isRegistered field")
                        //TODO Handle This Error Accordingly This Error Is Not Much Important
                    }
                }
                onComplete(updatedStudentDataState)
            }
            else throw AppErrorException(ErrorType.DATA_FETCHED_IS_NULL,"fetchStudentData StudentFirebaseService","Data Not Found")
        }
        catch (e:Exception){
            Log.d("Student","error while fetching data $e")
            throw AppErrorException(ErrorType.UNEXPECTED_ERROR,"fetchStudentData StudentFirebaseService","$e")
        }
    }

    suspend fun getEmailFromStudentRegistration(registrationNumber:String):Student{
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
            Log.d("studentFirebaseService","Error Occurred $e")
            throw AppErrorException(ErrorType.UNEXPECTED_ERROR,"getEmailFromStudentRegistration StudentFirebaseService","$e")
        }
    }

    suspend fun register(email: String, password: String): String {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid

            if (uid == null) {
                Log.d("Register", "UID returned by Firebase Authentication is null")
                throw AppErrorException(ErrorType.DATA_FETCHED_IS_NULL,"register StudentFirebaseService","UID returned by Firebase Authentication is null")
            }
            uid
        }
        catch (e: FirebaseAuthWeakPasswordException) {
            throw AppErrorException(ErrorType.WEAK_PASSWORD_ERROR,"register StudentFirebaseService","$e")
        }
        catch (e: Exception) {
            throw AppErrorException(ErrorType.UNEXPECTED_ERROR,"register StudentFirebaseService","$e")
        }
    }


    suspend fun fetchUserDetails(userUid:String , onComplete:(ResultState.Success<User>)->Unit){
        try {
            val snapshot = userRef.child(userUid).get().await()
            if(!snapshot.exists())
                throw AppErrorException(ErrorType.DATA_NOT_FOUND,"fetchUserDetails StudentFirebaseService","User does not exist in database")

            val user: User? = snapshot.getValue(User::class.java)
            if(user == null)
                throw AppErrorException(ErrorType.DATA_NOT_FOUND,"fetchUserDetails StudentFirebaseService","User is Null in database")
            Log.d("fetchUserDetails","CallingOnComplete")
            onComplete(ResultState.Success(user))
        }
        catch (e:Exception)
        {
            Log.d("User","error while fetching details $e")
            throw AppErrorException(ErrorType.UNEXPECTED_ERROR,"fetchUserDetails StudentFirebaseService","$e")
        }
    }

    suspend fun getUserRegistrationNumberFromEmail(email:String):Student{
        val student = Student()
        try {
            var snapshot=studentsRef.orderByChild("email").equalTo(email).get().await()
            snapshot=snapshot.children.first()

            student.registrationNumber= snapshot.child("registrationNumber").getValue(String::class.java)!!
            student.isRegistered= snapshot.child("isRegistered").getValue(Boolean::class.java)!!
            return student
        }
        catch (e:Exception)
        {
            Log.d("getUserRegistrationNumberFromEmail","error occurred")
            throw AppErrorException(ErrorType.UNEXPECTED_ERROR,"getUserRegistrationNumberFromEmail StudentFirebaseService","$e")
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
            throw AppErrorException(ErrorType.UNEXPECTED_ERROR,"addUser StudentFirebaseService","$e")
        }
    }


    //Used By Worker
    suspend fun fetchScheduleId(userUid: String): String {
        var scheduleId: String?
        val ref = database.child("User").child(userUid).child("scheduleId")
        try {
            val snapshot = ref.get().await()
            if (snapshot.exists())
                scheduleId = snapshot.getValue(String::class.java)
            else
                throw AppErrorException(ErrorType.DATA_NOT_FOUND,"fetchScheduleId StudentFirebaseService","Snapshot does not exist")

            if(scheduleId == null)
                    throw AppErrorException(ErrorType.DATA_FETCHED_IS_NULL,"fetchScheduleId StudentFirebaseService","ScheduleId fetched is null")

        } catch (e: Exception) {
            Log.d("studentFirebaseService fetchScheduleId", "Error occurred $e")
            throw AppErrorException(ErrorType.UNEXPECTED_ERROR,"addUser StudentFirebaseService","$e")
        }

        return scheduleId
    }

    private var scheduleDatabaseListener: ValueEventListener? = null
    suspend fun fetchSchedule(scheduleId: String, onDataChange: (Schedule) -> Unit) {
        val scheduleRef = database.child("Schedule").child(scheduleId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val schedule = snapshot.getValue(Schedule::class.java)
                    if(schedule == null)
                        throw AppErrorException(ErrorType.DATA_FETCHED_IS_NULL,"fetchSchedule StudentFirebaseService","Schedule Fetched or updated Is null")

                    onDataChange(schedule)
                } else
                    throw AppErrorException(ErrorType.DATA_NOT_FOUND,"fetchSchedule StudentFirebaseService","Schedule not found with ID: $scheduleId")
            }
            override fun onCancelled(e: DatabaseError) {
                Log.e("fetchScheduleAndAddListener", "Error fetching schedule with ID: $scheduleId. Error: ${e.message}")
                throw AppErrorException(ErrorType.CANCELLED,"fetchSchedule StudentFirebaseService",e.message)
            }
        }

        scheduleRef.addValueEventListener(listener)
        scheduleDatabaseListener = listener // Assign the listener to manage it later
    }

    fun removeScheduleListener(scheduleId: String) {
        try {
            val scheduleRef = database.child("Schedule").child(scheduleId)

            scheduleDatabaseListener?.let { listener ->
                scheduleRef.removeEventListener(listener) // Remove the previously assigned listener
                Log.d("removeScheduleListener", "Listener removed successfully for schedule ID: $scheduleId")
                scheduleDatabaseListener = null // Reset the listener reference
            } ?: run {
                Log.w("removeScheduleListener", "No listener to remove for schedule ID: $scheduleId")
            }
        } catch (e: Exception) {
            Log.e("removeScheduleListener", "Error occurred while removing listener: ${e.message}")
            throw AppErrorException(ErrorType.UNEXPECTED_ERROR,"removeScheduleListener StudentFirebaseService","$e")
        }
    }


    //Used By Worker
    @RequiresApi(Build.VERSION_CODES.O)
    fun scheduleStartCheck(userUid:String, scheduleUid: String, startTime:Long, context: Context, cycleUid:String) {
        val delayMillis =startTime - System.currentTimeMillis()

        val workRequest = OneTimeWorkRequestBuilder<CycleStateWorker>()
            .setInputData(
                workDataOf(
                    "scheduleUid" to scheduleUid,
                    "action" to "start",
                    "userUid" to userUid,
                    "cycleUid" to cycleUid
                )
            )
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setBackoffCriteria(
                backoffPolicy = BackoffPolicy.LINEAR,
                duration = Duration.ofSeconds(30)
            )
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
        Log.d("Worker","LastLine")
    }

    //Used By worker
    suspend fun startTimeAndExpectedTime(scheduleUid:String):Pair<Long,Long>{
        val scheduleRef = database.child("Schedule").child(scheduleUid)
        val scheduleEstimateTimeRef = scheduleRef.child("estimateTime")
        val estimateTimeSnapshot = scheduleEstimateTimeRef.get().await()
        val startTimeRef = scheduleRef.child("startTime")

        val estimateTime : Long?
        if(estimateTimeSnapshot.exists())
            estimateTime = estimateTimeSnapshot.getValue(Long::class.java)
        else{
            Log.d("Worker","Error while fetching the schedule Status")
            throw AppErrorException(ErrorType.DATA_NOT_FOUND,"startTimeAndExpectedTime StudentFirebaseService Worker","estimateTimeSnapshot does not exist")
        }

        val startTimeSnapshot = startTimeRef.get().await()
        val startTime : Long?

        if(startTimeSnapshot.exists())
            startTime = startTimeSnapshot.getValue(Long::class.java)
        else{
            Log.d("Worker","Error while fetching the schedule startTime")
            throw AppErrorException(ErrorType.DATA_NOT_FOUND,"startTimeAndExpectedTime StudentFirebaseService Worker","startTimeSnapshot does not exist")
        }

        if(startTime == null || estimateTime==null){
            Log.d("Worker","StartTime or estimateTime is null")
            throw AppErrorException(ErrorType.DATA_FETCHED_IS_NULL,"startTimeAndExpectedTime StudentFirebaseService Worker","StartTime or estimateTime is null")
        }

        return Pair(startTime,estimateTime)
    }

    // Function to update the state of schedule called within worker eg. 'from booked to ongoing'
    //Used By Worker
    suspend fun updateScheduleState(scheduleUid:String, newState: ScheduleState){
        val scheduleRef = database.child("Schedule").child(scheduleUid)
        val scheduleStateRef = scheduleRef.child("status").child("status")
        try {
            scheduleStateRef.setValue(newState).await()
        }
        catch (e:Exception){
            Log.d("Worker","Failed to update the schedule state $e")
            throw AppErrorException(ErrorType.UNEXPECTED_ERROR,"updateScheduleState StudentFirebaseService Worker","$e")
        }
    }

    suspend fun updatePrevBalance(userUid:String, balance:Long){
        val prevBalanceRef=userRef.child(userUid).child("prevBalance")

        try {
            prevBalanceRef.setValue(balance).await()
        }
        catch (e:Exception){
            Log.d("Worker","Error while updating the prevBalance in user $e")
            throw AppErrorException(ErrorType.UNEXPECTED_ERROR,"updateScheduleState StudentFirebaseService Worker","$e")
        }
    }

    suspend fun createSchedule(schedule:Schedule) : Pair<String,Long> {
        val scheduleRef = database.child("Schedule")
        try {
            val scheduleUid = scheduleRef.push().key

            if(scheduleUid !=null){
                schedule.scheduleUid = scheduleUid
                scheduleRef.child(scheduleUid).setValue(schedule).await()

                return Pair(scheduleUid,schedule.startTime)
            }
            else throw AppErrorException(ErrorType.DATA_FETCHED_IS_NULL,"createSchedule StudentFirebaseService","scheduleId is null")
        }
        catch(e:Exception){
            Log.d("createSchedule","Error occurred when creating a new Schedule $e")
            throw AppErrorException(ErrorType.UNEXPECTED_ERROR,"createSchedule StudentFirebaseService","$e")
        }
    }

    suspend fun startTimer(userUid:String,cycleUid:String){
        try {
            val snapshot = userRef.child(userUid)
            snapshot.child("timerStartTime").setValue( System.currentTimeMillis()).await()
            Log.d("startTimer","Timer set successfully")
            snapshot.child("cycleId").setValue(cycleUid).await()
            Log.d("startTimer","CycleUid set successfully in user")
        }
        catch (e:Exception){
            Log.d("startTimer","Error occurred while updating the timer in user")
            throw AppErrorException(ErrorType.UNEXPECTED_ERROR,"startTimer StudentFirebaseService","$e")
        }
    }

    //Function is called when new schedule is created successfully, updates all the schedule related information in user node
    suspend fun bookSchedule(userId:String,scheduleId:String){
        try {
            val snapshot = userRef.child(userId).child("scheduleId")
            snapshot.setValue(scheduleId).await()
        }
        catch (e:Exception){
            Log.d("bookSchedule studentFirebaseService","error Occurred $e")
            throw AppErrorException(ErrorType.UNEXPECTED_ERROR,"bookSchedule StudentFirebaseService","$e")
        }
    }


    //Used When completing a schedule/ride and simply sets the scheduleId = "" in userNode
    suspend fun updateUserScheduleId(userUid:String,updatedScheduleId:String){
        try{
            val ref = userRef.child(userUid)
            val snapshot = ref.child("scheduleId")
            snapshot.setValue(updatedScheduleId).await()
        }
        catch (e:Exception){
            Log.d("updateUserScheduleId","StudentFirebaseService Some Error Occurred $e")
            throw AppErrorException(ErrorType.UNEXPECTED_ERROR,"updateUserScheduleId StudentFirebaseService","$e")
        }
    }

    //User By Worker2
    @RequiresApi(Build.VERSION_CODES.O)
    fun returnOrCancelScheduleWorkerStart(schedule:Schedule, context: Context) {

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val workRequest = OneTimeWorkRequestBuilder<ReturnOrCancelWorker>()
            .setInputData(
                workDataOf(
                    "schedule" to Gson().toJson(schedule)
            ))
            //.setInitialDelay(1*60*1000, TimeUnit.MILLISECONDS)
            .setBackoffCriteria(
                backoffPolicy = BackoffPolicy.LINEAR,
                duration = Duration.ofSeconds(30)
            )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
        Log.d("Worker2","Worker2 Set Successfully")
    }

    //Used By worker2 - Updates the BookingHistory
    suspend fun updateBookingHistory(userUid:String,scheduleUid:String){
        val cycleRef = userRef.child(userUid)
        cycleRef.child("bookingHistory").get().addOnSuccessListener { snapshot ->
            val currentHistory = snapshot.getValue(object : GenericTypeIndicator<List<String>>() {}) ?: emptyList()

            // Create an updated list
            val updatedHistory = currentHistory.toMutableList()
            updatedHistory.add(scheduleUid)

            // Update the database
            cycleRef.child("bookingHistory").setValue(updatedHistory)
                .addOnSuccessListener {
                    Log.d("Firebase", "Booking history updated successfully")
                }
                .addOnFailureListener { e ->
                    Log.e("Firebase", "Failed to update booking history", e)
                    throw AppErrorException(ErrorType.UNEXPECTED_ERROR,"updateBookingHistory StudentFirebaseService","Failed to Update User BookingHistory $e")
                }
        }.addOnFailureListener { e ->
            Log.e("Firebase", "Failed to fetch booking history", e)
            throw AppErrorException(ErrorType.UNEXPECTED_ERROR,"updateBookingHistory StudentFirebaseService","Failed to fetch user BookingHistory$e")
        }
    }
}