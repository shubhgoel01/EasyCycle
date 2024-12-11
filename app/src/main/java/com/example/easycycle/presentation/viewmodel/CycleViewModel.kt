package com.example.easycycle.presentation.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easycycle.data.model.Cycle
import com.example.easycycle.domain.usecases.CycleUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CycleViewModel @Inject constructor(
    private val cycleUseCases: CycleUseCases
): ViewModel(){
    fun addCycle(cycle: Cycle){
        viewModelScope.launch(Dispatchers.IO) {
            cycleUseCases.addCycle(cycle)
        }

    }
}