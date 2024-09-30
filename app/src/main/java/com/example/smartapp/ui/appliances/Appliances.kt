package com.example.smartapp.ui.appliances

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appliances")
data class Appliances(
    @PrimaryKey(autoGenerate = true) val applianceId: Int = 0,
    val roomId: String,
    var applianceName: String,
    val applianceColor: String,
    var applianceStatus: Boolean
){



}
