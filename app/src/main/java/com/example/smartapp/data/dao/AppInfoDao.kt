package com.example.smartapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.smartapp.data.tables.AppInfo
import com.example.smartapp.data.tables.Appliances

@Dao
interface AppInfoDao {
    @Insert
    suspend fun insertInfo(appInfo: AppInfo)


    @Query("SELECT * FROM AppInfo")
    suspend fun getAppInfo(): AppInfo?


    @Delete
    suspend fun deleteAppInfo(appInfo: AppInfo)

    @Update
    suspend fun updateAppInfo(appInfo: AppInfo)

    @Query("UPDATE appInfo SET isMacAddressUpdated = :isAddressUpdated WHERE _id = :id")
    suspend fun updateMacAddressStatus(isAddressUpdated: Boolean,id: Int)

}