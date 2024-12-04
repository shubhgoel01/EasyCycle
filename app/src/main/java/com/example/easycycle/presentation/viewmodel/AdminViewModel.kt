package com.example.easycycle.presentation.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easycycle.data.model.Student
import com.example.easycycle.domain.usecases.AdminUseCases
import com.example.easycycle.domain.usecases.StudentUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val adminUseCases: AdminUseCases,   //1
    private val studentUseCases: StudentUseCases
) : ViewModel()
{
    //---------------------------------------------------------------------------------------------------
    fun addStudent(context: Context,student: Student){
        viewModelScope.launch {
            try {
                studentUseCases.addStudentExecute(context,student)
            }
            catch (e:Exception){
                Toast.makeText(context, "Error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    //---------------------------------------------------------------------------------------------------
}

// 1. As we have passed here that we need the AdminUseCases object here, the hilt will see in the Module if it can provide a object of this ( :AdminUseCases ) type
