package com.example.easycycle.domain.usecases

import com.example.easycycle.data.Enum.Location
import com.example.easycycle.data.model.Cycle
import com.example.easycycle.data.remote.CycleFirebaseService
import com.example.easycycle.data.remote.SharedFirebaseService
import javax.inject.Inject

class CycleUseCases @Inject constructor(
    private val cycleDatabase: CycleFirebaseService,
    private val sharedDatabase : SharedFirebaseService
) {
    suspend fun getCycleList(updateCycleList:(List<Cycle>)->Unit){
        val cycleList:List<Cycle> = cycleDatabase.getAllCycles(Location.NILGIRI)
        updateCycleList(cycleList)
    }

    suspend fun addCycle(cycle:Cycle){
        cycleDatabase.addCycle(cycle)
    }
}