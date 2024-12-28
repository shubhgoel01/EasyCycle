package com.example.easycycle.domain.usecases

import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class SharedUseCase @Inject constructor(
    private val cycleUseCase: CycleUseCases,
    private val studentUseCase: StudentUseCases,
    private val auth: FirebaseAuth,
) {
    suspend fun bookAvailableCycleAndSetTimer(onComplete: (String) -> Unit){
        val cycleId:String = cycleUseCase.bookAvailableCycle(onComplete)
        studentUseCase.startTimer(auth.currentUser!!.uid,cycleId)
    }
}