package com.example.zadaniemobv

import android.content.Context
import android.content.SharedPreferences
import android.os.Debug
import android.util.Log
import com.example.zadaniemobv.model.DataUser
import com.example.zadaniemobv.model.User

class PreferenceData private constructor() {

    private fun getSharedPreferences(context: Context?): SharedPreferences? {
        return context?.getSharedPreferences(
            shpKey, Context.MODE_PRIVATE
        )
    }

    companion object {
        @Volatile
        private var INSTANCE: PreferenceData? = null

        private val lock = Any()

        fun getInstance(): PreferenceData =
            INSTANCE ?: synchronized(lock) {
                INSTANCE
                    ?: PreferenceData().also { INSTANCE = it }
            }

        private const val shpKey = "com.example.zadaniemobv"
        private const val userKey = "userKey"
        private var sharingLocation = false;
        private const val sharingKey = "sharingKey"


    }
    fun clearData(context: Context?) {
        val sharedPref = getSharedPreferences(context) ?: return
        val editor = sharedPref.edit()
        editor.clear()
        editor.apply()
    }

    fun putUser(context: Context?, user: DataUser?) {
        val sharedPref = getSharedPreferences(context) ?: return
        val editor = sharedPref.edit()
        user?.toJson()?.let {
            editor.putString(userKey, it)
        } ?: editor.remove(userKey)

        editor.apply()
    }

    fun getUser(context: Context?): DataUser? {
        val sharedPref = getSharedPreferences(context) ?: return null
        val json = sharedPref.getString(userKey, null) ?: return null

        return DataUser.fromJson(json)
    }

    fun putSharing(context: Context?, sharing: Boolean) {
        val sharedPref = getSharedPreferences(context) ?: return
        val editor = sharedPref.edit()
        editor.putBoolean(sharingKey, sharing)
        Log.d("Debug","" + sharing)

        editor.apply()
    }

    fun getSharing(context: Context?): Boolean {
        val sharedPref = getSharedPreferences(context) ?: return false
        val sharing = sharedPref.getBoolean(sharingKey, false)
        Log.d("Debug","" + sharing)

        return sharing
    }

}