package com.example.smartapp.ui.dashboard

import android.Manifest
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.smartapp.R
import com.example.smartapp.base.BaseActivity
import com.example.smartapp.data.AppDatabase
import com.example.smartapp.data.preference.PreferenceManager
import com.example.smartapp.databinding.ActivityDashboardBinding
import com.example.smartapp.listener.ItemAddListener
import com.example.smartapp.socket.ServerHandler
import com.example.smartapp.socket.SocketMessageModel
import com.example.smartapp.ui.appliances.Appliances
import com.example.smartapp.ui.appliances.AppliancesFragment
import com.example.smartapp.ui.appliances.AppliancesFragment.Companion.selectedRoomId
import com.example.smartapp.ui.login.LoginActivity
import com.example.smartapp.ui.rooms.RoomFragment
import com.example.smartapp.ui.rooms.Rooms
import com.example.smartapp.utils.AppConstants
import com.example.smartapp.utils.AppConstants.ADD_APPLIANCES
import com.example.smartapp.utils.AppConstants.ADD_ROOM
import com.example.smartapp.utils.AppConstants.CONNECTION_FAILED
import com.example.smartapp.utils.AppConstants.CONNECTION_SUCCESS
import com.example.smartapp.utils.AppConstants.WIFI_PASSWORD
import com.example.smartapp.utils.globalLiveData
import com.example.smartapp.utils.showConfirmationDialog
import com.example.smartapp.utils.showEditTextDialog
import com.example.smartapp.utils.showToast
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.WebSocket


class DashboardActivity : BaseActivity(), ItemAddListener {

