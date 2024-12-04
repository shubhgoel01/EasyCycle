package com.example.easycycle

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApp : Application()

//This is required to get the application Context otherwise how hilt will know which application context,
//as we cannot define application context in module as in case of other objects
//After creating this class add it to the 'Manifests' android:name=".MyApp"