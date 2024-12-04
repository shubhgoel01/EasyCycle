package com.example.easycycle.data.model

data class AllAdmins(
    var adminId:String="",
    var email:String="",
    var registrationTimeStamp:Long=0L,
    var imageURL:String="",
    var phone:String=""
)

data class Admin(
    var adminId:String="",
    val role:String="Admin",
    var activityHistory: List<String> = listOf("Default"),
    var firstLogInTimeStamp:Long=0L
)

data class adminDataState(
    var isLoading:Boolean=true,
    var error:Boolean=false,
    var AllAdmins:AllAdmins= AllAdmins(),
    var errorMessage:String?=null
)