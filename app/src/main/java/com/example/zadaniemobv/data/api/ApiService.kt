package com.example.zadaniemobv.data.api

import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {
    @Headers("x-apikey: ...")
    @POST("user/create.php")
    suspend fun registerUser(@Body userInfo: UserRegistration): Response<RegistrationResponse>

    @Headers("x-apikey: ...")
    @POST("user/create.php")
    suspend fun loginUser(@Body userInfo: UserLogin): Response<LoginResponse>

    @POST("users/logout")
    fun logout(): Call<Response<Void>>

    companion object{
        fun create(): ApiService {

            val retrofit = Retrofit.Builder()
                .baseUrl("https://zadanie.mpage.sk/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(ApiService::class.java)
        }
    }
}

data class UserRegistration(val name: String, val email: String, val password: String)
data class RegistrationResponse(val uid: String, val access: String, val refresh: String)
data class UserLogin(val name: String, val email: String, val password: String)

data class LoginResponse(val uid: String, val access: String, val refresh: String)

