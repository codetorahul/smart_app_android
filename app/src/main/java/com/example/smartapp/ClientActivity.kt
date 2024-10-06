package com.example.smartapp

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import android.text.format.Formatter
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.smartapp.socket.ServerHandler
import com.example.smartapp.style.theme.SmartAppTheme
import com.example.smartapp.utils.AppConstants
import io.socket.client.Socket
import okhttp3.WebSocket

class ClientActivity : ComponentActivity() {

    lateinit var mSocketInstance: WebSocket
    var socket : Socket?=null
        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    ){
                        it.send("....Hello Jenkins....")

                    }
                }
            }
        }
            val ipAddress = getWifiIpAddress()
            println("Connected WiFi IP Address: $ipAddress")
    }

    private fun getWifiIpAddress(): String? {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo
        val ip = wifiInfo.ipAddress
        return Formatter.formatIpAddress(ip)
    }

    @Composable
    fun Greeting(name: String, modifier: Modifier = Modifier, setCallback : (socket: WebSocket) -> Unit) {

        Column {
            Text(
                text = "Hell $name!",
                modifier = modifier
            )

            Button(onClick = {
//                val client = OkHttpClient()
//                val request = Request.Builder().url("http://10.0.2.2:8080").build()
//                val listener = MyWebSocketListener()
//
//                mSocketInstance = client.newWebSocket(request, listener)
//
//                client.dispatcher.executorService.shutdown()
//
//                setCallback(mSocketInstance)

                ServerHandler.setSocket()

               // socket = ServerHandler.mSocket
                socket!!.connect()
                socket!!.emit(AppConstants.SOCKET_EVENT, "Hello Server")

                socket!!.on(AppConstants.SOCKET_EVENT){ args ->
                    println(args[0])

                    if(args[0] !=null){
                        val value = args[0] as String
                        println("Value received: "+value)
                    }
                }

            }) {
                Text(
                    text = "Connect With Server"
                )
            }



            Button(onClick = {


                socket!!.emit("counter", "Is that ok if i remain connected with you")

            }) {
                Text(
                    text = "Message AGAIN to Server"
                )
            }
        }


    }

    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
        SmartAppTheme {
          //  Greeting("Android")
        }
    }
}