package com.example.zadaniemobv.interceptor


import android.content.Context
import com.example.zadaniemobv.PreferenceData
import com.example.zadaniemobv.api.ApiService
import com.example.zadaniemobv.api.RefreshTokenRequest
import com.example.zadaniemobv.model.DataUser
import com.example.zadaniemobv.model.User
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Route

class TokenAuthenticator(val context: Context) : Authenticator {
    override fun authenticate(route: Route?, response: okhttp3.Response): Request? {

        if (response.request.url.toUrl().path.contains("/user/create.php", true)
            || response.request.url.toUrl().path.contains("/user/login.php", true)
            || response.request.url.toUrl().path.contains("/user/refresh.php", true)
        ) {
            //here we do not need a authorization token
        } else {
            if (response.code == 401) {
                val userItem = PreferenceData.getInstance().getUser(context)
                userItem?.let { user ->
                    val tokenResponse = ApiService.create(context).refreshTokenBlocking(
                        RefreshTokenRequest(user.refresh)
                    ).execute()

                    if (tokenResponse.isSuccessful) {
                        tokenResponse.body()?.let {
                            val new_user = User(
                                user.username,
                                user.email,
                                user.uid,
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
        }
        return null
    }
}
