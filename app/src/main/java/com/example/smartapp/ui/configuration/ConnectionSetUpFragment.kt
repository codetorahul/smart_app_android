package com.example.smartapp.ui.configuration

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartapp.R
import com.example.smartapp.data.AppDatabase
import com.example.smartapp.databinding.FragmentConnectionSetupBinding
import com.example.smartapp.listener.DialogListener
import com.example.smartapp.socket.ServerHandler
import com.example.smartapp.socket.SocketMessageModel
import com.example.smartapp.ui.ServerConnection
import com.example.smartapp.ui.WifiConnection
import com.example.smartapp.ui.dashboard.DashboardActivity.Companion.IS_CONNECTED_TO_DEVICE_HOTSPOT
import com.example.smartapp.data.tables.Rooms
import com.example.smartapp.utils.AppConstants.CONNECTION_FAILED
import com.example.smartapp.utils.AppConstants.CONNECTION_SUCCESS
import com.example.smartapp.utils.AppConstants.DIALOG_LOCATION_ENABLE
import com.example.smartapp.utils.AppConstants.DIALOG_WIFI_INFO
import com.example.smartapp.utils.AppConstants.MODE_CONNECTION
import com.example.smartapp.utils.AppConstants.OPTION_CANCEL
import com.example.smartapp.utils.AppConstants.OPTION_SEND
import com.example.smartapp.utils.checkLocation
import com.example.smartapp.utils.getRoomId
import com.example.smartapp.utils.globalLiveData
import com.example.smartapp.utils.hideProgressBar
import com.example.smartapp.utils.showCustomDialog
import com.example.smartapp.utils.showToast
import com.google.gson.Gson
import kotlinx.coroutines.launch


class ConnectionSetUpFragment : Fragment(), DialogListener {

    private var _binding: FragmentConnectionSetupBinding? = null
    private val roomWithAppliancesList = ArrayList<RoomsWithAppliances>()
    private val roomListToPopulate = ArrayList<RoomsWithAppliances>()

    private val binding get() = _binding!!
    private var sectionedAdapter : SectionedHeaderAdapter? =null

    var serverConnection: ServerConnection?= null
    var wifiConnection: WifiConnection?= null
    private  var isConnected = false
        private  var fetchedMacAddress: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConnectionSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (context as ConfigurationActivity).showDoneButton(false)
        (context as ConfigurationActivity).showAddButton(false)
        _binding!!.rvConfig.layoutManager= LinearLayoutManager(requireContext())

        sectionedAdapter = SectionedHeaderAdapter(requireContext(),roomListToPopulate)
        _binding!!.rvConfig.adapter = sectionedAdapter

        fetchRoomWithTheirApplianceData(requireContext())

        _binding!!.btnConnection.setOnClickListener{
            if(context is ConfigurationActivity) {
                serverConnection = (context as ConfigurationActivity).serverConnection
                wifiConnection = (context as ConfigurationActivity).wifiConnection
                wifiConnection?.setListener(this)

                serverConnection?.performServerConnection()
            }
        }

