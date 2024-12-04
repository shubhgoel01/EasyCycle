package com.example.easycycle.data.model

import com.google.firebase.database.PropertyName

data class Student(
    @PropertyName("name")                  var name: String="",
    @PropertyName("email")                 var email: String="",
    @PropertyName("registrationNumber")    var registrationNumber: String="",
    @PropertyName("branch")                var branch:String=" ",
    @PropertyName("imageURL")              var imageURL:String="",
    @PropertyName("phone")                 var phone:String="",
    @PropertyName("registrationTimeStamp") var registrationTimeStamp:Long=0L,
    @PropertyName("isRegistered") var isRegistered:Boolean=false,
)

data class StudentDataState(
    var isLoading:Boolean=true,
    var error:Boolean=false,
    var student:Student= Student(),
    var errorMessage:String?=null
)