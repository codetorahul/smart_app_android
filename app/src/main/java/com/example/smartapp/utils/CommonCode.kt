package com.example.smartapp.utils

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData


fun showToast(context: Context, message: String){
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

private val _globalLiveData = MutableLiveData<String>()
val globalLiveData: LiveData<String> get() = _globalLiveData

fun updateData(newData: String) {
    _globalLiveData.value = newData
}

// Method to update LiveData from background thread
fun updateDataInBackground(newData: String) {
    _globalLiveData.postValue(newData)
}