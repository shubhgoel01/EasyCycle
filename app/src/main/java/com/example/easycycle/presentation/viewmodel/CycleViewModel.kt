package com.example.easycycle.presentation.viewmodel


import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easycycle.data.Enum.Location
import com.example.easycycle.data.model.Cycle
import com.example.easycycle.data.model.StudentDataState
import com.example.easycycle.data.model.allCycleDataState
import com.example.easycycle.data.model.bookCycle
import com.example.easycycle.domain.usecases.CycleUseCases
import com.example.easycycle.domain.usecases.SharedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CycleViewModel @Inject constructor(
    private val cycleUseCases: CycleUseCases,
    private val sharedUseCase: SharedUseCase
): ViewModel(){

    private var _reserveAvailableCycleState = MutableStateFlow(bookCycle())
    var reserveAvailableCycleState: StateFlow<bookCycle> = _reserveAvailableCycleState
    fun updateReserveAvailableCycleState(it:bookCycle){
        _reserveAvailableCycleState.value = _reserveAvailableCycleState.value.copy(
            isLoading = it.isLoading,
            error = it.error,
            errorMessage = it.errorMessage,
            cycle = it.cycle
        )
    }

    private var _getAllCycleDataState = MutableStateFlow(allCycleDataState())
    var getAllCycleDataState: StateFlow<allCycleDataState> = _getAllCycleDataState
    fun updateGetAllCycleDataState(it:allCycleDataState){
        _getAllCycleDataState.value = _getAllCycleDataState.value.copy(
            isLoading = it.isLoading,
            error = it.error,
            errorMessage = it.errorMessage,
            list = it.list
        )
    }

    fun addCycle(cycle: Cycle){
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("addCycle",cycle.toString())
            cycleUseCases.addCycle(cycle)
        }
    }

    fun reserveAvailableCycle(){
        viewModelScope.launch(Dispatchers.IO) {
            sharedUseCase.bookAvailableCycleAndSetTimer{
                _reserveAvailableCycleState.value = _reserveAvailableCycleState.value.copy(
                    isLoading = it.isLoading,
                    error = it.error,
                    errorMessage = it.errorMessage,
                    cycle = it.cycle
                )
            }
        }
    }

    fun getAllCyclesAndAddListener(location: Location) {
        viewModelScope.launch(Dispatchers.IO) {
            cycleUseCases.getAllCyclesAndAddEventListener(location).collect { cycleList ->
                if(cycleList.isNotEmpty())
                _getAllCycleDataState.value = _getAllCycleDataState.value.copy(
                    isLoading = false,
                    list = cycleList,
                    error = false,
                    errorMessage = ""
                )
                else
                    _getAllCycleDataState.value = _getAllCycleDataState.value.copy(
                        isLoading = false,
                        error = true,
                        errorMessage = "Either Some Error Occurred or No Cycle Is There"
                    )
            }
        }
    }

    fun getAllCyclesRemoveListener(location: Location) {
        cycleUseCases.getAllCyclesRemoveListener(location)
    }
}