package com.example.smartapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.smartapp.ui.appliances.Appliances
import com.example.smartapp.ui.rooms.Rooms

@Dao
interface ApplianceDao {
    @Insert
    suspend fun insertAppliance(user: Appliances)

    @Query("SELECT * FROM appliances WHERE roomId = :roomId")
    suspend fun getApplianceByRoomId(roomId: Int): List<Appliances>

    @Query("SELECT * FROM appliances")
    suspend fun getAllAppliances(): List<Appliances>


    @Delete
    suspend fun deleteAppliance(rooms: Appliances)

    @Update
    suspend fun updateAppliance(appliances: Appliances)

    @Query("SELECT COUNT(*) FROM appliances WHERE roomId = :roomId")
    fun getApplianceCountByRoomId(roomId: String?): Int
}