package com.example.easycycle.data.model

data class Analytics( // Per day analytics
    var totalPaymentsDone: Double = 0.0,
    var totalCompletedRides: Int = 0,
    var totalCancelledRides: Int = 0,
    var totalCouponsUsed: Int = 0
)

data class Activity(        // separate entry for each activity
    var userId: String = "",
    var timeStamp: Long = 0L, // Use epoch time
    var description: String = ""
)

//These once created cannot be deleted