  //  private var socket: Socket?=null
    private var webSocket: WebSocket?=null
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityDashboardBinding
    private var showMenuRetry  = false
    private  var isConnected = false
private var alertBuilder: AlertDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)


       // connectToSocket()
        connectToWebSocket()

        val navController = findNavController(R.id.nav_host_fragment_content_dashboard)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        handleListener()
        manageLiveData()
    }

    private fun manageLiveData() {

        globalLiveData.observe(this){
            if( it == CONNECTION_SUCCESS){
                isConnected = true
                showMenuRetry = false
                showToast(this, "Connected to Server")
                requestLocationPermission()
            }else if (it == CONNECTION_FAILED){
                showMenuRetry = true
                alertBuilder?.dismiss()
                if(!isConnected){
                    showToast(this, "It seems server is not running.")
                }else{
                    showToast(this, "Dis-Connected from Server")
                }
                isConnected= false
            }
            invalidateOptionsMenu()
        }
    }

    private fun handleListener() {
        binding.fab.setOnClickListener { view ->
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_dashboard) as NavHostFragment
            val currentFragment = navHostFragment.childFragmentManager.fragments.lastOrNull()

            lifecycleScope.launch(Dispatchers.IO) {
                if (currentFragment is RoomFragment) {
                    val roomCount =
                        AppDatabase.getDatabase(this@DashboardActivity).roomDao().getRoomsCount()
                    runOnUiThread {
                        if (roomCount >= AppConstants.THRESHOLD_ROOMS) {
                            showToast(
                                this@DashboardActivity,
                                getString(R.string.room_count_exceed_message)
                            )
                        } else {
                            showEditTextDialog(
                                this@DashboardActivity,
                                title = "ADD ROOM",
                                typeOfDialog = ADD_ROOM
                            ) {

                                if (currentFragment is RoomFragment) {
                                    val roomName = it as String
                                    val room = Rooms(roomName, "")
                                    lifecycleScope.launch(Dispatchers.IO) {
                                        AppDatabase.getDatabase(this@DashboardActivity).roomDao()
                                            .insertRoom(room)

                                        val navHostFragment =
                                            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_dashboard) as NavHostFragment
                                        val currentFragment =
                                            navHostFragment.childFragmentManager.fragments.lastOrNull()

                                        if (currentFragment is RoomFragment) {
                                            (currentFragment).fetchRoomData(this@DashboardActivity)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    val applianceCount = AppDatabase.getDatabase(this@DashboardActivity).applianceDao().getApplianceCountByRoomId(selectedRoomId)

                    runOnUiThread {
                        if (applianceCount >= AppConstants.THRESHOLD_APPLIANCES) {
                            showToast(this@DashboardActivity, getString(R.string.appliance_count_exceed_message))
                        } else {
                            runOnUiThread {
                                showEditTextDialog(
                                    this@DashboardActivity,
                                    title = "ADD APPLIANCE",
                                    typeOfDialog = ADD_APPLIANCES
                                ) {
                                    val appliance = it

                                    val appliances = Appliances(
                                        roomId = selectedRoomId!!,
                                        applianceName = appliance,
                                        applianceStatus = false,
                                        applianceColor = ""
                                    )
                                    lifecycleScope.launch(Dispatchers.IO) {
                                        AppDatabase.getDatabase(this@DashboardActivity)
                                            .applianceDao().insertAppliance(appliances)

                                        if (currentFragment is AppliancesFragment) {
                                            (currentFragment).fetchRoomApplianceData(this@DashboardActivity)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_dashboard)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(com.example.smartapp.R.menu.custom_menu, menu);

        val menuItemRetry = menu!!.findItem(com.example.smartapp.R.id.action_retry)

        if(showMenuRetry){
            menuItemRetry.setVisible(true)
        }else{
            menuItemRetry.setVisible(false)
        }
        return true;
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        // Example: handle different menu items
        if (id == com.example.smartapp.R.id.action_logout) {
            showConfirmationDialog(this, getString(R.string.logout_text)){
                PreferenceManager.getInstance(this)!!.putBoolean(AppConstants.IS_LOGGED_IN, false)
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            return true
        }
        else if (id == com.example.smartapp.R.id.action_retry) {
            connectToWebSocket()

            return true
        }
        return super.onOptionsItemSelected(item)
    }




    override fun onItemAddClick(item: Any) {

    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                123)
        }else{
            scanWifiNetworks()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission granted, now you can scan for Wi-Fi networks
                scanWifiNetworks()
            } else {
                // Permission denied
                Toast.makeText(this, "Location permission is required to scan Wi-Fi networks", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun scanWifiNetworks() {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

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
                            this@DashboardActivity,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return
                    }
                    getScanResults(wifiManager.scanResults)
                  //  unregisterReceiver(this)

                } else {
                    // Scan failed
                    Toast.makeText(applicationContext, "Wi-Fi scan failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        registerReceiver(wifiScanReceiver, intentFilter)

        // Start a Wi-Fi scan
        val success = wifiManager.startScan()
        if (!success) {
            // Scan failed
            Toast.makeText(this, "Wi-Fi scan failed to start", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getScanResults(scanResults: List<ScanResult>) {
//        val scanResults = wifiManager.scanResults
//        for (scanResult in scanResults) {
//            Log.d("WiFiScan", "SSID: ${scanResult.SSID}, BSSID: ${scanResult.BSSID}, Signal: ${scanResult.level}")
//        }

        for (scanResult in scanResults) {
        Log.d("WiFiScan", "SSID: ${scanResult.SSID}, BSSID: ${scanResult.BSSID}, Signal: ${scanResult.level}")
        }

        val wifiList = scanResults.map { "${it.SSID} (${it.BSSID})" }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Available Wi-Fi Networks")
        builder.setItems(wifiList.toTypedArray()) { dialog, which ->
            // Handle Wi-Fi network selection
            val selectedNetwork = scanResults[which]

            showEditTextDialog(
                this,
                positiveButton = "SEND",
                title = selectedNetwork.SSID,
                nameToUpdate = "",
                typeOfDialog = WIFI_PASSWORD
            ){
                val password = it

                // Socket Event: Update toggle status on server
                ServerHandler.webSocket?.let { it ->
                    val data = SocketMessageModel(
                        type = AppConstants.TYPE_WIFI_INFO,
                        ssdId = selectedNetwork.SSID,
                        password = password)

                    /*if (it.connected())
                        it.emit(AppConstants.SOCKET_EVENT, Gson().toJson(data))*/

                        it.send(Gson().toJson(data))
                }
            }
        }

        alertBuilder = builder.create()
        alertBuilder!!.show()
    }

  /* private fun connectToSocket(){
        ServerHandler.setSocket()

        socket = ServerHandler.mSocket
        socket!!.connect()

//       val data = SocketMessageModel(type = AppConstants.TYPE_WIFI_INFO, ssdId = "Rahul 5G", password = "123456")
//       val gson = Gson().toJson(data)
//        socket!!.emit(AppConstants.SOCKET_EVENT, gson)

        socket!!.on(AppConstants.SOCKET_EVENT){ args ->
            println(args[0])

            if(args[0] !=null){
                val value = args[0] as String
                println("Value received: "+value)
            }
        }

    }*/

    private fun connectToWebSocket(){
        ServerHandler.setSocket()

        webSocket = ServerHandler.webSocket

//       val data = SocketMessageModel(type = AppConstants.TYPE_WIFI_INFO, ssdId = "Rahul 5G", password = "123456")
//       val gson = Gson().toJson(data)
//        socket!!.emit(AppConstants.SOCKET_EVENT, gson)

//        webSocket!!.to(AppConstants.SOCKET_EVENT){ args ->
//            println(args[0])
//
//            if(args[0] !=null){
//                val value = args[0] as String
//                println("Value received: "+value)
//            }
//        }

    }
}