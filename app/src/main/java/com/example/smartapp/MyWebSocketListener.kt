package com.example.smartapp

import com.example.smartapp.model.ConnectionModel
import com.example.smartapp.socket.SocketMessageModel
import com.example.smartapp.utils.AppConstants
import com.example.smartapp.utils.AppConstants.CONNECTION_FAILED
import com.example.smartapp.utils.AppConstants.CONNECTION_SUCCESS
import com.example.smartapp.utils.updateData
import com.example.smartapp.utils.updateDataInBackground
import com.google.gson.Gson
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.Response
import okio.ByteString

class   MyWebSocketListener : WebSocketListener() {
    override fun onOpen(webSocket: WebSocket, response: Response) {
        // WebSocket connection is opened
        val data = SocketMessageModel(
            type = AppConstants.TYPE_GENERAL,
            message = "Hello, JORAWAR!")

        webSocket.send(Gson().toJson(data))

    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        // Message received from the server
        println(">>> WEBSOCKET- Message Received : $text")
        updateDataInBackground(ConnectionModel(CONNECTION_SUCCESS,text))

    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        // Byte message received from the server
        println(">>> WEBSOCKET- Receiving bytes : " + bytes.hex())
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        // Server is closing the connection
        webSocket.close(1000, null)
        println(">>> WEBSOCKET- Closing : $code / $reason")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        // Connection failed2
        updateDataInBackground(ConnectionModel(CONNECTION_FAILED,""))

        t.printStackTrace()
        println(">>> WEBSOCKET- Failed : ${t.printStackTrace()}")

    }
}