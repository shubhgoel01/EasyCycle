package com.example.easycycle.data.model

import com.google.firebase.Timestamp

data class User(
    var registrationNumber:String="",
    var paymentHistory: MutableList<String> = mutableListOf("Default"),
    var couponsClaimed: MutableList<String> = mutableListOf("Default"),     //For future use
    var bookingHistory: MutableList<String> = mutableListOf("Default"),
    val role:String="User",
    var userEngagementId: UserEngagement=UserEngagement(),
    var firstLogInTimeStamp:Long=0L,       //First time when user registered or signed in
    var account_Enabled:Boolean=true,
    var stayLoggedIn:Boolean = true,
    var scheduleId:String = "",
    var prevBalance : Int = 0,
    var timerStartTime : Long? = null,
    var cycleId:String = ""
)
data class UserEngagement(
    var totalCompletedRides: Int = 0,
    var totalCancelledRides: Int = 0,
    var totalPayments: Double = 0.0,
    var totalDelays: Int = 0,
    var lastAccessTime: Long = 0L, // Timestamp for last access
    var activityHistory: List<String> = listOf("Default"),
    var totalFeedbackGiven: Int = 0
)

data class userDataState(
    var isLoading:Boolean=true,
    var error:Boolean=false,
    var user:User= User(),
    var errorMessage:String?=null
)