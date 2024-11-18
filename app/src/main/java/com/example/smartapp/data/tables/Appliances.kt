package com.example.smartapp.data.tables

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appliances")
data class Appliances(
    @PrimaryKey(autoGenerate = true) val _id: Int = 0,
    val roomId: String,
    var applianceName: String,
    var applianceId: String,
    val applianceColor: String,
    var applianceStatus: Boolean
){



}
