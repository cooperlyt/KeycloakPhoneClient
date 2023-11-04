package cc.coopersoft.accesstoken.interceptor

import cc.coopersoft.accesstoken.api.AccessTokenHolder
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class WhiteAccessTokenInterceptor(
    tokenHolder: AccessTokenHolder?,
    private val whiteList: List<String>
) : AccessTokenInterceptor(
    tokenHolder!!
) {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        return if (whiteList.stream()
                .anyMatch { s: String? ->
                    s?.let { chain.request().url().toString().matches(s.toRegex()) } == true
                }
        ) {
            accessTokenRequest(chain)
        } else chain.proceed(chain.request())
    }
}