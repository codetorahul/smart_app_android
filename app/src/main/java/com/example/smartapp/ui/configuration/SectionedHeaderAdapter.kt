package com.example.smartapp.ui.configuration

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartapp.R
import com.example.smartapp.data.tables.Appliances

class SectionedHeaderAdapter(private val context: Context, private val items: List<RoomsWithAppliances>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_HEADER = 0
    private val TYPE_ITEM = 1
    private var prevHeaderId = ""

    override fun getItemViewType(position: Int): Int {
        return if (items[position].roomId != prevHeaderId){
            prevHeaderId = items[position].roomId
            TYPE_HEADER
        } else {
            TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sectioned, parent, false)
           return SectionedViewHolder(context,view)

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        println(":::: ROOM ID: ${item.roomId} ROom NAme: ${item.roomName}, isAddressUpdate: ${item.isMacAddressAdded}")
        if (holder is SectionedViewHolder && item.isMacAddressAdded) {
            holder.bind(item.roomName+" -- "+item.roomId,item.appliances)
        }
    }

    override fun getItemCount(): Int = items.size

    // Define ViewHolder for Item
    class SectionedViewHolder(val context: Context, itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val headerTitle: TextView = itemView.findViewById(R.id.tvHeaderTitle)

        private val rvRoomAppliances: RecyclerView = itemView.findViewById(R.id.rvAppliance)
        fun bind(title: String,roomApplianceList: List<Appliances>) {

            headerTitle.text = title

            rvRoomAppliances.layoutManager= LinearLayoutManager(context)

            val sectionedItemAdapter = SectionedItemAdapter(roomApplianceList)
            rvRoomAppliances.adapter = sectionedItemAdapter
        }
    }
}
