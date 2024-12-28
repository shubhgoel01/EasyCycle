package com.example.easycycle.data.model

import com.example.easycycle.data.Enum.Location
import com.example.easycycle.data.Enum.ScheduleState
import java.io.Serializable

data class Schedule(
    var userUid: String = "",
    var cycleUid: String = "",
    var startTime: Long = 0L,
    var estimateTime: Long = 0L,
    var scheduleUid: String = "",
    var paymentDetail : PaymentDetail = PaymentDetail(),
    var status: ScheduleStatus = ScheduleStatus(),
    var startDestination: Location =Location.NILGIRI,       //For future
    var endDestination:Location=Location.NILGIRI,       //For future
) : Serializable

data class ScheduleStatus(
    var status: ScheduleState = ScheduleState.BOOKED,
    var statusTime: Long = 0L,      //Basically completion time
    var delay: Delay? = null,
    var beforeTime: BeforeTime? = null
)

data class Delay(
    var isDelay: Boolean = false,
    var penalty: Long = 0L,
    var paymentId: String = ""
)

data class PaymentDetail(
    var paymentId: String = "",
    var amount:Long = 0L
)


data class BeforeTime(
    var remainingTime: Long = 0L,
    var amountReturned: Double = 0.0        //For future use
)

