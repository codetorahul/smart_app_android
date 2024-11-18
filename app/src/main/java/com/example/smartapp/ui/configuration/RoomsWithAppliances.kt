package com.example.smartapp.ui.configuration

import com.example.smartapp.data.tables.Appliances

data class RoomsWithAppliances(
    var _roomId: Int,
    var roomId: String,
    var roomName: String,
    val roomColor: String,
    var appliances: List<Appliances>,
    var isMacAddressAdded : Boolean =false
)