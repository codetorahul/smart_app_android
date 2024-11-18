package com.example.smartapp.listener

interface DialogListener {
    fun onOptionClick(optionType: String, others: Any, dialogType: String)
}