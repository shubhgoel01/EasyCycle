package com.example.easycycle.data.remote

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import javax.inject.Inject

@Entity(tableName="Profile_table")
data class Profile(
    @PrimaryKey
    var registrationNumber: String="",
    var name: String="",
    var email: String="",
    var branch:String=" ",
    var imageURL:String="",
    var phone:String="",
    var loginTimeStamp:Long=0L,
    var userType:String
)

@Dao
interface ProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(studentData: Profile)

    @Query("SELECT * FROM Profile_table LIMIT 1") // Assuming a single profile
    suspend fun getProfile(): Profile?

    @Query("DELETE FROM Profile_table")
    suspend fun clearProfile()
}



@Database(entities=[Profile::class],version= 1,exportSchema=false)
abstract class ProfileDatabase: RoomDatabase(){
    abstract fun profileDao():ProfileDao

    companion object {
        @Volatile
        private var INSTANCE: ProfileDatabase? = null

        //WHILE CREATING A DATABASE WE NEED TO PASS THE CONTEXT OF THE APPLICATION
        //so that only one instance of database is there for the whole application
        fun getDatabase(context: Context): ProfileDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ProfileDatabase::class.java,
                    "item_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
class ProfileRepository @Inject constructor(private val studentDao: ProfileDao) {
    suspend fun insertProfile(studentData: Profile) {
        studentDao.insert(studentData)
    }

    suspend fun getProfile(): Profile? {
        return studentDao.getProfile()
    }

    suspend fun clearProfile() {
        studentDao.clearProfile()
    }
}
