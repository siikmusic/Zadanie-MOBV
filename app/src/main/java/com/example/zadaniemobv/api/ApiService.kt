package com.example.zadaniemobv.api

import android.content.Context
import com.example.zadaniemobv.interceptor.AuthInterceptor
import com.example.zadaniemobv.interceptor.TokenAuthenticator
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST("user/create.php")
    suspend fun registerUser(@Body userInfo: UserRegistration): Response<RegistrationResponse>

    @POST("user/login.php")
    suspend fun loginUser(@Body userInfo: UserLogin): Response<RegistrationResponse>
    @GET("user/get.php")
    suspend fun getUser(
        @Query("id") id: String
    ): Response<UserResponse>
    @POST("user/reset.php")
    suspend fun resetPassword(@Body resetInfo: ResetPasswordRequest): Response<ResetPasswordResponse>
    @POST("user/refresh.php")
    fun refreshTokenBlocking(
        @Body refreshInfo: RefreshTokenRequest
    ): Call<RefreshTokenResponse>

    @GET("geofence/list.php")
    suspend fun listGeofence(): Response<GeofenceListResponse>

    @POST("geofence/update.php")
    suspend fun updateGeofence(@Body body: GeofenceUpdateRequest): Response<GeofenceUpdateResponse>

    @DELETE("geofence/update.php")
    suspend fun deleteGeofence(): Response<GeofenceUpdateResponse>
    companion object{
        fun create(context: Context): ApiService {

            val client = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(context))
                .authenticator(TokenAuthenticator(context))
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl("https://zadanie.mpage.sk/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(ApiService::class.java)
        }
    }
}
data class UserRegistration(val name: String, val email: String, val password: String)
data class RegistrationResponse(val uid: String, val access: String, val refresh: String)

data class UserLogin(val name: String, val password: String)
data class RefreshTokenRequest(val refresh: String)
data class RefreshTokenResponse(val uid: String, val access: String, val refresh: String)

data class LoginResponse(val uid: String, val access: String, val refresh: String)

data class ResetPasswordRequest(val email: String)

data class ResetPasswordResponse(val status:String, val message:String)
data class UserResponse(val id: String, val name: String, val photo: String)
data class GeofenceUpdateRequest(val lat: Double, val lon: Double, val radius: Double)
data class GeofenceListResponse(
    val me: GeofenceListMeResponse,
    val list: List<GeofenceListAllResponse>
)

data class GeofenceListMeResponse(
    val uid: String,
    val lat: Double,
    val lon: Double,
    val radius: Double,
)

data class GeofenceListAllResponse(
    val uid: String,
    val name: String,
    val updated: String,
    val radius: Double,
    val photo: String
)

data class GeofenceUpdateResponse(val success: String)