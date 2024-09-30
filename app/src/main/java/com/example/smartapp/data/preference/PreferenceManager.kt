package com.example.smartapp.data.preference

import android.content.Context
import android.content.SharedPreferences


class PreferenceManager private constructor(context: Context) {

    private val sharedPref: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    fun getInt(key: String?): Int {
        return sharedPref.getInt(key, 0)
    }

    fun putInt(key: String?, value: Int) {
        val editor = sharedPref.edit()
        editor.putLong(key, value.toLong())
        editor.apply()
    }

    fun putString(key: String?, value: String?) {
        val editor = sharedPref.edit()
        editor.putString(key, value) // Commit the edits!
        editor.apply()
    }

    fun getString(key: String?): String? {
        return sharedPref.getString(key, null)
    }

    fun putBoolean(key: String?, value: Boolean) {
        val editor = sharedPref.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }



    fun getBoolean(key: String?): Boolean {
        return sharedPref.getBoolean(key, false)
    }

    fun clearAllPrefs() {
        val editor = sharedPref.edit()
        editor.clear()
        editor.apply()
    }

    companion object {

        private const val PREFS_NAME = "my_prefs"
        private const val KEY_EXAMPLE = "example_key"
        private var mAppSharedPreferenceInstance: PreferenceManager? = null
        fun getInstance(context: Context): PreferenceManager? {
            if (mAppSharedPreferenceInstance == null) {
                synchronized(PreferenceManager::class.java) {
                    if (mAppSharedPreferenceInstance == null) mAppSharedPreferenceInstance =
                        PreferenceManager(context)
                }
            }
            return mAppSharedPreferenceInstance
        }
    }

}