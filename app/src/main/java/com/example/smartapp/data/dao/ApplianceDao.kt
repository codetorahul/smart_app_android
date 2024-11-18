package com.example.smartapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.smartapp.data.tables.Appliances

@Dao
interface ApplianceDao {
    @Insert
    suspend fun insertAppliance(user: Appliances)

    @Query("SELECT * FROM appliances WHERE roomId = :roomId")
    suspend fun getApplianceByRoomId(roomId: String): List<Appliances>

    @Query("SELECT * FROM appliances")
    suspend fun getAllAppliances(): List<Appliances>


    @Delete
    suspend fun deleteAppliance(rooms: Appliances)


    @Query("UPDATE appliances SET roomId = :newRoomId WHERE roomId = :oldRoomId")
    suspend fun updateRoomIdForAppliance(oldRoomId: String, newRoomId: String)

    @Update
    suspend fun updateAppliance(appliances: Appliances)

    @Query("SELECT COUNT(*) FROM appliances WHERE roomId = :roomId")
    fun getApplianceCountByRoomId(roomId: String?): Int

    @Query("DELETE FROM appliances WHERE roomId = :roomId")
    fun deleteAppliancesByRoomId(roomId: String?): Int


}