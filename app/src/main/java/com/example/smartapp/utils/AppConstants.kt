package com.example.smartapp.utils

object AppConstants {

    const val THRESHOLD_ROOMS = 10
    const val THRESHOLD_APPLIANCES = 4
    const val DEMO_EMAIL= "abc@gmail.com"
    const val DEMO_PASSWORD= "123456"

    const val IS_LOGGED_IN = "isLoggedIn"


    // Dialog type
    const val ADD_ROOM = "addRoom"
    const val RENAME_ROOM = "renameRoom"
    const val ADD_APPLIANCES = "addAppliances"
    const val RENAME_APPLIANCES = "renameAppliances"
    const val WIFI_PASSWORD= "wifiPassword"
    const val LOGOUT_CONFIRMATION = "logoutConfirmation"
    const val DELETE_CONFIRMATION = "deleteConfirmation"

    // Option Type
    const val OPTION_DELETE = "optionDelete"
    const val OPTION_RENAME = "optionRename"

    // Dialog options
    const val OPTION_SEND = "Send"
    const val OPTION_CANCEL = "cancel"

    // Dialog type
    const val DIALOG_LOCATION_ENABLE = "Location Enable Dialog"
    const val DIALOG_WIFI_INFO = "Wifi Info"

    // Socket
    // for testing
    // const val WEB_SOCKET_URL = "ws://192.168.0.199:8080"

    const val WEB_SOCKET_URL = "ws://10.0.2.2:8080"

    // uncomment below code
    //const val WEB_SOCKET_URL = "ws://192.168.4.1:8080"
    //   const val SOCKET_URL = "http://10.0.2.2:8000"
    //  const val SOCKET_URL = "http://192.168.4.1:8080"

    const val SOCKET_URL = WEB_SOCKET_URL
    const val IP_4TH_VALUE = "250"

    const val SOCKET_EVENT = "info"
    const val TYPE_GENERAL = "General"
    const val TYPE_WIFI_INFO = "WifiInfo"
    const val TYPE_APPLIANCE_STATUS = "ApplianceStatus"
    const val TYPE_DELETE_ROOM = "DeleteRoom"
    const val TYPE_DELETE_APPLIANCE = "DeleteAppliance"

    const val CONNECTION_SUCCESS = "Connection Success"
    const val CONNECTION_FAILED = "Connection Failed"

    const val IS_CONFIGURED = "IsConfigured"

    // Mode
    const val MODE_CONFIGURETION = "ConfigureMode"
    const val MODE_CONNECTION = "Connection"






}