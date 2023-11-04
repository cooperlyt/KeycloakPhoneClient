package cc.coopersoft.accesstoken.interceptor

import android.util.Log
import cc.coopersoft.accesstoken.api.AccessTokenHolder
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import java.io.IOException

abstract class AccessTokenInterceptor(private val tokenHolder: AccessTokenHolder) : Interceptor {
    private fun noTokenResponse(request: Request): Response {
        Log.w("Keycloak", "no Token return 401")
        return Response.Builder()
            .code(401)
            .protocol(Protocol.HTTP_2)
            .message("Unauthorized")
            .request(request)
            .body(ResponseBody.create(null, ByteArray(0)))
            .build()
    }

    @Throws(IOException::class)
    private fun proceed(chain: Interceptor.Chain, token: String): Response {
        Log.d("AccessTokenInterceptor", "add authorization to header")
        return chain.proceed(
            chain.request().newBuilder()
                .addHeader("authorization", token).build()
        )
    }

    @Throws(IOException::class)
    protected fun accessTokenRequest(chain: Interceptor.Chain): Response {
        if (tokenHolder.hasToken()) {
            var token = tokenHolder.accessToken
            if (token.isPresent) {
                val response = proceed(chain, token.get())
                if (response.code() == 401) {
                    Log.w("AccessTokenInterceptor", "server return 401 !  refresh token retry")
                    token = tokenHolder.refreshToken()
                    if (token.isPresent) {
                        return proceed(chain, token.get())
                    }
                }
                return response
            }
        }
        return noTokenResponse(chain.request())
    }
}