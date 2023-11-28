package com.example.zadaniemobv.model

import com.google.gson.Gson
import java.io.IOException
import java.io.Serializable

data class DataUser(
    val username: String,
    val photo: String,
    val lat: Double,
    val lon: Double,
    val radius: Double

): Serializable
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