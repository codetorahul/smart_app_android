package com.example.smartapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.smartapp.data.dao.ApplianceDao
import com.example.smartapp.data.dao.RoomDao
import com.example.smartapp.ui.appliances.Appliances
import com.example.smartapp.ui.rooms.RoomListConverter
import com.example.smartapp.ui.rooms.Rooms

@Database(entities = [Rooms::class, Appliances::class], version = 2, exportSchema = false)
@TypeConverters(RoomListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun roomDao(): RoomDao
    abstract fun applianceDao(): ApplianceDao


    companion object {
        // Singleton instance to prevent multiple instances of the database being created
        @Volatile
        private var INSTANCE: AppDatabase? = null

        @Synchronized
        fun getDatabase(context: Context): AppDatabase {
             return INSTANCE ?: synchronized(this) {
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "smart_app"
                    ).build()
                    INSTANCE = instance
                    instance
            }
        }
    }
}
