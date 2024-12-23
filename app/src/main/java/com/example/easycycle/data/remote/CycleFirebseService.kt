package com.example.easycycle.data.remote

import android.util.Log
import com.example.easycycle.data.Enum.Location
import com.example.easycycle.data.model.Cycle
import com.example.easycycle.data.model.bookCycle
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
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

    private var allCycleDatabaseListener: ValueEventListener? = null

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
        allCycleDatabaseListener = listener // Assign the listener to `databaseListener`

        return cycleListFlow
    }


    fun getAllCyclesRemoveEventListener(location: Location) {
        try {
            val query = if (location == Location.ALL) {
                cyclesRef // No filtering, fetch all cycles
            } else {
                cyclesRef.orderByChild("location").equalTo(location.name) // Filter by location
            }

            allCycleDatabaseListener?.let { listener ->
                query.removeEventListener(listener) // Remove the previously assigned listener
                Log.d("removeEventListener", "Listener removed successfully for location: $location")
                allCycleDatabaseListener = null // Reset the listener to avoid reuse
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

    suspend fun bookSchedule(scheduleUid:String,cycleUid:String,estimatedNextAvailableTime:Long){
        try {
            val snapshot = cyclesRef.child(cycleUid)
            snapshot.child("booked").setValue(true).await()
            snapshot.child("cycleStatus").child("estimatedNextAvailableTime").setValue(estimatedNextAvailableTime).await()
            snapshot.child("cycleStatus").child("scheduleId").setValue(scheduleUid).await()
        }
        catch (e:Exception){
            Log.e("bookSchedule cycleFirebaseService","Error Occurred")
            throw e
        }
    }

    suspend fun updateNextAvailableTime(cycleUid:String,updatedTime:Long){
        try {
            Log.d("CycleFirebaseService","Entered updateAvailableTime")
            val snapshot = cyclesRef.child(cycleUid)
            snapshot.child("cycleStatus").child("estimatedNextAvailableTime").setValue(updatedTime).await()
            Log.d("updateNextAvailableTime","Successfully updated next available time")
        }
        catch (e:Exception){
            Log.d("CycleFirebaseService","updateAvailableTime Error Occurred when updating availableTIme")
            throw e
        }
    }

    suspend fun updateCycleBooked(cycleUid: String,value:Boolean){
        try {
            val snapshot = cyclesRef.child(cycleUid)
            snapshot.child("booked").setValue(value).await()
            Log.d("updateCycleBooked","CycleFireBaseService Successfully updated",)
        }
        catch (e:Exception){
            Log.e("updateCycleBooked","cycleFirebaseService Error Occurred")
            throw e
        }
    }
    suspend fun updateBookingHistory(cycleUid:String,scheduleUid:String){
        val cycleRef = cyclesRef.child(cycleUid)
        cycleRef.child("bookingHistory").get().addOnSuccessListener { snapshot ->
            val currentHistory = snapshot.getValue(object : GenericTypeIndicator<List<String>>() {}) ?: emptyList()

            // Create an updated list
            val updatedHistory = currentHistory.toMutableList()
            updatedHistory.add(scheduleUid)

            // Update the database
            cycleRef.child("bookingHistory").setValue(updatedHistory)
                .addOnSuccessListener {
                    Log.d("Firebase", "Booking history updated successfully")
                }
                .addOnFailureListener { e ->
                    Log.e("Firebase", "Failed to update booking history", e)
                    throw e
                }
        }.addOnFailureListener { e ->
            Log.e("Firebase", "Failed to fetch booking history", e)
            throw e
        }
    }
}