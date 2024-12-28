package com.example.easycycle.data.model

import com.example.easycycle.data.Enum.ErrorType

class AppErrorException(val type: ErrorType, location:String, details:String) : Exception(details)