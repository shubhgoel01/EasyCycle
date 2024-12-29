package com.example.easycycle.domain.usecases

import android.util.Log
import com.example.easycycle.data.Enum.ErrorType
import com.example.easycycle.data.Enum.Location
import com.example.easycycle.data.model.AppErrorException
import com.example.easycycle.data.model.Cycle
import com.example.easycycle.data.model.ResultState
import com.example.easycycle.data.remote.CycleFirebaseService
import com.example.easycycle.data.remote.SharedFirebaseService
import com.example.easycycle.logErrorOnLogcat
import com.example.easycycle.logMessageOnLogcat
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class CycleUseCases @Inject constructor(
    private val cycleDatabase: CycleFirebaseService,
    private val sharedDatabase : SharedFirebaseService
) {
    suspend fun addCycle(cycle:Cycle){
        cycleDatabase.addCycle(cycle)
    }

    suspend fun getAllCyclesAndAddEventListener(location: Location): StateFlow<List<Cycle>> {
        return cycleDatabase.getAllCyclesAndAddEventListener(location)
    }

    fun getAllCyclesRemoveListener(location: Location) {
        cycleDatabase.getAllCyclesRemoveEventListener(location)
    }
    suspend fun bookAvailableCycle(onComplete: (String) -> Unit):String {
        val cycleList = cycleDatabase.getAvailableCycles() // Fetch all available cycles
        //THIS IS INEFFICIENT BECAUSE cycleList WILL GET ALL CYCLES NODES BUT I JUST NEED AVAILABLE cycleUid

        for (cycle in cycleList) {
            // Attempt to book a cycle using a transaction
            // SEE HERE I AM JUST PASSING THE cycleUid OVER HERE HENCE OVERALL BOOKING PROCESS CAN BE FURTHER OPTIMIZED IN TERMS OF NETWORK CALLS
            try {
                logMessageOnLogcat("Transaction","Trying to book cycleId ${cycle.cycleId}")
                val status = cycleDatabase.Transaction(cycle.cycleId)
                //If Any Error Occurred, then throw it, so it can be handled inside the catch block
                if(status is ResultState.Error)
                      throw status.error

                //If no error has occurred then simply continue with the flow
                onComplete(cycle.cycleId)
                return cycle.cycleId
            }
            catch (e:AppErrorException){
                logErrorOnLogcat("Transaction",e)
                //TODO separately handle unexpectedError
            }
        }
        throw AppErrorException(ErrorType.DATA_NOT_FOUND,"bookAvailableCycle CycleUseCases","No Cycle Is available")
    }

}