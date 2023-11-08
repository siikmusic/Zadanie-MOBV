package com.example.zadaniemobv.interceptor

import android.content.Context
import com.example.zadaniemobv.PreferenceData
import com.example.zadaniemobv.api.AppConfig
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
            .newBuilder()
            .addHeader("Accept", "application/json")
            .addHeader("Content-Type", "application/json")


        val token = PreferenceData.getInstance().getUser(context)?.access
        request.header("Authorization","Bearer $token")

        // add api key to each request
        request.addHeader("x-apikey", AppConfig.API_KEY)

        return chain.proceed(request.build())
    }
}