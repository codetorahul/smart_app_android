package com.example.smartapp.socket

import android.util.Log
import com.example.smartapp.MyWebSocketListener
import com.example.smartapp.utils.AppConstants
import io.socket.client.IO
import io.socket.client.Socket
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import java.net.URISyntaxException


object ServerHandler {
    var webSocket: WebSocket? =null

    @Synchronized
    fun setSocket(socketUrl: String = AppConstants.SOCKET_URL){
        try {
            val client = OkHttpClient()

            val request = Request.Builder().url(socketUrl).build()
            webSocket = client.newWebSocket(request, MyWebSocketListener())

            client.dispatcher.executorService.shutdown()

        }
        catch (e: URISyntaxException){
            Log.e("SOCKET EXCEPTION",e.printStackTrace().toString() )
        }
    }

    @Synchronized
    fun getSocket(): WebSocket?{
        return  webSocket
    }
}
/*
object ServerHandler {
    var mSocket: Socket? =null

    @Synchronized
    fun setSocket(){
        try {
            mSocket = IO.socket(AppConstants.SOCKET_URL)
        }
        catch (e: URISyntaxException){
            Log.e("SOCKET EXCEPTION",e.printStackTrace().toString() )
        }
    }

    @Synchronized
    fun getSocket(): Socket?{
        return  mSocket
    }
}*/
