package com.example.smartapp.listener

interface ItemListener {
    fun onItemClick(item: Any)
    fun onStatusChange(status: Boolean, item: Any)
    fun onItemLongClick(item: Any)
}