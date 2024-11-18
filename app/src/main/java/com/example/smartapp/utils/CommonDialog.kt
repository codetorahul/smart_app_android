package com.example.smartapp.utils

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TextView
import com.example.smartapp.R
import com.example.smartapp.data.preference.PreferenceManager
import com.example.smartapp.listener.ItemAddListener
import com.example.smartapp.listener.OptionSelectListener
import com.example.smartapp.ui.login.LoginActivity
import com.example.smartapp.utils.AppConstants.OPTION_DELETE
import com.example.smartapp.utils.AppConstants.OPTION_RENAME

fun showEditTextDialog(context: Context, positiveButton: String = "ADD", title: String,
                       nameToUpdate: String = "", typeOfDialog : String, taskToPerformOnCancel: (()-> Unit)? =  null,   taskToPerform: (name : String) -> Unit) {
    val inflater = LayoutInflater.from(context)
    val dialogView = inflater.inflate(R.layout.dialog_add, null)
    val editText = dialogView.findViewById<EditText>(R.id.etName)


    if(typeOfDialog == AppConstants.ADD_ROOM || typeOfDialog == AppConstants.ADD_APPLIANCES) {
        val editTextHint =
            if (typeOfDialog == AppConstants.ADD_ROOM) "Enter Room Name" else "Enter Appliance Name"
        // Find the EditText in the dialog layout
        editText.setHint(editTextHint)
    }else if(typeOfDialog == AppConstants.WIFI_PASSWORD){
        editText.setHint("Enter Wifi Password")
    }

    if(nameToUpdate.isNotEmpty()){
        editText.setText(nameToUpdate)
    }

    // Build the AlertDialog
    val dialogBuilder = AlertDialog.Builder(context)
    dialogBuilder.setTitle(title)
        .setView(dialogView)
        .setPositiveButton(positiveButton) { _, _ ->
            taskToPerform(editText.text.toString().trim())
            
        }
        .setNegativeButton("CANCEL"){_,_->

            taskToPerformOnCancel?.let {
                it()
            }
        }

    // Show the dialog
    dialogBuilder.create().show()
}

fun showConfirmationDialog(context: Context,  message: String,  stepOnYesPress: () -> Unit) {
    val builder: AlertDialog.Builder = AlertDialog.Builder(context)
    builder.setTitle("Confirmation")
    builder.setMessage(message)

    // Set up the "Yes" button
    builder.setPositiveButton("Yes", DialogInterface.OnClickListener { _, _ ->
        stepOnYesPress()
    })

    // Set up the "No" button
    builder.setNegativeButton("No", DialogInterface.OnClickListener { dialog, _ ->
        // Handle the "No" action
        dialog.dismiss() // Close the dialog
    })

    // Show the dialog
    val dialog: AlertDialog = builder.create()
    dialog.show()
}

fun showOptionDialog(context: Context, name: String, listener: OptionSelectListener, item: Any) {
    val inflater = LayoutInflater.from(context)
    val dialogView = inflater.inflate(R.layout.dialog_options, null)

    // Build the AlertDialog
    val dialogBuilder = AlertDialog.Builder(context)
    dialogBuilder.setTitle(name)
        .setView(dialogView)

    // Find the EditText in the dialog layout
    val tvDelete = dialogView.findViewById<TextView>(R.id.tvDelete)
    val tvRename = dialogView.findViewById<TextView>(R.id.tvRename)

    // Show the dialog
    val alertDialog =  dialogBuilder.create()
    alertDialog.show()
    tvDelete.setOnClickListener{
        listener.onOptionSelect(OPTION_DELETE, item)
        alertDialog.dismiss()
    }
    tvRename.setOnClickListener{
        listener.onOptionSelect(OPTION_RENAME, item)
        alertDialog.dismiss()
    }



}