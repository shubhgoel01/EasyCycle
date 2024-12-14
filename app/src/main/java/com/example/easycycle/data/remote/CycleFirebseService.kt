package com.example.easycycle.data.remote

import android.util.Log
import com.example.easycycle.data.Enum.Location
import com.example.easycycle.data.model.Cycle
import com.example.easycycle.data.model.allCycleDataState
import com.example.easycycle.data.model.bookCycle
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.resume

class CycleFirebaseService @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase
) {
    private var database = firebaseDatabase.reference
    private val cyclesRef=database.child("Cycles")

    private var databaseListener: ValueEventListener? = null

    /*
    suspend fun getAllCyclesAndAddEventListener(location: Location): List<Cycle> {
        val cycleList = mutableListOf<Cycle>()
        var allCycleDataState : allCycleDataState = allCycleDataState()

        val query = if (location == Location.ALL) {
            cyclesRef // No filtering, fetch all cycles
        } else {
            cyclesRef.orderByChild("location").equalTo(location.name) // Filter by location
        }

        try {
            Log.d("getAllCycles","Now Fetching allCycleData")
            val snapshot = query.get().await()
            if (snapshot.exists()) {
                cycleList.clear()
                for (childSnapshot in snapshot.children) {
                    val cycle = childSnapshot.getValue(Cycle::class.java)
                    if (cycle != null) cycleList.add(cycle)
                }
            }
            else{
                Log.d("getAllCycles","Snapshot does not exist")
                throw Exception("Snapshot does not exist")
            }
        }
        catch (e:Exception){
            Log.d("getAllCycle","Error Occurred when fetching allCyclesData")
            throw Exception("Error Occurred when fetching allCyclesData $e")
        }

            query.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    cycleList.clear() // Clear previous data before adding updated values
                    for (childSnapshot in snapshot.children) {
                        val cycle = childSnapshot.getValue(Cycle::class.java)
                        if (cycle != null) cycleList.add(cycle)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("CycleFirebaseService", "Error fetching cycles: ${error.message}")
                    //TODO Handle using callBack Function to update the allCycleDataState
                }
            })
        return cycleList
    }

    fun getAllCyclesRemoveEventListener(location: Location) {
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

     */

    private val cycleListFlow = MutableStateFlow<List<Cycle>>(emptyList())
    suspend fun getAllCyclesAndAddEventListener(location: Location): StateFlow<List<Cycle>> {
        val query = if (location == Location.ALL) {
            cyclesRef // No filtering, fetch all cycles
        } else {
            cyclesRef.orderByChild("location").equalTo(location.name) // Filter by location
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val updatedList = snapshot.children.mapNotNull { it.getValue(Cycle::class.java) }
                cycleListFlow.value = updatedList
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CycleFirebaseService", "Error fetching cycles: ${error.message}")
                cycleListFlow.value = emptyList()
            }
        }

        query.addValueEventListener(listener)
        databaseListener = listener // Assign the listener to `databaseListener`

        return cycleListFlow
    }


    fun getAllCyclesRemoveEventListener(location: Location) {
        try {
            val query = if (location == Location.ALL) {
                cyclesRef // No filtering, fetch all cycles
            } else {
                cyclesRef.orderByChild("location").equalTo(location.name) // Filter by location
            }

            databaseListener?.let { listener ->
                query.removeEventListener(listener) // Remove the previously assigned listener
                Log.d("removeEventListener", "Listener removed successfully for location: $location")
                databaseListener = null // Reset the listener to avoid reuse
            } ?: run {
                Log.w("removeEventListener", "No listener to remove for location: $location")
            }
        } catch (e: Exception) {
            Log.e("removeEventListener", "Error occurred: ${e.message}")
        }
    }



    //IT RETURNS FULL CYCLE NODE BUT WHERE IT IS USED I JUST NEED CYCLE-ID hence IN-EFFICIENT
    suspend fun getAvailableCycles(): List<Cycle>? {
        Log.d("Cycles","inside getAvailableCycles")
        val cycleList = mutableListOf<Cycle>()
        return try {
            val snapshot = cyclesRef
                .orderByChild("booked")
                .equalTo(false)
                .get()
                .await()

            if (snapshot.exists()) {
                snapshot.children.mapNotNullTo(cycleList) { it.getValue(Cycle::class.java) }
                // snapshot.children.mapNotNull { it.child("cycleId").getValue(String::class.java) }
                // instead use this line for optimization, Update later
                Log.d("Cycles","snapshot exist $cycleList")
                cycleList
            } else {
                Log.d("Cycles","snapshot does not exist")
                null
            }
        } catch (e: Exception) {
            throw e
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

    suspend fun Transaction(cycleId: String): bookCycle {
        Log.d("Transaction", "CycleId: $cycleId")
        val cycleRef = cyclesRef.child(cycleId)
        val bookedRef = cycleRef.child("underProcess")

        return suspendCancellableCoroutine { continuation ->
            bookedRef.runTransaction(object : Transaction.Handler {
                override fun doTransaction(currentData: MutableData): Transaction.Result {

                    val currentValue = currentData.getValue(Boolean::class.java) ?: false
                    Log.d("Transaction", "Current Value: $currentValue")

                    // If current value is null or true (cycle under process), abort the transaction
                    return if (currentValue) {
                        Log.d("Transaction", "Cycle is either already under process or null (not initialized).")
                        Transaction.abort()
                    } else {
                        // Update cycle status to 'under process'
                        Log.d("Transaction", "Updating cycle status to under process.")
                        currentData.value = true // Mark cycle as under process
                        Transaction.success(currentData) // Commit the transaction
                    }
                }

                override fun onComplete(
                    error: DatabaseError?,
                    committed: Boolean,
                    currentData: DataSnapshot?
                ) {
                    val resultState = when {
                        error != null -> {
                            Log.e("CycleUpdate", "Transaction failed: ${error.message}")
                            bookCycle(isLoading = false, error = true, errorMessage = error.message ?: "Unknown error")
                        }
                        committed -> {
                            Log.d("CycleUpdate", "Transaction committed successfully.")
                            bookCycle(isLoading = false, error = false)
                        }
                        else -> {
                            Log.d("CycleUpdate", "Transaction was aborted.")
                            bookCycle(isLoading = false, error = true, errorMessage = "Transaction aborted, cycle is already under process")
                        }
                    }
                    continuation.resume(resultState)
                }
            })
        }
    }
}