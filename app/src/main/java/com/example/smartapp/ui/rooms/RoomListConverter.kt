package com.example.smartapp.ui.rooms

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class RoomListConverter {
    @TypeConverter
    fun fromString(value: String): List<Rooms> {
        val listType = object : TypeToken<List<Rooms>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromList(list: List<Rooms>): String {
        return Gson().toJson(list)
    }
}