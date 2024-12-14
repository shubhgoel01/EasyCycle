package com.example.easycycle.data.model

import com.example.easycycle.data.Enum.Location
import com.google.firebase.database.PropertyName
import java.io.Serializable

data class Cycle(
    var cycleId: String = "",
    var bookingHistory: List<String> = listOf("Default"),
    var imageURL: String? = null,
    var cycleStatus: CycleStatus = CycleStatus(),
    var location: Location =Location.NILGIRI,
    var underProcess : Boolean = false,
    var booked:Boolean=false,
) //: Serializable

data class CycleStatus(
    var scheduleId:String="",
    var estimatedNextAvailableTime:Long=0L
)

//data class LastLocation(
//    var latitude:String="",
//    var longitude:String=""
//)

data class AvailableCycleDataState(
    var iSLoading:Boolean = true,
    var error:Boolean = false,
    var errorMessage:String = "",
    val list: List<Cycle> = listOf()
)

data class allCycleDataState(
    var isLoading:Boolean = true,
    var error:Boolean = false,
    var errorMessage:String = "",
    val list: List<Cycle> = listOf()
)

data class bookCycle(
    var isLoading:Boolean = true,
    var error:Boolean = false,
    var errorMessage:String = "",
    var cycle:Cycle? = null
)
