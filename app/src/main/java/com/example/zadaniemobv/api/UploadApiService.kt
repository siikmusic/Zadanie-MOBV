package com.example.zadaniemobv.api

import android.content.Context
import com.example.zadaniemobv.interceptor.AuthInterceptor
import com.example.zadaniemobv.interceptor.TokenAuthenticator
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.DELETE
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface UploadApiService {
    @Multipart
    @POST("user/photo.php")
    suspend fun uploadProfileImage(
        @Part image: MultipartBody.Part
    ): Response<UploadImageResponse>

    @DELETE("user/photo.php")
    suspend fun deleteProfileImage(
    ): Response<UploadImageResponse>
    companion object{
        fun create(context: Context): UploadApiService {

            val client = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(context))
                .authenticator(TokenAuthenticator(context))
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl("https://upload.mcomputing.eu/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(UploadApiService::class.java)
        }
    }
    data class UploadImageResponse(
        val id: Int,
        val name: String,
        val photo: String
    )
}