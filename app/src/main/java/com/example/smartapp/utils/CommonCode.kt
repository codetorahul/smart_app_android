package com.example.smartapp.utils

import android.R
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.smartapp.data.tables.AppInfo
import com.example.smartapp.data.tables.Rooms
import com.example.smartapp.listener.DialogListener
import com.example.smartapp.model.ConnectionModel
import com.example.smartapp.utils.AppConstants.DIALOG_LOCATION_ENABLE
import com.example.smartapp.utils.AppConstants.OPTION_CANCEL
import com.google.android.material.snackbar.Snackbar
import java.net.URI


fun showToast(context: Context, message: String){
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

fun showSnackBar(context: Context, message: String){
    val view: View = (context as Activity).findViewById(R.id.content) // Get root view
    Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
}
private val _globalLiveData = MutableLiveData<ConnectionModel>()
val globalLiveData: LiveData<ConnectionModel> get() = _globalLiveData

fun updateData(newData: ConnectionModel) {
    _globalLiveData.value = newData
}

// Method to update LiveData from background thread
fun updateDataInBackground(newData: ConnectionModel) {
    _globalLiveData.postValue(newData)
}


var dialog : AlertDialog?=null;

 fun showProgressDialog(context: Context) {
    // Create a ProgressBar programmatically
    val progressBar = ProgressBar(context).apply {
        layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    // Create an AlertDialog and set the ProgressBar in it
     dialog = AlertDialog.Builder(context)
        .setView(progressBar) // Add the ProgressBar as the content of the dialog
        .setCancelable(false) // Disable outside clicks to dismiss the dialog
        .create()

    // Show the dialog
    dialog!!.show()


}


fun hideProgressBar(){
    dialog?.dismiss()
}

fun getSchemeFromUrl(urlString: String): String {
    // Parse the URL using the URI class
    val uri = URI(urlString)

    // Return the scheme (e.g., "https")
    return uri.scheme ?: ""
}

fun getPortFromUrl(urlString: String): String {
    // Parse the URL using the URI class
    val uri = URI(urlString)

    return uri.port.toString()
}


fun checkLocation(context: Activity, listener: DialogListener): Boolean{
    val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    var gps_enabled = false
    var network_enabled = false

    try {
        gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
    } catch (ex: Exception) {
    }

    try {
        network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    } catch (ex: Exception) {
    }


    if (!gps_enabled && !network_enabled) {
        createLocationServiceError(context, listener)
        return false
    }else{
        return true
    }
}

fun createLocationServiceError(activityObj: Activity, listener: DialogListener) {
    // show alert dialog if Internet is not connected

    val builder = android.app.AlertDialog.Builder(activityObj)

    builder.setMessage(
        "You need to activate location service to use this feature. Please turn on network or GPS mode in location settings and then RETRY."
    )
        .setCancelable(false)
        .setPositiveButton(
            "Settings"
        ) { dialog, id ->
            val intent: Intent = Intent(
                Settings.ACTION_LOCATION_SOURCE_SETTINGS
            )
            activityObj.startActivity(intent)
            dialog.dismiss()
        }
        .setNegativeButton(
            "Cancel"
        ) { dialog, id ->
            run {
                listener.onOptionClick(OPTION_CANCEL, "", DIALOG_LOCATION_ENABLE)
                dialog.dismiss()
            }
        }
    val alert = builder.create()
    alert.show()
}

fun showCustomDialog(activityObj: Activity, message: String,
                     showPositiveButton : Boolean, showNegativeButton : Boolean,
                     positiveText: String="Settings", negativeText: String="Cancel") {
    // show alert dialog if Internet is not connected

    val builder = android.app.AlertDialog.Builder(activityObj)

    builder.setMessage(message)
        .setCancelable(false)


    if(showPositiveButton){
        builder.setPositiveButton(
            positiveText
        ) { dialog, id ->
            // write your action to perform
            dialog.dismiss()
        }
    }

    if(showNegativeButton){
        builder.setNegativeButton(
            negativeText
        ) { dialog, id ->
            run {
                dialog.dismiss()
            }
        }
    }
    val alert = builder.create()
    alert.show()
}


 fun isDevicesConfigured(appInfo: AppInfo?): Boolean {
    return appInfo != null && appInfo.isDevicesAddedInConfigMode && appInfo.isMacAddressUpdated
}
 fun getRoomId(it: Rooms): String {
    return if(it.isRoomIdUpdated){
        println(">>>>>> ROOM ID UPDATED..SO RoomId:  ${it.roomId}")
        it.roomId
    }else {
        println(">>>>>> Normal Combination Id:  ${it.roomId + it._id}")
        it.roomId + it._id

    }
}