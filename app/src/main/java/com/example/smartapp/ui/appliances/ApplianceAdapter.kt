package com.example.smartapp.ui.appliances

import android.R.attr.bottom
import android.R.attr.left
import android.R.attr.right
import android.R.attr.top
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.smartapp.R
import com.example.smartapp.listener.ItemListener


class ApplianceAdapter(private val itemList: List<Appliances>) :
    RecyclerView.Adapter<ApplianceAdapter.ItemViewHolder>() {

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
        holder.tvName.text = currentItem.applianceName

        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        lp.setMargins(0, 0, 0, 0)

        holder.ivImage.setImageResource(R.drawable.appliance)
        holder.ivImage.layoutParams = lp
        val status = "Status: " + if(currentItem.applianceStatus) "ON" else "OFF"
        holder.tvStatus.text = status
        holder.tvStatus.visibility =ViewGroup.VISIBLE

        holder.switchStatus.setOnCheckedChangeListener(null)
        holder.switchStatus.isChecked = currentItem.applianceStatus
        holder.switchStatus.visibility =ViewGroup.VISIBLE

        holder.switchStatus.setOnCheckedChangeListener{ _: CompoundButton, b: Boolean ->
            listener?.onStatusChange(b, currentItem)
        }

        holder.itemView.setOnClickListener {
            listener?.onItemClick(it)
        }
        holder.itemView.setOnLongClickListener{
            listener?.onItemLongClick(currentItem)
            true
        }


    }

    override fun getItemCount(): Int = itemList.size

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivImage: ImageView = itemView.findViewById(R.id.ivRoom)
        val tvName: TextView = itemView.findViewById(R.id.tvRoom)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val switchStatus: SwitchCompat = itemView.findViewById(R.id.SwitchStatus)



    }
}