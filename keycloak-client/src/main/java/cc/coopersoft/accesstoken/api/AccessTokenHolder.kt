package cc.coopersoft.accesstoken.api

import cc.coopersoft.accesstoken.api.callback.AuthenticationCallback
import java.util.Optional

interface AccessTokenHolder {
    val accessToken: Optional<String>
    fun refreshToken(): Optional<String>
    fun clearToken()
    fun hasToken(): Boolean
    fun requireToken(credential: Credential?, callback: AuthenticationCallback?)
}