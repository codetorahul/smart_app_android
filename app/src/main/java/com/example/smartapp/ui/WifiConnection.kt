package com.example.smartapp.ui

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.smartapp.listener.DialogListener
import com.example.smartapp.socket.ServerHandler
import com.example.smartapp.socket.SocketMessageModel
import com.example.smartapp.utils.AppConstants
import com.example.smartapp.utils.AppConstants.DIALOG_WIFI_INFO
import com.example.smartapp.utils.AppConstants.OPTION_SEND
import com.example.smartapp.utils.AppConstants.WIFI_PASSWORD
import com.example.smartapp.utils.hideProgressBar
import com.example.smartapp.utils.showEditTextDialog
import com.example.smartapp.utils.showToast
import com.google.gson.Gson

class WifiConnection(val context: Context) {

    private  var wifiManager: WifiManager? = null
    private var alertBuilder: AlertDialog? = null
    private  var dialogListener: DialogListener?= null

    fun setListener(dialogListener: DialogListener){
        this.dialogListener = dialogListener
    }
     fun scanWifiNetworks() {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        // Ensure Wi-Fi is enabled
        if (!wifiManager.isWifiEnabled) {
            wifiManager.isWifiEnabled = true
        }

        // Register a broadcast receiver to get scan results
        val wifiScanReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val success = intent?.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false) ?: false
                if (success) {
                    if (ActivityCompat.checkSelfPermission(
                            context as Activity,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return
                    }
                    println("::::::::: WIFI FETCHED")
                    showToast(context.applicationContext, "::::::::: WIFI FETCHED")
                    getScanResults(wifiManager.scanResults)
                    context.unregisterReceiver(this)
                } else {
                    // Scan failed
                    Toast.makeText(context!!.applicationContext, "Wi-Fi scan failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        context.registerReceiver(wifiScanReceiver, intentFilter)

        // Start a Wi-Fi scan
        val success = wifiManager.startScan()
        if (!success) {

            if(this.wifiManager !=null && this.wifiManager!!.scanResults.isNotEmpty()) {
                getScanResults(this.wifiManager!!.scanResults)
            }else{

                // Scan failed
                Toast.makeText(context, "Wi-Fi scan failed to start", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getScanResults(scanResults: List<ScanResult>) {
//        val scwifiManageranResults = wifiManager.scanResults
//        for (scanResult in scanResults) {
//            Log.d("WiFiScan", "SSID: ${scanResult.SSID}, BSSID: ${scanResult.BSSID}, Signal: ${scanResult.level}")
//        }

        for (scanResult in scanResults) {
            Log.d("WiFiScan", "SSID: ${scanResult.SSID}, BSSID: ${scanResult.BSSID}, Signal: ${scanResult.level}")
        }

        val wifiList = scanResults.map { "${it.SSID} (${it.BSSID})" }

        showDialogOfWifiList(wifiList,scanResults)

    }

    private fun showDialogOfWifiList(wifiList: List<String>, scanResults: List<ScanResult>) {
        val builder = AlertDialog.Builder(context)
        builder.setCancelable(false)
        builder.setTitle("Available Wi-Fi Networks")
        builder.setItems(wifiList.toTypedArray()) { dialog, which ->
            // Handle Wi-Fi network selection
            val selectedNetwork = scanResults[which]

            showEditTextDialog(
                context,
                positiveButton = "SEND",
                title = selectedNetwork.SSID,
                nameToUpdate = "",
                typeOfDialog = WIFI_PASSWORD,
                taskToPerformOnCancel = { hideProgressBar() }

            ){
                val password = it

                val data = SocketMessageModel(
                    type = AppConstants.TYPE_WIFI_INFO,
                    ssdId = selectedNetwork.SSID,
                    password = password)

                dialogListener?.onOptionClick(OPTION_SEND, data, DIALOG_WIFI_INFO)
            }
        }

        alertBuilder = builder.create()
        alertBuilder!!.show()
    }

    fun dismissDialog() {
        alertBuilder?.dismiss()
    }


}