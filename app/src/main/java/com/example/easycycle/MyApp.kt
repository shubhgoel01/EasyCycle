package com.example.easycycle

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.easycycle.Worker.CycleStateWorker
import dagger.assisted.AssistedFactory
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MyApplication: Application(), Configuration.Provider {
    @Inject
    lateinit var hiltWorkerFactory: HiltWorkerFactory


    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(hiltWorkerFactory)
            .setMinimumLoggingLevel(Log.DEBUG)
            .build()
    }
}

@AssistedFactory
interface CycleStateWorkerFactory {
    fun create(
        context: Context,
        workerParams: WorkerParameters
    ): CycleStateWorker
}

//This is required to get the application Context otherwise how hilt will know which application context,
//as we cannot define application context in module as in case of other objects
//After creating this class add it to the 'Manifests' android:name=".MyApp"