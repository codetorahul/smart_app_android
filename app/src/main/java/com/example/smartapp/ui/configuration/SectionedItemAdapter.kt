package com.example.smartapp.ui.configuration

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartapp.R
import com.example.smartapp.data.tables.Appliances


class SectionedItemAdapter(private val itemList: List<Appliances>) :
    RecyclerView.Adapter<SectionedItemAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sectioned_item, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val currentItem = itemList[position]
        val textToSet =currentItem.applianceName +" - "+currentItem.applianceId
        holder.tvApplianceName.text = textToSet
    }

    override fun getItemCount(): Int = itemList.size

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvApplianceName: TextView = itemView.findViewById(R.id.tvApplianceName)

    }
}