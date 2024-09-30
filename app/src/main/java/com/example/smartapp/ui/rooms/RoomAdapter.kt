package com.example.smartapp.ui.rooms

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartapp.R
import com.example.smartapp.listener.ItemListener

class RoomAdapter(private val itemList: List<Rooms>) :
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
        holder.tvRoom.text = currentItem.roomName

        holder.itemView.setOnClickListener {
            listener?.onItemClick(currentItem)
        }

        holder.itemView.setOnLongClickListener{
            listener?.onItemLongClick(currentItem)
            true
        }

    }

    override fun getItemCount(): Int = itemList.size

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvRoom: TextView = itemView.findViewById(R.id.tvRoom)
    }
}