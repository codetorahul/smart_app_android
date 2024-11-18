package com.example.smartapp.data.tables

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.RequiresApi
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "appInfo")
data class AppInfo(
    @PrimaryKey(autoGenerate = true) val _id: Int = 0,
    var isDevicesAddedInConfigMode: Boolean = false,
    val isMacAddressUpdated: Boolean = false,
) : Parcelable {

    constructor(isDevicesAddedInConfigMode: Boolean, isMacAddressUpdated: Boolean) : this(0, isDevicesAddedInConfigMode, isMacAddressUpdated)


    @RequiresApi(Build.VERSION_CODES.Q)
    constructor(parcel: Parcel) : this(
        _id=  parcel.readInt(),
        isDevicesAddedInConfigMode =parcel.readBoolean(),
        isMacAddressUpdated =   parcel.readBoolean(),
    )

    override fun describeContents(): Int {
        return  0
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(_id)
        parcel.writeBoolean(isDevicesAddedInConfigMode)
        parcel.writeBoolean(isMacAddressUpdated)
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
