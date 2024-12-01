package com.example.smartapp.ui.dashboard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.smartapp.R
import com.example.smartapp.base.BaseActivity
import com.example.smartapp.data.AppDatabase
import com.example.smartapp.data.preference.PreferenceManager
import com.example.smartapp.data.tables.AppInfo
import com.example.smartapp.databinding.ActivityDashboardBinding
import com.example.smartapp.listener.ItemAddListener
import com.example.smartapp.socket.ServerHandler
import com.example.smartapp.ui.ServerConnection
import com.example.smartapp.ui.WifiConnection
import com.example.smartapp.ui.appliances.AppliancesFragment
import com.example.smartapp.ui.configuration.ConfigurationActivity
import com.example.smartapp.ui.configuration.ConfigurationActivity.Companion
import com.example.smartapp.ui.login.LoginActivity
import com.example.smartapp.ui.rooms.RoomFragment
import com.example.smartapp.ui.rooms.RoomFragmentDirections
import com.example.smartapp.utils.AppConstants
import com.example.smartapp.utils.AppConstants.CONNECTION_FAILED
import com.example.smartapp.utils.AppConstants.CONNECTION_SUCCESS
import com.example.smartapp.utils.globalLiveData
import com.example.smartapp.utils.hideProgressBar
import com.example.smartapp.utils.showConfirmationDialog
import com.example.smartapp.utils.showSnackBar
import com.example.smartapp.utils.showToast
import kotlinx.coroutines.launch


class DashboardActivity : BaseActivity(), ItemAddListener {

    private var serverConnection: ServerConnection?= null
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var wifiConnection: WifiConnection

    private  var isConnected = false
    private var navController : NavController? = null
    companion object{
        var IS_CONNECTED_TO_DEVICE_HOTSPOT = true
        val TAG = DashboardActivity::class.java.simpleName
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        serverConnection = ServerConnection(this)
        wifiConnection = WifiConnection(this)

        addRoomFragmentAsDefault()
        binding.fab.visibility = View.GONE

        handleListener()

        if(ServerHandler.webSocket == null) {
            serverConnection!!.performServerConnection()
        }
        manageLiveData()
    }

    private fun addRoomFragmentAsDefault() {
        val action = RoomFragmentDirections.actionRoomFragment(TAG)
        navController = findNavController(R.id.nav_host_fragment_content_dashboard)
        navController!!.navigate(action)
        appBarConfiguration = AppBarConfiguration(navController!!.graph)
        setupActionBarWithNavController(navController!!, appBarConfiguration)
    }


    private fun manageLiveData() {

        globalLiveData.observe(this) { event ->
            event.getContentIfNotHandled()?.let { connectionModel ->
                hideProgressBar()
                if (connectionModel.connectionStatus == CONNECTION_SUCCESS) {
                    isConnected = true
                    showSnackBar(this@DashboardActivity, "Connected to Server")

                } else if (connectionModel.connectionStatus == CONNECTION_FAILED) {
                    wifiConnection.dismissDialog()
                    if (!isConnected) {
                        showSnackBar(this@DashboardActivity, "It seems server is not running.")

                    } else {
                        showSnackBar(this@DashboardActivity, "Dis-Connected from Server")
                    }

                    isConnected = false
                }
                invalidateOptionsMenu()
            }
        }
    }

