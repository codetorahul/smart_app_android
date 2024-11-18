package com.example.smartapp.firebase

import com.google.firebase.Firebase
import com.google.firebase.database.FirebaseDatabase

class MyFirebase {

    companion object {
        var firebaseDatabase : FirebaseDatabase? = null
    }

    fun getInstance() : FirebaseDatabase{
        if(firebaseDatabase != null) {
            firebaseDatabase = FirebaseDatabase.getInstance();
        }
        return firebaseDatabase!!
    }
}