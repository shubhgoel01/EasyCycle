package com.example.easycycle.Modules

import com.example.easycycle.data.model.Admin
import com.example.easycycle.data.remote.AdminFirebaseService
import com.example.easycycle.data.remote.CycleFirebaseService
import com.example.easycycle.data.remote.SharedFirebaseService
import com.example.easycycle.data.remote.StudentFirebaseService
import com.example.easycycle.domain.usecases.AdminUseCases
import com.example.easycycle.domain.usecases.StudentUseCases
import com.example.easycycle.presentation.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)       //1
object AppModule {      //2
    @Provides
    @Singleton
    fun provideAdminUseCases(
        adminDatabase: AdminFirebaseService,
        sharedDatabase : SharedFirebaseService
    ):AdminUseCases{
        return AdminUseCases(adminDatabase,sharedDatabase)
    }

    @Provides
    @Singleton
    fun provideStudentUseCases(
        studentDatabase: StudentFirebaseService,
        sharedDatabase : SharedFirebaseService
    ):StudentUseCases{
        return StudentUseCases(studentDatabase,sharedDatabase)
    }

    @Provides
    @Singleton
    fun provideAdminFireBAseService(
        firebaseDatabase: FirebaseDatabase
    ):AdminFirebaseService{
        return AdminFirebaseService(firebaseDatabase)
    }

    @Provides
    @Singleton
    fun provideStudentFireBAseService(
        firebaseDatabase: FirebaseDatabase,
        auth: FirebaseAuth,
        firebaseStorage: FirebaseStorage,
        ): StudentFirebaseService {
        return StudentFirebaseService(firebaseDatabase,auth,firebaseStorage)
    }

    @Provides
    @Singleton
    fun provideSharedFirebaseService(
        firebaseDatabase: FirebaseDatabase,
        auth: FirebaseAuth
    ): SharedFirebaseService {
        return SharedFirebaseService(firebaseDatabase,auth)
    }

    @Provides
    @Singleton
    fun provideCycleFirebaseService(
        firebaseDatabase: FirebaseDatabase
    ): CycleFirebaseService {
        return CycleFirebaseService(firebaseDatabase)
    }

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase {
        return Firebase.database
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideStorage(): FirebaseStorage {
        return Firebase.storage
    }
}


/*
------------------------1-----------------------------

This decides the lifetime of all the dependencies provided in this module
We usually create a separate module for each, eg.
          SingletonComponent for dependencies having lifetime equal to the Application
          ActivityComponent lifetime dependent dependencies
          ViewModelComponent


------------------2----------------------------------

In case AdminUseCases needs some parameter let say of type :MyApi that is already provided in AppModule as above then it can be
     passed as
     fun provideAdminUseCases(api :MyApi):AdminUseCases{
        return AdminUseCases(api )
    }

    Note: We do not need to call all these functions Dagger-Hilt will automatically call these functions and provide the parameters
    based on the requirements
     */