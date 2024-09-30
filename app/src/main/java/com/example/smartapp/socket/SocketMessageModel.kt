package com.example.smartapp.socket

data class SocketMessageModel(
    var type: String="",
    var roomId: String="",
    var applianceId: String="",
    var applianceStatus: Boolean= false,
    var wifiName: String="",
    var ssdId : String="",
    var password: String="",
    var message: String=""
)
