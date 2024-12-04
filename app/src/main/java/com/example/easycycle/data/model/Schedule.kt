package com.example.easycycle.data.model

import java.io.Serializable

data class Schedule(
    var userUid: String = "",
    var cycleUid: String = "",
    var startTime: Long = 0L,
    var estimateTime: Long = 0L,
    var scheduleUid: String = "",
    var paymentId: String = "",
    var status: ScheduleStatus = ScheduleStatus()
    //var startDestination:String="",     //For future
    //var endDestination:String="",       //For future
) : Serializable

data class Delay(
    var isDelay: Boolean = false,
    var penalty: Double = 0.0,
    var paymentId: String = ""
)


data class BeforeTime(
    var remainingTime: Long = 0L,
    var amountReturned: Double = 0.0        //For future use
)

enum class ScheduleState {
    COMPLETED,  // Indicates the schedule is completed
    CANCELLED,  // Indicates the schedule is cancelled
    ONGOING     // Indicates the schedule is in progress
}

data class ScheduleStatus(
    var status: ScheduleState = ScheduleState.ONGOING,
    var statusTime: Long = 0L,
    var delay: Delay? = null,
    var beforeTime: BeforeTime? = null
)

data class SchedulesDataState(
    val isLoading: Boolean = true,
    val error: Boolean = false,
    val scheduleDataList: List<Schedule> = emptyList(),
    val errorMessage: String = ""
)
