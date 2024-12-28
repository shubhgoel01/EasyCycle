package com.example.easycycle

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.easycycle.data.model.AppErrorException
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.ceil

fun calculateTimeElapsed(startTime: Long, estimatedTime: Long): String {
    // Calculate the actual elapsed time beyond the estimated time
    val elapsedMillis = System.currentTimeMillis() - (startTime + estimatedTime)
    val minutes = elapsedMillis / (1000 * 60)
    return "$minutes minutes" // Return the elapsed time in minutes
}

fun calculateEstimatedCost(timeInMilliSec: Long): Long {
    // Convert elapsed time to minutes
    val elapsedMinutes = timeInMilliSec / (1000 * 60)

    // If there is no extra time, the cost is 0
    if (elapsedMinutes <= 0) return 0

    // Calculate the upper limit (round up to the next hour)
    val upperLimitInHours = ceil(elapsedMinutes / 60.0).toInt()

    // Calculate the cost (2 rupees per hour)
    val cost = upperLimitInHours * 2

    return cost.toLong()
}


@RequiresApi(Build.VERSION_CODES.O)
fun formatTimestamp(timestamp: Long): String {
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")
    return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).format(formatter)
}

fun logErrorOnLogcat(tag:String,value: AppErrorException){
    Log.e(tag,value.message.toString())
}

fun logInformationOnLogcat(tag:String,value:String){
    Log.i(tag,value)
}

fun logMessageOnLogcat(tag:String,value:String){
    Log.d(tag,value)
}

fun isValidEmail(email: String): Boolean {
    val emailPattern = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    val regex = Regex(emailPattern)
    return regex.matches(email)
}
