package com.example.smartapp.ui.appliances

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.smartapp.R
import com.example.smartapp.data.tables.Appliances
import com.example.smartapp.listener.ItemListener
import com.example.smartapp.ui.configuration.ConfigurationActivity


class ApplianceAdapter(private val context: Context,private val itemList: List<Appliances>, private val isRoomIdUpdated: Boolean,private val screenType: String) :
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


        holder.switchStatus.visibility =ViewGroup.VISIBLE

        if(screenType == ConfigurationActivity.TAG) {

            // In Configuration mode, no need to show toggle button
            holder.switchStatus.visibility =ViewGroup.GONE

            // Insertion or Rename can only work in Configuration mode
            holder.itemView.setOnLongClickListener{
                listener?.onItemLongClick(currentItem)
                true
            }
        }

            if(!isRoomIdUpdated) {
                // Room is not configured , hence appliance can't be accessed.
                holder.switchStatus.visibility =ViewGroup.GONE
                holder.clRoom.background = ContextCompat.getDrawable(context, R.drawable.background_disable)
            }
            else{
                holder.clRoom.background = ContextCompat.getDrawable(context, R.drawable.background)
            }

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


        // User can only change status once room configured
        holder.switchStatus.setOnCheckedChangeListener{ _: CompoundButton, b: Boolean ->
            if(screenType != ConfigurationActivity.TAG && isRoomIdUpdated) {
                    listener?.onStatusChange(b, currentItem)
            }
        }
    }

    override fun getItemCount(): Int = itemList.size

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivImage: ImageView = itemView.findViewById(R.id.ivRoom)
        val tvName: TextView = itemView.findViewById(R.id.tvRoom)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val switchStatus: SwitchCompat = itemView.findViewById(R.id.SwitchStatus)
        val clRoom: ConstraintLayout = itemView.findViewById(R.id.clRoot)


    }
}