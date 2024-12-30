package com.example.easycycle.domain.usecases

import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class SharedUseCase @Inject constructor(
    private val cycleUseCase: CycleUseCases,
    private val studentUseCase: StudentUseCases,
    private val auth: FirebaseAuth,
) {
    suspend fun findAndBookAvailableCycleAndSetTimer(onComplete: (String) -> Unit){
        val cycleId:String = cycleUseCase.findAndBookAvailableCycle(onComplete)
        studentUseCase.startTimer(auth.currentUser!!.uid,cycleId)
    }

    suspend fun checkAndBookAvailableCycleAndSetTimer(cycleId:String,onComplete: () -> Unit){
        cycleUseCase.checkAndBookCycle(cycleId,onComplete)
        studentUseCase.startTimer(auth.currentUser!!.uid,cycleId)
    }
}