package com.example.smartapp.ui.rooms

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "rooms")
data class Rooms(
    @PrimaryKey(autoGenerate = true) val roomId: Int = 0,
    var roomName: String,
    val roomColor: String
) : Parcelable {

    constructor(roomName: String?, roomColor: String) : this(0, roomName!!, roomColor)


    constructor(parcel: Parcel) : this(
       roomId =  parcel.readInt(),
      roomName =   parcel.readString()!!,
      roomColor=   parcel.readString()!!
    )

    override fun describeContents(): Int {
        return  0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(roomId)
        parcel.writeString(roomName)
        parcel.writeString(roomColor)

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
