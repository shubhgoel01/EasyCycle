package com.example.easycycle.Worker

import android.content.Context
import android.util.Log
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.easycycle.calculateEstimatedCost
import com.example.easycycle.data.Enum.ScheduleState
import com.example.easycycle.data.remote.StudentFirebaseService
import com.example.easycycle.presentation.viewmodel.UserViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltWorker
class CycleStateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val studentFirebaseService: StudentFirebaseService,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val scheduleUid = inputData.getString("scheduleUid") ?: return Result.failure()
        val userUid = inputData.getString("userUid") ?: return Result.failure()
        val action = inputData.getString("action") ?: return Result.failure()

        try {
            // Fetch the schedule details from the database
            Log.d("Worker","doWork Called")
            val currentScheduleUid :String? = fetchScheduleUid(userUid)
            if(currentScheduleUid != scheduleUid || currentScheduleUid.isNullOrEmpty())
                    return Result.success()

            val startTimeAndExpectedTime : Pair<Long,Long> = startTimeAndExpectedTime(scheduleUid)

            when (action) {
                "start" -> {
                    // Change state to Ongoing
                    if (System.currentTimeMillis() >= startTimeAndExpectedTime.first) {
                        updateScheduleState(scheduleUid, ScheduleState.ONGOING)
                        enqueueEndCheck(scheduleUid,userUid,startTimeAndExpectedTime.second)
                    }
                }
                "end" -> {
                    // Check for delayed state
                    if (System.currentTimeMillis() >= startTimeAndExpectedTime.first + startTimeAndExpectedTime.second) {
                        updateScheduleState(scheduleUid, ScheduleState.OVERTIME)
                        updatePrevBalance(userUid,startTimeAndExpectedTime.first,startTimeAndExpectedTime.second)
                        enqueueNextPenaltyCheck(scheduleUid,userUid)
                    }
                }
            }

            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("Worker","Error")
            return Result.failure(Data.Builder().putString("error",e.toString()).build())
        }
    }

    private suspend fun fetchScheduleUid(userUid : String):String?{
        return studentFirebaseService.fetchScheduleId(userUid)
    }
    private suspend fun startTimeAndExpectedTime(scheduleUid: String): Pair<Long, Long> {
        // Fetch the schedule details from Firebase
        // Example: Replace this with actual database query logic
        return studentFirebaseService.startTimeAndExpectedTime(scheduleUid)
    }

    private suspend fun updateScheduleState(scheduleUid: String, newState: ScheduleState) {
        // Update the schedule state in Firebase
        // Example: Replace with database update logic
        studentFirebaseService.updateScheduleState(scheduleUid,newState)
    }

    private suspend fun updatePrevBalance(userUid:String, startTime:Long, estimateTime: Long){
        //Update prevBalance in user
        var updatedPrevBalance = calculateEstimatedCost(System.currentTimeMillis() - (startTime + estimateTime))
        studentFirebaseService.updatePrevBalance(userUid,updatedPrevBalance)
        //userViewModel.updateExtendedFare(updatedPrevBalance)
    }

    private fun enqueueEndCheck(scheduleUid : String , userUid:String, estimateTime:Long) {
        val delayMillis = estimateTime
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

        val workRequest = OneTimeWorkRequestBuilder<CycleStateWorker>()
            .setInputData(
                workDataOf(
                    "scheduleUid" to scheduleUid,
                    "action" to "end",
                    "userUid" to userUid
                )
            )
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(applicationContext).enqueue(workRequest)
    }

    private fun enqueueNextPenaltyCheck(scheduleUid: String , userUid:String) {
        val delayMillis = 1L * 60L * 60L * 1000L // 1 hour

        val workRequest = OneTimeWorkRequestBuilder<CycleStateWorker>()
            .setInputData(
                workDataOf(
                    "scheduleUid" to scheduleUid,
                    "action" to "end",
                    "userUid" to userUid
                )
            )
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(applicationContext).enqueue(workRequest)
    }

}