        setLiveData()
    }

    private fun setLiveData() {
        globalLiveData.observe(viewLifecycleOwner){
            if( it.connectionStatus == CONNECTION_SUCCESS){
                isConnected = true
               try {
                   //TODO: Below code is for testing. UNComment and Use.
                  // fetchedMacAddress = Integer.parseInt(it.receivedMacAddress).toString()

                   // TODO: Comment below code if you want to use above code .
                   fetchedMacAddress = it.receivedMacAddress
               }catch (error:Error){

               }
                showToast(requireContext().applicationContext, "Connected to Server")
                if(checkLocation(context as Activity, this) && !IS_CONNECTED_TO_DEVICE_HOTSPOT) {
                    requestLocationPermission()
                }
            }else if (it.connectionStatus == CONNECTION_FAILED){
                hideProgressBar()
                wifiConnection?.dismissDialog()
                if(!isConnected){
                    (context as Activity).runOnUiThread {
                        showToast(requireContext().applicationContext, "It seems server is not running.")
                    }
                }else{
                    (context as Activity).runOnUiThread {
                        showToast(requireContext().applicationContext, "Dis-Connected from Server")
                    }
                }

                // code for testing only
                /*  if(checkLocation(this, this)) {
                      requestLocationPermission()
                  }*/

                isConnected= false
            }
        }
    }

    private fun fetchRoomWithTheirApplianceData(context: Context) {
        lifecycleScope.launch {
            val appDatabase = AppDatabase.getDatabase(context)
            val roomDao = appDatabase.roomDao()
            val listRooms =  roomDao.getAllRooms()

            roomWithAppliancesList.clear()
            roomListToPopulate.clear()
            listRooms.map { room ->
                fetchRoomApplianceData(room)
             }
        }
    }


    private suspend fun fetchRoomApplianceData(roomInfo: Rooms) {
            roomInfo.let {
                var _roomId  = it._id
                var roomId  = getRoomId(it)
                var roomName  = it.roomName
                var roomColor  = it.roomColor
                val isRoomIdUpdated = it.isRoomIdUpdated

                val applianceDao = AppDatabase.getDatabase(requireContext()).applianceDao()
                val listAppliances =  applianceDao.getApplianceByRoomId(roomId = roomId.trim())

                println(":::::: RoomName: ${it.roomName} RoomId: ${roomId.trim()} Appliance's List: ${listAppliances.size}")

                val roomToAdd= RoomsWithAppliances(_roomId,roomId, roomName, roomColor , listAppliances, isRoomIdUpdated)
                roomWithAppliancesList.add(roomToAdd)

                if(isRoomIdUpdated){
                    roomListToPopulate.add(roomToAdd)
                }

                if(isRoomMacAddressUpdate()){
                    sectionedAdapter?.notifyDataSetChanged()
                }

        }
    }

    private fun isRoomMacAddressUpdate(): Boolean {
       for(i in 0..<roomListToPopulate.size){
                if(roomListToPopulate[i].isMacAddressAdded){
                    return true
                }
        }
        return false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onOptionClick(optionType: String, others: Any, dialogType: String) {
        hideProgressBar()
        if(optionType == OPTION_CANCEL && dialogType == DIALOG_LOCATION_ENABLE){
            showCustomDialog(requireActivity(), message = getString(R.string.denied_location_dialog_message)
                , showPositiveButton = false, showNegativeButton = true, negativeText = "OK")
        }
        else if(optionType == OPTION_SEND && dialogType == DIALOG_WIFI_INFO){
            // Socket Event: Update toggle status on server
            val socketMessage = others as SocketMessageModel
            ServerHandler.webSocket?.send(Gson().toJson(socketMessage))

            updateMacAddressOfRoom()
            }

}

    private fun updateMacAddressOfRoom() {
       if(!fetchedMacAddress.isNullOrEmpty() && roomWithAppliancesList.size>0){
           for (room in roomWithAppliancesList) {
               if(!room.isMacAddressAdded){
                   val oldRoomId = room.roomId
                   room.roomId = fetchedMacAddress!!
                   room.isMacAddressAdded = true
                   println(">>>>>> ROOM-NAME: ${room.roomName} OLD-ROOM-ID:${oldRoomId.trim()} , NEW-ID: ${fetchedMacAddress} ")

                   roomListToPopulate.add(room)
                   updateRoomListInDB(fetchedMacAddress!!, oldRoomId)
                   break
               }
           }
           sectionedAdapter?.notifyDataSetChanged()
       }
    }

    private fun updateRoomListInDB(newRoomId: String, oldRoomId: String) {
        lifecycleScope.launch {
            AppDatabase.getDatabase(requireContext()).roomDao().updateRoomIdAndItsStatus(Integer.parseInt(oldRoomId),newRoomId,true)
            AppDatabase.getDatabase(requireContext()).applianceDao().updateRoomIdForAppliance(oldRoomId.trim(),newRoomId)
        }
    }


    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                123)
        }else{
            wifiConnection?.scanWifiNetworks()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission granted, now you can scan for Wi-Fi networks
                wifiConnection?.scanWifiNetworks()
            } else {
                // Permission denied
                Toast.makeText(requireContext().applicationContext, "Location permission is required to scan Wi-Fi networks", Toast.LENGTH_SHORT).show()
            }
        }
    }


}