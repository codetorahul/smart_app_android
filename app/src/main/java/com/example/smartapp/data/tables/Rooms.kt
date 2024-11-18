package com.example.smartapp.data.tables

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.RequiresApi
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rooms")
data class Rooms(
    @PrimaryKey(autoGenerate = true) val _id: Int = 0,
    var roomName: String,
    val roomColor: String,
    val roomId: String,
    val isRoomIdUpdated: Boolean =false
) : Parcelable {

    constructor(roomName: String?, roomColor: String, roomId: String, isRoomIdUpdated: Boolean) : this(0, roomName!!, roomColor, roomId,isRoomIdUpdated)


    @RequiresApi(Build.VERSION_CODES.Q)
    constructor(parcel: Parcel) : this(
        _id=  parcel.readInt(),
       roomId =parcel.readString()!!,
      roomName =   parcel.readString()!!,
      roomColor=   parcel.readString()!!,
      isRoomIdUpdated = parcel.readInt() !=0
    )

    override fun describeContents(): Int {
        return  0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(_id)
        parcel.writeString(roomId)
        parcel.writeString(roomName)
        parcel.writeString(roomColor)

        val intToSave = if(isRoomIdUpdated) 1 else 0
        parcel.writeInt(intToSave)


    }

    companion object CREATOR : Parcelable.Creator<Rooms> {
        override fun createFromParcel(parcel: Parcel): Rooms {
            return Rooms(parcel)
        }

        override fun newArray(size: Int): Array<Rooms?> {
            return arrayOfNulls(size)
        }
    }
}
