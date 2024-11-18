package com.example.smartapp.ui.rooms

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.smartapp.R
import com.example.smartapp.data.AppDatabase
import com.example.smartapp.data.tables.Rooms
import com.example.smartapp.databinding.FragmentRoomBinding
import com.example.smartapp.listener.ItemListener
import com.example.smartapp.listener.OptionSelectListener
import com.example.smartapp.socket.ServerHandler
import com.example.smartapp.socket.SocketMessageModel
import com.example.smartapp.ui.configuration.ConfigurationActivity
import com.example.smartapp.ui.dashboard.DashboardActivity
import com.example.smartapp.utils.AppConstants
import com.example.smartapp.utils.AppConstants.RENAME_ROOM
import com.example.smartapp.utils.getRoomId
import com.example.smartapp.utils.showConfirmationDialog
import com.example.smartapp.utils.showEditTextDialog
import com.example.smartapp.utils.showOptionDialog
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class RoomFragment : Fragment(), ItemListener, OptionSelectListener {

    private var _binding: FragmentRoomBinding? = null
    private val binding get() = _binding!!
    private val roomsList = ArrayList<Rooms>()
    private  var roomAdapter : RoomAdapter? = null
    private val args: RoomFragmentArgs by navArgs()
    private lateinit var screenType: String


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRoomBinding.inflate(inflater, container, false)
        return binding.root
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
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = RoomFragmentArgs.fromBundle(requireArguments())

        screenType = args.screenType

        val textToSet = "You can add maximum ${AppConstants.THRESHOLD_ROOMS} rooms."
        _binding!!.textviewFirst.text = textToSet

        if(screenType == ConfigurationActivity.TAG){

            val navController = findNavController(requireActivity(), R.id.nav_host_fragment_content_configuration)
            navController.currentDestination?.label = resources.getString(R.string.title_configuration)

            var appBarConfiguration = AppBarConfiguration(navController.graph)
            ( requireActivity() as ConfigurationActivity).setupActionBarWithNavController(navController, appBarConfiguration)

            // showing header for configuration mode
            _binding!!.tvHeading.visibility = View.VISIBLE

           _binding!!.clRoom.setBackgroundColor(resources.getColor(R.color.purple_200,null))
        }

        _binding!!.rvRooms.layoutManager= GridLayoutManager(view.context, 3)

        roomAdapter = RoomAdapter(requireContext(),roomsList)
        roomAdapter!!.setListener(this)
        _binding!!.rvRooms.adapter = roomAdapter

        fetchRoomData(requireContext())
    }

    fun fetchRoomData(context: Context) {
        lifecycleScope.launch {

            val appDatabase = AppDatabase.getDatabase(context)
            val roomDao = appDatabase.roomDao()
            val listRooms =  roomDao.getAllRooms()
            println(":::::: ROOMS LIST: ${listRooms.size}")
            roomsList.clear()
            roomsList.addAll(listRooms)
            roomAdapter!!.notifyDataSetChanged()

            _binding?.let {
                if (roomsList.size > 0) {
                    it.tvNoItemText.visibility = ViewGroup.GONE
                    it.rvRooms.visibility = ViewGroup.VISIBLE
                } else {
                    it.tvNoItemText.visibility = ViewGroup.VISIBLE
                    it.rvRooms.visibility = ViewGroup.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemClick(item: Any) {
        val selectedRoom =  item as Rooms
        val bundle = Bundle()
        bundle.putParcelable("Room-Info", selectedRoom)
        bundle.putString("Screen-Type", screenType)
        bundle.putBoolean("isRoomIdUpdated", selectedRoom.isRoomIdUpdated)
        findNavController().navigate(R.id.action_Room_to_Appliances, bundle)

    }

    override fun onStatusChange(status: Boolean, item: Any) {

    }

    override fun onItemLongClick(item: Any) {
        val selectedRoom = item as Rooms
        showOptionDialog(requireContext(), "Room Name -  "+selectedRoom.roomName, this, selectedRoom )
    }

    override fun onOptionSelect(optionType: String, item: Any) {
        val room = item as Rooms
        if(optionType == AppConstants.OPTION_DELETE){
            showConfirmationDialog(requireContext(), requireContext().getString(R.string.delete_text)){
                lifecycleScope.launch ( Dispatchers.IO) {
                    AppDatabase.getDatabase(requireContext()).roomDao().deleteRoom(room)
                    AppDatabase.getDatabase(requireContext()).applianceDao().deleteAppliancesByRoomId(roomId = getRoomId(room))
                    fetchRoomData(requireContext())

                    // Socket Event: Delete Appliance on Server
                    ServerHandler.webSocket?.let {
                        val data = SocketMessageModel(
                            type = AppConstants.TYPE_DELETE_ROOM,
                            roomId = room.roomId.toString(),
                        )

               /*         if (it.connected())
                            it.emit(AppConstants.SOCKET_EVENT, Gson().toJson(data))
                   */

                            it.send( Gson().toJson(data))


                    }
                }
            }
        }else{
            showEditTextDialog(
                requireContext(),
                positiveButton = "RENAME",
                title = "RENAME ROOM",
                nameToUpdate = room.roomName,
                typeOfDialog = RENAME_ROOM
            ){
                val renamedValue = it
                room.roomName = renamedValue
                lifecycleScope.launch(Dispatchers.IO) {
                    AppDatabase.getDatabase(requireContext()).roomDao().updateRoom(room)
                    fetchRoomData(requireContext())
                }
            }
        }
    }
}