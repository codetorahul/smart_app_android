package com.example.smartapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.smartapp.data.tables.Rooms

@Dao
interface RoomDao {
    @Insert
    suspend fun insertRoom(rooms: Rooms)

//    @Query("SELECT * FROM users WHERE room = :userId")
//    suspend fun getUserById(userId: Int): Rooms?

    @Query("SELECT * FROM rooms")
    suspend fun getAllRooms(): List<Rooms>

    @Query("DELETE FROM rooms WHERE roomId = :roomId")
    suspend fun deleteRoomById(roomId: Int)

    @Query("SELECT COUNT(*) FROM rooms ")
    suspend fun getRoomsCount(): Int

    @Delete
    suspend fun deleteRoom(rooms: Rooms)

    @Query("UPDATE rooms SET roomId = :newRoomId, isRoomIdUpdated= :isRoomUpdated WHERE _id = :oldRoomId")
    suspend fun updateRoomIdAndItsStatus(oldRoomId: Int, newRoomId: String, isRoomUpdated: Boolean)

    @Update
    suspend fun updateRoom(rooms: Rooms)
}