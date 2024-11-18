package com.example.smartapp.ui.rooms

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.smartapp.R
import com.example.smartapp.data.tables.Rooms
import com.example.smartapp.listener.ItemListener
import com.example.smartapp.ui.configuration.ConfigurationActivity

class RoomAdapter(private val context: Context,private val itemList: List<Rooms>) :
    RecyclerView.Adapter<RoomAdapter.ItemViewHolder>() {

        private var listener : ItemListener? = null

        fun setListener(listener: ItemListener){
            this.listener = listener
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_room_layout, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val currentItem = itemList[position]

          if(!currentItem.isRoomIdUpdated) {
              holder.clRoom.background = ContextCompat.getDrawable(context, R.drawable.background_disable)
          }
          else{
              holder.clRoom.background = ContextCompat.getDrawable(context, R.drawable.background)
          }

        holder.tvRoom.text = currentItem.roomName

        holder.itemView.setOnClickListener {
            listener?.onItemClick(currentItem)
        }

        holder.itemView.setOnLongClickListener {
            listener?.onItemLongClick(currentItem)
            true
        }

    }

    override fun getItemCount(): Int = itemList.size

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvRoom: TextView = itemView.findViewById(R.id.tvRoom)
        val clRoom: ConstraintLayout = itemView.findViewById(R.id.clRoot)
    }
}