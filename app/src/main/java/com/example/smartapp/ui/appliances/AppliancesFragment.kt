package com.example.smartapp.ui.appliances

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.smartapp.R
import com.example.smartapp.data.AppDatabase
import com.example.smartapp.databinding.FragmentApplianceBinding
import com.example.smartapp.listener.ItemListener
import com.example.smartapp.listener.OptionSelectListener
import com.example.smartapp.socket.ServerHandler
import com.example.smartapp.socket.SocketMessageModel
import com.example.smartapp.ui.rooms.Rooms
import com.example.smartapp.utils.AppConstants
import com.example.smartapp.utils.AppConstants.RENAME_APPLIANCES
import com.example.smartapp.utils.AppConstants.RENAME_ROOM
import com.example.smartapp.utils.showConfirmationDialog
import com.example.smartapp.utils.showEditTextDialog
import com.example.smartapp.utils.showOptionDialog
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class AppliancesFragment : Fragment(), ItemListener , OptionSelectListener {

    private var _binding: FragmentApplianceBinding? = null
    private val appliancesList = ArrayList<Appliances>()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var roomInfo: Rooms? =null
    private var applianceAdapter : ApplianceAdapter? =null

    companion object {
        var selectedRoomId: String? = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        arguments?.let {
            roomInfo= arguments?.getParcelable("Room-Info")
        }
        _binding = FragmentApplianceBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding!!.rvAppliance.layoutManager= GridLayoutManager(view.context, 3)
         applianceAdapter = ApplianceAdapter(appliancesList)
        applianceAdapter!!.setListener(this)
        _binding!!.rvAppliance.adapter = applianceAdapter

        fetchRoomApplianceData(requireContext())
    }

    fun fetchRoomApplianceData(context: Context) {
        lifecycleScope.launch {
            roomInfo?.let {
                _binding!!.tvRoomName.text = it.roomName
                selectedRoomId = it.roomId.toString()
                val applianceDao = AppDatabase.getDatabase(context).applianceDao()
                val listAppliances =  applianceDao.getApplianceByRoomId(roomId = it.roomId)
                println(":::::: ${it.roomName} Appliance's List: ${listAppliances.size}")
                appliancesList.clear()
                appliancesList.addAll(listAppliances)
                applianceAdapter!!.notifyDataSetChanged()


                if(appliancesList.size>0){
                    _binding!!.tvNoItemText.visibility = ViewGroup.GONE
                    _binding!!.rvAppliance.visibility = ViewGroup.VISIBLE
                }else{
                    _binding!!.tvNoItemText.visibility = ViewGroup.VISIBLE
                    _binding!!.rvAppliance.visibility = ViewGroup.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        selectedRoomId = null
    }

    override fun onItemClick(item: Any) {}

    override fun onStatusChange(status: Boolean, item: Any) {
        val updatedAppliance = item as Appliances
        updatedAppliance.applianceStatus = status
//        println("::: Appliance Id: ${updatedAppliance.applianceId} and Appliance Name: ${updatedAppliance.applianceName}")

        lifecycleScope.launch {
                val applianceDao = AppDatabase.getDatabase(requireContext()).applianceDao()
                applianceDao.updateAppliance(updatedAppliance)

            fetchRoomApplianceData(requireContext())
        }
        println(">>>> DATA: ${Gson().toJson(updatedAppliance)}")
        // Socket Event: Update toggle status on server
        ServerHandler.webSocket?.let {
            val data = SocketMessageModel(
                type = AppConstants.TYPE_APPLIANCE_STATUS,
                roomId = updatedAppliance.roomId,
                applianceId = updatedAppliance.applianceId.toString(),
                applianceStatus = updatedAppliance.applianceStatus)

           /* if (it.connected())
                it.emit(AppConstants.SOCKET_EVENT, Gson().toJson(data))
            */


            it.send( Gson().toJson(data))

        }

    }

    override fun onItemLongClick(item: Any) {
        val selectedAppliance = item as Appliances
        showOptionDialog(requireContext(), "Appliance Name -  "+selectedAppliance.applianceName, this, selectedAppliance )

    }

    override fun onOptionSelect(optionType: String, item: Any) {
        val appliances = item as Appliances
        if(optionType == AppConstants.OPTION_DELETE){
            showConfirmationDialog(requireContext(), requireContext().getString(R.string.delete_text)){
                lifecycleScope.launch ( Dispatchers.IO) {
                    AppDatabase.getDatabase(requireContext()).applianceDao().deleteAppliance(appliances)
                    fetchRoomApplianceData(requireContext())

                    // Socket Event: Delete Appliance on Server
                    ServerHandler.webSocket?.let {
                        val data = SocketMessageModel(
                            type = AppConstants.TYPE_DELETE_APPLIANCE,
                            roomId = appliances.roomId,
                            applianceId = appliances.applianceId.toString(),
                            applianceStatus = appliances.applianceStatus)

                  /*      if (it.connected())
                            it.emit(AppConstants.SOCKET_EVENT, Gson().toJson(data))
                   */
                        println(">>>> DATA: ${Gson().toJson(data)}")
                            it.send(Gson().toJson(data))


                    }
                }
            }
        }else{
            showEditTextDialog(
                requireContext(),
                positiveButton = "RENAME",
                title = "RENAME APPLIANCES",
                nameToUpdate = appliances.applianceName,
                typeOfDialog = RENAME_APPLIANCES
            ){
                val renamedValue = it
                appliances.applianceName = renamedValue
                lifecycleScope.launch(Dispatchers.IO) {
                    AppDatabase.getDatabase(requireContext()).applianceDao().updateAppliance(appliances)
                    fetchRoomApplianceData(requireContext())
                }
            }
        }
    }
}