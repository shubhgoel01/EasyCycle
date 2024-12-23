package com.example.easycycle.Worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.example.easycycle.data.model.Schedule
import com.example.easycycle.data.remote.CycleFirebaseService
import com.example.easycycle.data.remote.SharedFirebaseService
import com.example.easycycle.data.remote.StudentFirebaseService
import com.google.gson.Gson
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import kotlin.math.pow

@HiltWorker
class ReturnOrCancelWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val studentFirebaseService: StudentFirebaseService,
    private val cycleFirebaseService: CycleFirebaseService,
    private val sharedFirebaseService: SharedFirebaseService
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val scheduleString = inputData.getString("schedule") ?: return Result.failure()
        val schedule = Gson().fromJson(scheduleString, Schedule::class.java)

        val maxRetries = 3

        // Task 1: Create Schedule History
        if (!retryTask(maxRetries) { sharedFirebaseService.createNewScheduleHistory(schedule) }) {
            return Result.failure(Data.Builder().putString("error", "Failed to create schedule history").build())
        }

        // Task 2: Remove Schedule
        if (!retryTask(maxRetries) { sharedFirebaseService.removeSchedule(schedule.scheduleUid) }) {
            return Result.failure(Data.Builder().putString("error", "Failed to remove schedule").build())
        }

        // Task 3: Update Cycle Booking History
        if (!retryTask(maxRetries) { cycleFirebaseService.updateBookingHistory(schedule.cycleUid, schedule.scheduleUid) }) {
            return Result.failure(Data.Builder().putString("error", "Failed to update cycle booking history").build())
        }

        // Task 4: Update User Booking History
        if (!retryTask(maxRetries) { studentFirebaseService.updateBookingHistory(schedule.userUid, schedule.scheduleUid) }) {
            return Result.failure(Data.Builder().putString("error", "Failed to update user booking history").build())
        }

        return Result.success()
    }

    private suspend fun retryTask(retries: Int, task: suspend () -> Unit): Boolean {
        var attempts = 0
        while (attempts < retries) {
            try {
                task()
                return true // Success
            } catch (e: Exception) {
                attempts++
                Log.e("Worker", "Task failed. Attempt $attempts of $retries", e)
                if (attempts == retries) {
                    return false // Fail after max retries
                }
                // Delay before retrying (exponential backoff)
                delay((2.0.pow(attempts.toDouble()) * 100).toLong())
            }
        }
        return false
    }
}
