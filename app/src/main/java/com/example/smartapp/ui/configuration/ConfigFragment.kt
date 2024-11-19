package com.example.smartapp.ui.configuration

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartapp.R
import com.example.smartapp.data.AppDatabase
import com.example.smartapp.databinding.FragmentConfigBinding
import com.example.smartapp.data.tables.Rooms
import com.example.smartapp.ui.dashboard.DashboardActivity
import com.example.smartapp.utils.getRoomId
import kotlinx.coroutines.launch


class ConfigFragment : Fragment() {

    private var _binding: FragmentConfigBinding? = null
    private val roomWithAppliancesList = ArrayList<RoomsWithAppliances>()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var roomInfo: Rooms? =null
    private var sectionedAdapter : SectionedHeaderAdapter? =null
    private lateinit var screenType: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        arguments?.let {
            roomInfo= arguments?.getParcelable("Room-Info")
            screenType = arguments?.getString("Screen-Type")!!
        }
        _binding = FragmentConfigBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        requireActivity().addMenuProvider(object : MenuProvider{
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {

            }

            override fun onPrepareMenu(menu: Menu) {
                // Hide specific activity menu items
                if(context is DashboardActivity) {
                    menu.findItem(R.id.action_config_mode).isVisible = false
                    menu.findItem(R.id.action_logout).isVisible = false
                    menu.findItem(R.id.action_config).isVisible = false
                    menu.findItem(R.id.action_reconnect_server).isVisible = false
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return false
            }
        }, viewLifecycleOwner)

        _binding!!.rvConfig.layoutManager= LinearLayoutManager(requireContext())

        sectionedAdapter = SectionedHeaderAdapter(requireContext(),roomWithAppliancesList)
        _binding!!.rvConfig.adapter = sectionedAdapter

        fetchRoomWithTheirApplianceData(requireContext())
    }

    private fun fetchRoomWithTheirApplianceData(context: Context) {
        lifecycleScope.launch {
            val appDatabase = AppDatabase.getDatabase(context)
            val roomDao = appDatabase.roomDao()
            val listRooms =  roomDao.getAllRooms()

            roomWithAppliancesList.clear()
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
                val listAppliances =  applianceDao.getApplianceByRoomId(roomId = roomId)

                println(":::::: ${it.roomName} Appliance's List: ${listAppliances.size}")

                roomWithAppliancesList.add(RoomsWithAppliances(_roomId,roomId, roomName, roomColor , listAppliances, isRoomIdUpdated))
                sectionedAdapter?.notifyDataSetChanged()

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}