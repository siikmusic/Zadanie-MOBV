package com.example.zadaniemobv.api

import android.content.Context
import android.util.Log
import com.example.zadaniemobv.db.AppRoomDatabase
import com.example.zadaniemobv.entities.GeofenceEntity
import com.example.zadaniemobv.entities.UserEntity
import com.example.zadaniemobv.model.User
import okhttp3.MultipartBody
import retrofit2.Response
import java.io.IOException

class DataRepository private constructor(
    private val service: ApiService,
    private val uploadService: UploadApiService,
    private val cache: LocalCache

) {

    companion object {
        const val TAG = "DataRepository"

        @Volatile
        private var INSTANCE: DataRepository? = null
        private val lock = Any()

        fun getInstance(context: Context): DataRepository =
            INSTANCE ?: synchronized(lock) {
                INSTANCE
                    ?: DataRepository(
                        ApiService.create(context),
                        UploadApiService.create(context),
                        LocalCache(AppRoomDatabase.getInstance(context).appDao())
                    ).also { INSTANCE = it }
            }
    }

    suspend fun apiRegisterUser(
        username: String,
        email: String,
        password: String
    ): Pair<String, User?> {
        if (username.isEmpty()) {
            return Pair("Username can not be empty", null)
        }
        if (email.isEmpty()) {
            return Pair("Email can not be empty", null)
        }
        if (password.isEmpty()) {
            return Pair("Password can not be empty", null)
        }
        try {
            val response = service.registerUser(UserRegistration(username, email, password))
            if (response.isSuccessful) {
                response.body()?.let { json_response ->
                    if(json_response.uid == "-2"){
                        return Pair("Failed to create user", null)
                    }
                    return Pair(
                        "",
                        User(
                            username,
                            email,
                            json_response.uid,
                            json_response.access,
                            json_response.refresh
                        )
                    )
                }
            }
            return Pair("Failed to create user", null)
        } catch (ex: IOException) {
            ex.printStackTrace()
            return Pair("Check internet connection. Failed to create user.", null)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return Pair("Fatal error. Failed to create user.", null)
    }

    suspend fun apiLoginUser(username: String, password: String): Pair<String, User?> {

        if (username.isEmpty()) {
            return Pair("Email can not be empty", null)
        }
        if (password.isEmpty()) {
            return Pair("Password can not be empty", null)
        }
        try {
            val response = service.loginUser(UserLogin(username, password))
            if (response.isSuccessful) {
                response.body()?.let { json_response ->
                    if (json_response.uid == "-1") {
                        return Pair("Wrong password or username.", null)
                    }
                    return Pair(
                        "",
                        User(
                            username,
                            username,
                            json_response.uid,
                            json_response.access,
                            json_response.refresh
                        )
                    )
                }
            }
            return Pair("Failed to login user", null)
        } catch (ex: IOException) {
            ex.printStackTrace()
            return Pair("Check internet connection. Failed to login user.", null)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return Pair("Fatal error. Failed to create user.", null)
    }

    suspend fun apiGetUser(
        uid: String
    ): Pair<String, User?> {
        try {
            val response = service.getUser(uid)

            if (response.isSuccessful) {
                response.body()?.let {
                    Log.d("responsetest", response.body().toString());
                    return Pair("", User(it.name, "", it.id, "", "", it.photo))
                }
            }

            return Pair("Failed to load user", null)
        } catch (ex: IOException) {
            ex.printStackTrace()
            return Pair("Check internet connection. Failed to load user.", null)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return Pair("Fatal error. Failed to load user.", null)
    }
    suspend fun apiResetPassword(
        email: String
    ): Pair<String, String?>{
        if(email == "") {
            return Pair("Error sending the email","")
        }
        val response = service.resetPassword(ResetPasswordRequest(email))
        if(response.isSuccessful) {
            response.body()?.let {
                return Pair("Reset e-mail sent to your email address",it.status)
            }
        }
        else {
            response.body()?.let {
                return Pair(it.message,it.status)
            }
        }
        return Pair("Error sending the email","")
    }
    suspend fun apiChangePassword(
        currentPassword: String,
        newPassword:String,
        repeatPassword:String
    ): Pair<String, String?> {
        if(currentPassword == "") {
            return Pair("Password is empty","")
        }
        if(newPassword == "") {
            return Pair("Password is empty","")
        }
        if(repeatPassword == "") {
            return Pair("Password is empty","")
        }
        if(repeatPassword != newPassword){
            return Pair("Passwords don't match","")
        }
        val response = service.resetKnownPassword(ResetKnownPasswordRequest(currentPassword,newPassword))
        if(response.isSuccessful) {
            response.body()?.let {
                return Pair("Password changed successfully",it.status)
            }
        }
        return Pair("There was an error changing the password","")
    }
    suspend fun insertGeofence(item: GeofenceEntity) {
        cache.insertGeofence(item)
        try {
            val response =
                service.updateGeofence(GeofenceUpdateRequest(item.lat, item.lon, item.radius))

            if (response.isSuccessful) {
                response.body()?.let {

                    item.uploaded = true
                    cache.insertGeofence(item)
                    return
                }
            }

            return
        } catch (ex: IOException) {
            ex.printStackTrace()
            return
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    suspend fun removeGeofence() {
        try {
            val response = service.deleteGeofence()

            if (response.isSuccessful) {
                response.body()?.let {
                    return
                }
            }

            return
        } catch (ex: IOException) {
            ex.printStackTrace()
            return
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    suspend fun apiListGeofence(): String {
        try {
            val response = service.listGeofence()
            //Log.d("API LIST GEO",re)
            if (response.isSuccessful) {
                response.body()?.list?.let {
                    val users = it.map {
                        UserEntity(
                            it.uid, it.name, it.updated,
                            0.0, 0.0, it.radius, it.photo
                        )
                    }
                    cache.insertUserItems(users)
                    return ""
                }
            }

            return "Failed to load users"
        } catch (ex: IOException) {
            ex.printStackTrace()
            return "Check internet connection. Failed to load users."
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return "Fatal error. Failed to load users."
    }

    fun getUsers() = cache.getUsers()
    suspend fun logout() = cache.logoutUser()
    suspend fun getUsersList() = cache.getUsersList()
    suspend fun uploadProfileImage(image: MultipartBody.Part): Response<UploadApiService.UploadImageResponse>? {
        try {
            val response =
                uploadService.uploadProfileImage(image)
            return response;


        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
    }
    suspend fun deleteProfileImage(): Response<UploadApiService.UploadImageResponse>? {
        return try {
            val response =
                uploadService.deleteProfileImage()
            response
        } catch (ex: IOException) {
            ex.printStackTrace()
            null
        }
    }
}