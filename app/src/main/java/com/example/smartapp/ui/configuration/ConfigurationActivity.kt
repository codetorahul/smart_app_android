package com.example.smartapp.ui.configuration

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.smartapp.R
import com.example.smartapp.base.BaseActivity
import com.example.smartapp.data.AppDatabase
import com.example.smartapp.data.tables.AppInfo
import com.example.smartapp.databinding.ActivityConfigurationBinding
import com.example.smartapp.ui.ServerConnection
import com.example.smartapp.ui.WifiConnection
import com.example.smartapp.data.tables.Appliances
import com.example.smartapp.ui.appliances.AppliancesFragment
import com.example.smartapp.ui.appliances.AppliancesFragment.Companion.selectedRoomId
import com.example.smartapp.ui.appliances.AppliancesFragment.Companion.selectedRoomName
import com.example.smartapp.ui.rooms.RoomFragment
import com.example.smartapp.ui.rooms.RoomFragmentDirections
import com.example.smartapp.data.tables.Rooms
import com.example.smartapp.ui.dashboard.DashboardActivity
import com.example.smartapp.utils.AppConstants
import com.example.smartapp.utils.AppConstants.ADD_APPLIANCES
import com.example.smartapp.utils.AppConstants.ADD_ROOM
import com.example.smartapp.utils.AppConstants.THRESHOLD_APPLIANCES
import com.example.smartapp.utils.showConfirmationDialog
import com.example.smartapp.utils.showEditTextDialog
import com.example.smartapp.utils.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ConfigurationActivity : BaseActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityConfigurationBinding
    private  val  infoHashMap = hashMapOf<String, ArrayList<String>>()
    private val defaultAppliances = arrayListOf<String>()

    var appInfo: AppInfo?= null

    var serverConnection: ServerConnection?= null
    var wifiConnection: WifiConnection?= null

    companion object{
        val TAG = ConfigurationActivity::class.java.simpleName
        var isDevicesListed =false
        var isDeviceConfigured = false
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.config_menu, menu);
        return true

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_save) {
            performSaveAction()
            return true
        }
        else if (id == R.id.action_retry) {
            //  performServerConnection()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun performSaveAction() {
        showConfirmationDialog(this, getString(R.string.save_text)) {

            lifecycleScope.launch {
                val appInfoDao = AppDatabase.getDatabase(this@ConfigurationActivity).appInfoDao()

                if (appInfo != null) {
                    appInfoDao.updateMacAddressStatus(true, appInfo!!._id)
                }
                startActivity(Intent(this@ConfigurationActivity, DashboardActivity::class.java))
            }
        }

    }

    private fun performAction() {
        isDevicesListed = true
        updateStepsView()
        showConfirmationDialog(this, getString(R.string.save_text)) {

            lifecycleScope.launch {
                var updatedInfo =
                    AppInfo(isDevicesAddedInConfigMode = true, isMacAddressUpdated = false)
                if(appInfo != null) {
                    updatedInfo =  AppInfo(_id = appInfo!!._id, isDevicesAddedInConfigMode = true, isMacAddressUpdated = appInfo!!.isMacAddressUpdated)
                }
                AppDatabase.getDatabase(this@ConfigurationActivity).appInfoDao().updateAppInfo(updatedInfo)
            }
            openConnectionFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityConfigurationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.title = getString(R.string.configuration)
        setSupportActionBar(binding.toolbar)

        serverConnection = ServerConnection(this)
        wifiConnection = WifiConnection(this)

        getAppInfoDataFromDB()
        updateStepsView()
        handleListener()
    }


    private fun getAppInfoDataFromDB() {
        lifecycleScope.launch {
            val appInfoDao =   AppDatabase.getDatabase(this@ConfigurationActivity).appInfoDao()
            appInfo =  appInfoDao.getAppInfo()

            addRoomFragmentAsDefault()

            if(appInfo!=null){
                isDevicesListed = appInfo!!.isDevicesAddedInConfigMode
                isDeviceConfigured = appInfo!!.isMacAddressUpdated
                updateStepsView()

                if(isDevicesListed){
                    println(":::: IS DEVICELISTED: ${isDevicesListed}")
                    openConnectionFragment()
                }
            }
        }
    }

    private fun addRoomFragmentAsDefault() {
        val action = RoomFragmentDirections.actionRoomFragment(TAG)
        val navController = findNavController(R.id.nav_host_fragment_content_configuration)
        navController.navigate(action)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    private fun openConnectionFragment(){
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_configuration) as NavHostFragment
        val currentFragment = navHostFragment.childFragmentManager.primaryNavigationFragment

        val navController = findNavController(R.id.nav_host_fragment_content_configuration)

        if (currentFragment is RoomFragment) {
            navController.navigate(R.id.action_Room_to_ConnectionSetUp)
        }else{
            navController.navigate(R.id.action_Appliance_to_ConnectionSetUp)
        }
    }

    private fun updateStepsView() {
        if(!isDevicesListed){
            binding.view1.setBackgroundColor(getColor(R.color.grey))
            binding.tvStep1.setTextColor(getColor(R.color.grey))
        }else{
            binding.view1.setBackgroundColor(getColor(R.color.teal_700))
            binding.tvStep1.setTextColor(getColor(R.color.teal_700))
        }

        if(!isDeviceConfigured){
            binding.view2.setBackgroundColor(getColor(R.color.grey))
            binding.tvStep2.setTextColor(getColor(R.color.grey))
        }else{
            binding.view2.setBackgroundColor(getColor(R.color.teal_700))
            binding.tvStep2.setTextColor(getColor(R.color.teal_700))
        }

    }

    private fun prepareApplianceIdList() {
        for( i in 1..THRESHOLD_APPLIANCES){
            defaultAppliances.add("R$i")
        }
    }

    private fun handleListener() {

        binding.btnDone.setOnClickListener{
            performAction()
        }
        binding.fab.setOnClickListener { view ->
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id    .nav_host_fragment_content_configuration) as NavHostFragment
            val currentFragment = navHostFragment.childFragmentManager.fragments.lastOrNull()

            lifecycleScope.launch(Dispatchers.IO) {
                if (currentFragment is RoomFragment) {
                    val roomCount =
                        AppDatabase.getDatabase(this@ConfigurationActivity).roomDao().getRoomsCount()
                    runOnUiThread {
                        if (roomCount >= AppConstants.THRESHOLD_ROOMS) {
                            showToast(
                                applicationContext,
                                getString(R.string.room_count_exceed_message)
                            )
                        } else {
                            showEditTextDialog(
                                this@ConfigurationActivity,
                                title = "ADD ROOM",
                                typeOfDialog = ADD_ROOM
                            ) {

                                if (currentFragment is RoomFragment) {
                                    val roomName = it as String
                                    val room = Rooms(roomName=roomName, roomColor = "", roomId = "", isRoomIdUpdated = false)
                                    updateLocalInfoHashMap(roomName)
                                    lifecycleScope.launch(Dispatchers.IO) {
                                        AppDatabase.getDatabase(this@ConfigurationActivity).roomDao()
                                            .insertRoom(room)

                                        val navHostFragment =
                                            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_configuration) as NavHostFragment
                                        val currentFragment =
                                            navHostFragment.childFragmentManager.fragments.lastOrNull()

                                        if (currentFragment is RoomFragment) {
                                            (currentFragment).fetchRoomData(this@ConfigurationActivity)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                else {
                    val applianceCount = AppDatabase.getDatabase(this@ConfigurationActivity).applianceDao().getApplianceCountByRoomId(selectedRoomId)

                    runOnUiThread {
                        if (applianceCount >= THRESHOLD_APPLIANCES) {
                            showToast(applicationContext, getString(R.string.appliance_count_exceed_message))
                        } else {
                            runOnUiThread {
                                showEditTextDialog(
                                    this@ConfigurationActivity,
                                    title = "ADD APPLIANCE",
                                    typeOfDialog = ADD_APPLIANCES
                                ) {
                                    val appliance = it
                                    val createdApplianceId = fetchApplianceId()
                                    val appliances = Appliances(
                                        roomId = selectedRoomId!!,
                                        applianceName = appliance,
                                        applianceStatus = false,
                                        applianceColor = "",
                                        applianceId = createdApplianceId
                                    )
                                    lifecycleScope.launch(Dispatchers.IO) {
                                        AppDatabase.getDatabase(this@ConfigurationActivity)
                                            .applianceDao().insertAppliance(appliances)

                                        if (currentFragment is AppliancesFragment) {
                                            (currentFragment).fetchRoomApplianceData(this@ConfigurationActivity)
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

    private fun fetchApplianceId(): String {
        var applianceId = ""
        if(infoHashMap.containsKey(selectedRoomName)){
            val data = infoHashMap[selectedRoomName]

            if(!data.isNullOrEmpty()) {
                println(" Size BEFORE delete: "+data.size +" , For Room- "+ selectedRoomName)
                applianceId = data[0]
                data.removeAt(0)
                println(" Size AFTER delete: "+data.size +" , For Room- "+ selectedRoomName)
                println(" Picked Application Id: "+applianceId +" For Room- "+ selectedRoomName)
            }
        }
        return  applianceId
    }

    private fun updateLocalInfoHashMap(roomName: String) {
        if(!infoHashMap.containsKey(roomName)){
            prepareApplianceIdList()
            infoHashMap[roomName] = defaultAppliances
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_configuration)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    fun showDoneButton(b: Boolean) {
        if(b){
            binding.btnDone.visibility = View.VISIBLE }
        else{
            binding.btnDone.visibility = View.GONE
        }
    }
    fun showAddButton(b: Boolean) {
        if(b){
            binding.fab.visibility = View.VISIBLE }
        else{
            binding.fab.visibility = View.GONE
        }
    }

}
