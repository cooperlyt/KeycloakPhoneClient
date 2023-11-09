package cc.coopersoft.accesstoken.interceptor

import cc.coopersoft.accesstoken.api.AccessTokenHolder
import cc.coopersoft.accesstoken.interceptor.AccessTokenInterceptor
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class AllAccessTokenInterceptor(tokenHolder: AccessTokenHolder?) : AccessTokenInterceptor(
    tokenHolder!!
) {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        return accessTokenRequest(chain)
    }
}