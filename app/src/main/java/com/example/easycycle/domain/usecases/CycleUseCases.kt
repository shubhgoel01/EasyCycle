package com.example.easycycle.domain.usecases

import android.util.Log
import com.example.easycycle.data.Enum.Location
import com.example.easycycle.data.model.Cycle
import com.example.easycycle.data.model.allCycleDataState
import com.example.easycycle.data.model.bookCycle
import com.example.easycycle.data.remote.CycleFirebaseService
import com.example.easycycle.data.remote.SharedFirebaseService
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
    suspend fun bookAvailableCycle(onComplete: (bookCycle) -> Unit):String? {
        try {
            val cycleList = cycleDatabase.getAvailableCycles() // Fetch all available cycles
            //THIS IS INEFFICIENT BECAUSE cycleList WILL GET ALL CYCLES NODES BUT I JUST NEED AVAILABLE cycleUid
            if (cycleList.isNullOrEmpty()) {
                onComplete(
                    bookCycle(
                        isLoading = false,
                        error = true,
                        errorMessage = "No available cycles"
                    )
                )
                return null
            }

            for (cycle in cycleList) {
                // Attempt to book a cycle using a transaction
                // SEE HERE I AM JUST PASSING THE cycleUid OVER HERE HENCE OVERALL BOOKING PROCESS CAN BE FURTHER OPTIMIZED IN TERMS OF NETWORK CALLS
                val status = cycleDatabase.Transaction(cycle.cycleId)
                if (!status.isLoading && status.errorMessage.isNullOrEmpty()) {
                    // Successfully booked a cycle
                    onComplete(
                        bookCycle(
                            isLoading = false,
                            error = false,
                            cycle = cycle
                        )
                    )
                    return cycle.cycleId
                }
            }
            // Not able to book any cycle after trying all available ones
            onComplete(
                bookCycle(
                    isLoading = false,
                    error = true,
                    errorMessage = "Unable to book any cycle due to some issue"
                )
            )
        } catch (e: Exception) {
            // Handle any exceptions that occurred during the process
            onComplete(
                bookCycle(
                    isLoading = false,
                    error = true,
                    errorMessage = "An error occurred: ${e.message}"
                )
            )
        }
        return null
    }

}