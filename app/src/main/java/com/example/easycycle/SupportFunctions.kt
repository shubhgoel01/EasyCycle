package com.example.easycycle

import android.os.Build
import androidx.annotation.RequiresApi
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