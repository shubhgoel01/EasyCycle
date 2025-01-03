package com.example.easycycle.presentation.viewmodel


import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easycycle.data.Enum.ErrorType
import com.example.easycycle.data.Enum.Location
import com.example.easycycle.data.model.AppErrorException
import com.example.easycycle.data.model.Cycle
import com.example.easycycle.data.model.ResultState
import com.example.easycycle.domain.usecases.CycleUseCases
import com.example.easycycle.domain.usecases.SharedUseCase
import com.example.easycycle.logErrorOnLogcat
import com.example.easycycle.logInformationOnLogcat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CycleViewModel @Inject constructor(
    private val cycleUseCases: CycleUseCases,
    private val sharedUseCase: SharedUseCase
): ViewModel(){

    private var _reserveAvailableCycleState = MutableStateFlow<ResultState<String>>(ResultState.Loading(false))
    var reserveAvailableCycleState: StateFlow<ResultState<String>> = _reserveAvailableCycleState
    fun updateReserveAvailableCycleState(it:ResultState<String>){
        _reserveAvailableCycleState.value = it
    }

    private var _getAllCycleDataState = MutableStateFlow<ResultState<List<Cycle>>>(ResultState.Loading(false))
    var getAllCycleDataState: StateFlow<ResultState<List<Cycle>>> = _getAllCycleDataState
    fun updateGetAllCycleDataState(it:ResultState<List<Cycle>>){
        _getAllCycleDataState.value = it
    }

    private val _cycleViewModelLoadingShow = MutableStateFlow<Boolean>(false)
    val cycleViewModelLoadingShow : StateFlow<Boolean> = _cycleViewModelLoadingShow

    init {
        observeLoadingStates()
    }

    fun addCycle(cycle: Cycle){
        viewModelScope.launch(Dispatchers.IO) {
            cycleUseCases.addCycle(cycle)
        }
    }

    fun reserveAvailableCycle(context: Context, onCancel:()->Unit,onComplete:(String)->Unit){
        viewModelScope.launch(Dispatchers.IO) {
            try {
                logInformationOnLogcat("reserveAvailableCycle","Now Calling Functions")
                sharedUseCase.findAndBookAvailableCycleAndSetTimer{
                    _reserveAvailableCycleState.value = ResultState.Success(it)
                    onComplete(it)
                }
                logInformationOnLogcat("reserveAvailableCycle","Successfully Completed")

            }
            catch (e:AppErrorException){
                logErrorOnLogcat("bookCycle",e)
                when(e.type){
                    ErrorType.DATA_NOT_FOUND ->{
                        //Toast.makeText(context,"No Cycle Is available",Toast.LENGTH_SHORT).show()
                        _reserveAvailableCycleState.value = ResultState.Loading(false)
                        onCancel()
                    }
                    else -> {
                        //TODO move to error screen
                        Toast.makeText(context,"Some Internal error occurred",Toast.LENGTH_SHORT).show()
                        _reserveAvailableCycleState.value = ResultState.Error(e)
                    }
                }
            }
        }
    }

    fun getAllCyclesAndAddListener(location: Location) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                logInformationOnLogcat("getAllCycles","Now fetching all cycles and adding listener")
                cycleUseCases.getAllCyclesAndAddEventListener(location).collect { cycleList ->
                    _getAllCycleDataState.value = ResultState.Success(cycleList)
                }
            }
            catch (e:AppErrorException){
                logErrorOnLogcat("GetAllCycles",e)
                when (e.type){
                    ErrorType.DATA_NOT_FOUND -> _getAllCycleDataState.value = ResultState.Error(e)
                    else-> {
                        //TODO Decide here what to do, either move to error screen or do nothing
                        // Here i Am not changing result state - so previous state is visible (If loading then loading) (if dataScreen then previous data screen)
                    }
                }
            }

        }
    }

    fun getAllCyclesRemoveListener(location: Location) {
        try {
            logInformationOnLogcat("getAllCycles","Removing Listener")
            cycleUseCases.getAllCyclesRemoveListener(location)
        }
        catch (e:AppErrorException){
            logErrorOnLogcat("AllCycles",e)
        }

    }

    fun checkAvailableAndBookCycle(cycleUid:String,context: Context, onCancel:()->Unit,onComplete:()->Unit){
        viewModelScope.launch(Dispatchers.IO) {
            try {
                logInformationOnLogcat("reserveAvailableCycle","Now Calling Functions")
                sharedUseCase.checkAndBookAvailableCycleAndSetTimer(cycleUid){
                    _reserveAvailableCycleState.value = ResultState.Success(cycleUid)
                    onComplete()
                }
            }
            catch (e:AppErrorException){
                logErrorOnLogcat("bookCycle",e)
                when(e.type){
                    ErrorType.CANCELLED -> {
                        _reserveAvailableCycleState.value = ResultState.Loading(false)
                        onCancel()
                    }
                    else ->{
                        Toast.makeText(context,"Some Internal error occurred",Toast.LENGTH_SHORT).show()
                        _reserveAvailableCycleState.value = ResultState.Error(e)
                    }
                }
            }
        }
    }

    private fun observeLoadingStates() {
        combine(
            _reserveAvailableCycleState,
            _getAllCycleDataState,
        ) { reserveAvailableCycleState, getAllCycleDataState ->
            // Check if any state is loading
            listOf(reserveAvailableCycleState, getAllCycleDataState).any {
                it is ResultState.Loading && it.isLoading
            }
        }.onEach { isLoading ->
            _cycleViewModelLoadingShow.value = isLoading
        }.launchIn(viewModelScope)

    }

}