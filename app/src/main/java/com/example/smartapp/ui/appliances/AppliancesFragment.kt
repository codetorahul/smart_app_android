package com.example.smartapp.ui.appliances

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.smartapp.R
import com.example.smartapp.data.AppDatabase
import com.example.smartapp.data.tables.Appliances
import com.example.smartapp.databinding.FragmentApplianceBinding
import com.example.smartapp.listener.ItemListener
import com.example.smartapp.listener.OptionSelectListener
import com.example.smartapp.socket.ServerHandler
import com.example.smartapp.socket.SocketMessageModel
import com.example.smartapp.ui.configuration.ConfigurationActivity
import com.example.smartapp.data.tables.Rooms
import com.example.smartapp.ui.dashboard.DashboardActivity
import com.example.smartapp.utils.AppConstants
import com.example.smartapp.utils.AppConstants.RENAME_APPLIANCES
import com.example.smartapp.utils.getRoomId
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
    private lateinit var screenType: String
    private var isRoomIdUpdated: Boolean = false

    companion object {
        var selectedRoomId: String? = null
        var selectedRoomName: String? = null
    }


    override fun onResume() {
        super.onResume()
        if(context is ConfigurationActivity) {
            (context as ConfigurationActivity).showDoneButton(true)
            (context as ConfigurationActivity).showAddButton(true)
        }else if(context is DashboardActivity){
            (context as DashboardActivity).showAddButton(false)
        }

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        arguments?.let {
            roomInfo= arguments?.getParcelable("Room-Info")
            screenType = arguments?.getString("Screen-Type")!!
            isRoomIdUpdated = arguments?.getBoolean("isRoomIdUpdated")!!
        }
        _binding = FragmentApplianceBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val textToSet = "You can add maximum ${AppConstants.THRESHOLD_APPLIANCES} Appliances."
        _binding!!.textviewFirst.text = textToSet


        if(screenType == ConfigurationActivity::class.java.simpleName){

            val navController = findNavController(requireActivity(), R.id.nav_host_fragment_content_configuration)
            navController.currentDestination?.label = resources.getString(R.string.title_configuration)

            var appBarConfiguration = AppBarConfiguration(navController.graph)
            ( requireActivity() as ConfigurationActivity).setupActionBarWithNavController(navController, appBarConfiguration)

            // showing header for configuration mode
            _binding!!.tvHeading.visibility = View.VISIBLE

            _binding!!.clRoom.setBackgroundColor(resources.getColor(R.color.purple_200,null))
        }

        _binding!!.rvAppliance.layoutManager= GridLayoutManager(view.context, 3)
         applianceAdapter = ApplianceAdapter(requireContext(),appliancesList, isRoomIdUpdated, screenType)
        applianceAdapter!!.setListener(this)
        _binding!!.rvAppliance.adapter = applianceAdapter

        fetchRoomApplianceData(requireContext())
    }

    fun fetchRoomApplianceData(context: Context) {
        lifecycleScope.launch {
            roomInfo?.let {
                _binding!!.tvRoomName.text = it.roomName
                selectedRoomId = getRoomId(it)
                selectedRoomName = it.roomName

                val applianceDao = AppDatabase.getDatabase(context).applianceDao()
                val listAppliances =  applianceDao.getApplianceByRoomId(roomId = selectedRoomId!!)
                println(":::::: ${it.roomName}  Appliance's List: ${listAppliances.size}")
                if(listAppliances.size>0){
                    println(":::::: Room ID (in Appliance) : ${listAppliances.get(0).roomId}  ")
                }

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
        if(ServerHandler.webSocket!=null)
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