    private fun handleListener() {
//        binding.fab.setOnClickListener { view ->
//            val navHostFragment =
//                supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_dashboard) as NavHostFragment
//            val currentFragment = navHostFragment.childFragmentManager.fragments.lastOrNull()
//
//            lifecycleScope.launch(Dispatchers.IO) {
//                if (currentFragment is RoomFragment) {
//                    val roomCount =
//                        AppDatabase.getDatabase(this@DashboardActivity).roomDao().getRoomsCount()
//                    runOnUiThread {
//                        if (roomCount >= AppConstants.THRESHOLD_ROOMS) {
//                            showToast(
//                                applicationContext,
//                                getString(R.string.room_count_exceed_message)
//                            )
//                        } else {
//                            showEditTextDialog(
//                                this@DashboardActivity,
//                                title = "ADD ROOM",
//                                typeOfDialog = ADD_ROOM
//                            ) {
//
//                                if (currentFragment is RoomFragment) {
//                                    val roomName = it as String
//                                    val room = Rooms(roomName, "")
//                                    lifecycleScope.launch(Dispatchers.IO) {
//                                        AppDatabase.getDatabase(this@DashboardActivity).roomDao()
//                                            .insertRoom(room)
//
//                                        val navHostFragment =
//                                            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_dashboard) as NavHostFragment
//                                        val currentFragment =
//                                            navHostFragment.childFragmentManager.fragments.lastOrNull()
//
//                                        if (currentFragment is RoomFragment) {
//                                            (currentFragment).fetchRoomData(this@DashboardActivity)
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//                else
//                { // Appliance Fragment
//                    val applianceCount = AppDatabase.getDatabase(this@DashboardActivity).applianceDao().getApplianceCountByRoomId(selectedRoomId)
//
//                    runOnUiThread {
//                        if (applianceCount >= AppConstants.THRESHOLD_APPLIANCES) {
//                            showToast(applicationContext, getString(R.string.appliance_count_exceed_message))
//                        } else {
//                            runOnUiThread {
//                                showEditTextDialog(
//                                    this@DashboardActivity,
//                                    title = "ADD APPLIANCE",
//                                    typeOfDialog = ADD_APPLIANCES
//                                ) {
//                                    val appliance = it
//
//                                    val appliances = Appliances(
//                                        roomId = selectedRoomId!!,
//                                        applianceName = appliance,
//                                        applianceStatus = false,
//                                        applianceColor = "",
//                                        applianceId = "R1"
//                                    )
//                                    lifecycleScope.launch(Dispatchers.IO) {
//                                        AppDatabase.getDatabase(this@DashboardActivity)
//                                            .applianceDao().insertAppliance(appliances)
//
//                                        if (currentFragment is AppliancesFragment) {
//                                            (currentFragment).fetchRoomApplianceData(this@DashboardActivity)
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_dashboard)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.action_reconnect_server)!!.isVisible = !isConnected
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.custom_menu, menu);

        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        // Example: handle different menu items

        if (id == R.id.action_reconnect_server) {
            serverConnection?.performServerConnection()
            return true
        }
       else if (id == R.id.action_logout) {
            showConfirmationDialog(this, getString(R.string.logout_text)){
                PreferenceManager.getInstance(this)!!.putBoolean(AppConstants.IS_LOGGED_IN, false)
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            return true
        }
        else if (id == R.id.action_config) {
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_dashboard) as NavHostFragment
          val currentFragment = navHostFragment.childFragmentManager.fragments.lastOrNull()

            if(currentFragment is RoomFragment) {
                navController?.navigate(R.id.action_Room_to_Config)
            }else if(currentFragment is AppliancesFragment){
                navController?.navigate(R.id.action_appliances_to_Config)

            }
            return true
        }
        else if (id == R.id.action_config_mode) {
            showConfirmationDialog(this, getString(R.string.switch_text)){
                addOrUpdateAppInfoDataToDB()
                startActivity(Intent(this, ConfigurationActivity::class.java))
                finish()
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }


    private fun addOrUpdateAppInfoDataToDB() {
        lifecycleScope.launch {
            val appInfoDao = AppDatabase.getDatabase(this@DashboardActivity).appInfoDao()
            val fetchedAppInfoDao = appInfoDao.getAppInfo()
            if (fetchedAppInfoDao == null) {
                val addInfo =
                    AppInfo(isDevicesAddedInConfigMode = false, isMacAddressUpdated = false)
                appInfoDao.insertInfo(addInfo)
            }else {
                   appInfoDao.updateMacAddressStatus(false, fetchedAppInfoDao._id)
            }
    }
}

    override fun onItemAddClick(item: Any) {}

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                123)
        }else{
            wifiConnection.scanWifiNetworks()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission granted, now you can scan for Wi-Fi networks
                wifiConnection.scanWifiNetworks()
            } else {
                // Permission denied
                showToast(applicationContext, "Location permission is required to scan Wi-Fi networks")
            }
        }
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


    fun showAddButton(b: Boolean) {
        if(b){
            binding.fab.visibility = View.VISIBLE }
        else{
            binding.fab.visibility = View.GONE
        }
    }

}