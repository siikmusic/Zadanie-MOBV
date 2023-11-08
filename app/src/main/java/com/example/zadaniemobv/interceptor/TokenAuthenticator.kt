package com.example.zadaniemobv.interceptor


import android.content.Context
import com.example.zadaniemobv.PreferenceData
import com.example.zadaniemobv.api.ApiService
import com.example.zadaniemobv.api.RefreshTokenRequest
import com.example.zadaniemobv.model.DataUser
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Route

class TokenAuthenticator(val context: Context) : Authenticator {
    override fun authenticate(route: Route?, response: okhttp3.Response): Request? {


        if (response.code == 401) {
            val userItem = PreferenceData.getInstance().getUser(context)
            userItem?.let { user ->
                val tokenResponse = ApiService.create(context).refreshTokenBlocking(
                    RefreshTokenRequest(user.refresh)
                ).execute()

                if (tokenResponse.isSuccessful) {
                    tokenResponse.body()?.let {
                        val new_user = DataUser(
                            user.username,
                            user.email,
                            user.id,
                            it.access,
                            it.refresh,
                            user.photo
                        )
                        PreferenceData.getInstance().putUser(context, new_user)
                        return response.request.newBuilder()
                            .header("Authorization", "Bearer ${new_user.access}")
                            .build()
                    }
                }
            }
            //if there was no success of refresh token we logout user and clean any data
            PreferenceData.getInstance().clearData(context)
            return null
        }
        return null

    }
}
