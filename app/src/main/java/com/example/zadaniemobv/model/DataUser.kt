package com.example.zadaniemobv.model

import com.google.gson.Gson
import java.io.IOException

data class DataUser(
    val username: String,
    val email: String,
    val id: String,
    val access: String,
    val refresh: String,
    val photo: String

)
 {

    fun toJson(): String? {
        return try {
            Gson().toJson(this)
        } catch (ex: IOException) {
            ex.printStackTrace()
            null
        }
    }

    companion object {
        fun fromJson(string: String): DataUser? {
            return try {
                Gson().fromJson(string, DataUser::class.java)
            } catch (ex: IOException) {
                ex.printStackTrace()
                null
            }
        }
    }
}