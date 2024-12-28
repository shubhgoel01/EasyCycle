package com.example.easycycle.data.model

sealed class ResultState<T> {
    data class Success<T>(val data: T?) : ResultState<T>()
    data class Error<T>(val error: AppErrorException) : ResultState<T>()
    class Loading<T>(var isLoading:Boolean = false) : ResultState<T>()
}
