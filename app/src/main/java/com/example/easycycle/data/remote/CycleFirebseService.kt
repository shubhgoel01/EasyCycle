package com.example.easycycle.data.remote

import android.util.Log
import com.example.easycycle.data.Enum.Location
import com.example.easycycle.data.model.Cycle
import com.example.easycycle.data.model.allCycleDataState
import com.example.easycycle.data.model.cycleUpdateState
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CycleFirebaseService @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase
) {
    private var database = firebaseDatabase.reference
    private val cyclesRef=database.child("Cycles")

    private var databaseListener: ValueEventListener? = null

    suspend fun getAllCycles(location: Location): List<Cycle> {
        val cycleList = mutableListOf<Cycle>()
        var allCycleDataState : allCycleDataState = allCycleDataState()

        val query = if (location == Location.ALL) {
            cyclesRef // No filtering, fetch all cycles
        } else {
            cyclesRef.orderByChild("location").equalTo(location.name) // Filter by location
        }

            val snapshot = query.get().await()
            if (snapshot.exists()) {
                cycleList.clear()
                for (childSnapshot in snapshot.children) {
                    val cycle = childSnapshot.getValue(Cycle::class.java)
                    if (cycle != null) cycleList.add(cycle)
                }
            }

            query.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    cycleList.clear() // Clear previous data before adding updated values
                    for (childSnapshot in snapshot.children) {
                        val cycle = childSnapshot.getValue(Cycle::class.java)
                        if (cycle != null) cycleList.add(cycle)
                    }

                    allCycleDataState = allCycleDataState.copy(
                        isLoading = false,
                        error = false,
                        list = cycleList
                    )
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("CycleFirebaseService", "Error fetching cycles: ${error.message}")

                    allCycleDataState = allCycleDataState.copy(
                        isLoading = false,
                        error = true,
                        errorMessage = "Error while updating all cycles $error.toString()"
                    )
                }
            })
        return cycleList
    }


    fun removeEventListener(location: Location) {
        try {
            val query = if (location == Location.ALL) {
                cyclesRef // No filtering, fetch all cycles
            } else {
                cyclesRef.orderByChild("location").equalTo(location.name) // Filter by location
            }

            // Ensure the listener is non-null before attempting to remove it
            databaseListener?.let { listener ->
                query.removeEventListener(listener)
                Log.d("removeEventListener", "Listener removed successfully for location: $location")
            } ?: run {
                Log.w("removeEventListener", "No listener to remove for location: $location")
            }
        } catch (e: Exception) {
            Log.e("removeEventListener", "Error occurred: ${e.message}")
            throw e // Optionally re-throw or handle the error here
        }
    }


    suspend fun addCycle(cycle:Cycle){
        try {
            cyclesRef.child(cycle.cycleId).setValue(cycle).await()
            Log.e("Cycle", "Cycle added successfully")
        }
        catch (e:Exception){
            Log.e("Cycle", "Error occurred while adding cycle: ${e.message}")
            throw e
        }
    }

    suspend fun cycleExist(cycle:Cycle):Boolean{
        try {
            val query = cyclesRef.orderByChild("cycleId").equalTo(cycle.cycleId)
            val snapshot = query.get().await()
            if(snapshot.exists())
                    return true
        }
        catch (e:Exception){
            throw e
        }
        return false
    }

        //This should be done under a transaction and use state class for this
        //This function should first check if the field is already set, if yes then the error should be thrown indicating the transaction
        //is not completed and user can be notified that the cycle is already booked.
        //However we will be fetching available cycles only hence it is not needed, but for database integrity its important
    suspend fun updateCycleStatus(cycleId: String): cycleUpdateState {
        val ref = cyclesRef.child(cycleId)
        val initialState = cycleUpdateState(isLoading = true)

        return try {
            // Wrap the runTransaction inside suspendCoroutine
            suspendCoroutine { continuation ->
                ref.runTransaction(object : Transaction.Handler {
                    override fun doTransaction(mutableData: MutableData): Transaction.Result {
                        val cycle = mutableData.getValue(Cycle::class.java)

                        // Abort if cycle is null or already under process
                        if (cycle == null || cycle.underProcess) {
                            Log.w("CycleUpdate", "Cycle is null or under process already.")
                            return Transaction.abort()
                        }

                        // Mark the cycle as under process
                        cycle.underProcess = true
                        mutableData.value = cycle

                        return Transaction.success(mutableData)
                    }

                    override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                        val resultState = when {
                            error != null -> {
                                Log.e("CycleUpdate", "Transaction failed: ${error.message}")
                                initialState.copy(
                                    isLoading = false,
                                    error = true,
                                    errorMessage = error.message ?: "Unknown error"
                                )
                            }
                            committed -> {
                                Log.d("CycleUpdate", "Transaction committed successfully.")
                                initialState.copy(
                                    isLoading = false,
                                    error = false

                                )
                            }
                            else -> {
                                Log.d("CycleUpdate", "Transaction was aborted.")
                                initialState.copy(
                                    isLoading = false,
                                    error = true,
                                    errorMessage = "Transaction aborted, cycle is already under process"
                                )
                            }
                        }
                        continuation.resume(resultState)
                    }
                })
            }
        } catch (e: Exception) {
            Log.e("CycleUpdate", "Error occurred during transaction: ${e.message}")
            initialState.copy(
                isLoading = false,
                error = true,
                errorMessage = e.message ?: "Unknown error"
            )
        }
    }
}