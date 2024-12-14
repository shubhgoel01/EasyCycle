package com.example.easycycle.domain.usecases

import android.util.Log
import com.example.easycycle.data.model.bookCycle
import com.example.easycycle.data.remote.CycleFirebaseService
import com.example.easycycle.data.remote.SharedFirebaseService
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class SharedUseCase @Inject constructor(
    private val cycleUseCase: CycleUseCases,
    private val studentUseCase: StudentUseCases,
    private val auth: FirebaseAuth,
) {
    suspend fun bookAvailableCycleAndSetTimer(onComplete: (bookCycle) -> Unit){
        try {
            val cycleId:String? = cycleUseCase.bookAvailableCycle(onComplete)
            if(cycleId!=null && auth.currentUser!=null) {
                studentUseCase.startTimer(auth.currentUser!!.uid,cycleId)
                //Setting cycleId
            }
            else if(auth.currentUser == null || cycleId == null) {
                Log.e("SharedUseCase bookAvailableCycleAndSetTimer","auth.currentUser is null or cycleId is null")
                throw Exception("auth.currentUser is null or cycleId is null")
            }
        }
        catch (e:Exception){
            Log.d("bookAvailableCycleAndSetTimer","Error Occurred $e")
            onComplete(
                bookCycle(
                    isLoading = false,
                    error = true,
                    errorMessage = "An error occurred inside sharedUseCase: ${e.message}"
                )
            )
        }

    }
}