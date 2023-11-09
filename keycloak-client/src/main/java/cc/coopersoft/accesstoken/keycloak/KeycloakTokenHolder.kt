package cc.coopersoft.accesstoken.keycloak

import android.util.Log
import cc.coopersoft.accesstoken.api.Credential
import cc.coopersoft.accesstoken.api.PhoneSupportAccessTokenHolder
import cc.coopersoft.accesstoken.api.callback.AuthenticationCallback
import cc.coopersoft.accesstoken.api.callback.CodeRequestCallback
import cc.coopersoft.accesstoken.api.callback.VerificationCallback
import cc.coopersoft.accesstoken.keycloak.model.KeycloakCodeResult
import cc.coopersoft.accesstoken.keycloak.model.KeycloakErrorResult
import cc.coopersoft.accesstoken.keycloak.model.KeycloakTokenInfo
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException
import java.lang.NullPointerException
import java.util.Date
import java.util.Optional
import java.util.function.BiConsumer

class KeycloakTokenHolder : PhoneSupportAccessTokenHolder {
    class Builder {
        private var authenticationCodeUrl: String? = null
        private var verificationCodeUrl: String? = null
        private var registrationCodeUrl: String? = null
        private var accessTokenUrl: String? = null
        //relative path after domain, before realms
        private var preRealmsPath: String = "/auth"
        private var host: String? = ""
        private var port: Int? = null
        private var realms: String? = null
        private var scheme = "https"
        var clientId: String? = null
            private set
        var clientSecret: String? = null
            private set
        var scope: String? = null
            private set
        val okHttpClientBuilder: OkHttpClient.Builder = OkHttpClient.Builder()
        var storage: KeycloakTokenStorage? = null
            private set

        private val rootUrl: String
            get() {
                if (host == null || realms == null) {
                    throw ExceptionInInitializerError("please set host info!")
                }
                return scheme + "://" +
                        host + Optional.ofNullable(port).map { p: Int? -> ":$port" }
                    .orElse("") +
                        "$preRealmsPath/realms/" + realms + "/"
            }

        fun getAuthenticationCodeUrl(): String {
            return Optional.ofNullable(authenticationCodeUrl)
                .orElse(rootUrl + "sms/authentication-code")
        }

        fun getVerificationCodeUrl(): String {
            return Optional.ofNullable(verificationCodeUrl)
                .orElse(rootUrl + "sms/verification-code")
        }

        fun getRegistrationCodeUrl(): String {
            return Optional.ofNullable(registrationCodeUrl)
                .orElse(rootUrl + "sms/registration-code")
        }

        fun getAccessTokenUrl(): String {
            return Optional.ofNullable(accessTokenUrl)
                .orElse(rootUrl + "protocol/openid-connect/token")
        }

        fun okHttpClientBuilder(): OkHttpClient.Builder {
            return okHttpClientBuilder
        }

        fun scheme(scheme: String): Builder {
            this.scheme = scheme
            return this
        }

        fun host(host: String?): Builder {
            this.host = host
            return this
        }

        fun port(port: Int?): Builder {
            this.port = port
            return this
        }

        /**
         * Relative path after domain, before realms
         *
         * @param preRealmsPath Relative path after domain, null means no relative path
         */
        fun preRealmsPath(preRealmsPath: String?): Builder {
            this.preRealmsPath = preRealmsPath ?: ""
            return this
        }

        fun realms(realms: String?): Builder {
            this.realms = realms
            return this
        }

        fun authenticationCodeUrl(url: String?): Builder {
            authenticationCodeUrl = url
            return this
        }

        fun verificationCodeUrl(url: String?): Builder {
            verificationCodeUrl = url
            return this
        }

        fun registrationCodeUrl(url: String?): Builder {
            registrationCodeUrl = url
            return this
        }

        fun accessTokenUrl(url: String?): Builder {
            accessTokenUrl = url
            return this
        }

        fun clientId(clientId: String?): Builder {
            this.clientId = clientId
            return this
        }

        fun clientSecret(clientSecret: String?): Builder {
            this.clientSecret = clientSecret
            return this
        }

        fun clientScope(scope: String?): Builder {
            this.scope = scope
            return this
        }

        fun storage(storage: KeycloakTokenStorage?): Builder {
            this.storage = storage
            return this
        }

        fun build(): KeycloakTokenHolder {
            return KeycloakTokenHolder(this)
        }
    }

    private var token: KeycloakTokenInfo? = null
    private var okHttpClient: OkHttpClient? = null
    private var authenticationCodeUrl: String? = null
    private var verificationCodeUrl: String? = null
    private var registrationCodeUrl: String? = null
    private var accessTokenUrl: String? = null
    private var storage: KeycloakTokenStorage? = null
    private var clientId: String? = null
    private var clientSecret: String? = null
    private var scope: String? = null

    internal constructor(keycloakServer: Builder) {
        okHttpClient = keycloakServer.okHttpClientBuilder.build()
        authenticationCodeUrl = keycloakServer.getAuthenticationCodeUrl()
        verificationCodeUrl = keycloakServer.getVerificationCodeUrl()
        registrationCodeUrl = keycloakServer.getRegistrationCodeUrl()
        accessTokenUrl = keycloakServer.getAccessTokenUrl()
        clientId = keycloakServer.clientId
        clientSecret = keycloakServer.clientSecret
        scope = keycloakServer.scope
        storage = keycloakServer.storage
    }

    private constructor() {}

    private fun getToken(): Optional<KeycloakTokenInfo> {
        if (token == null) {
            token = storage!!.loadToken()
        }
        Log.d("Keycloak", "getToken:$token")
        return Optional.ofNullable(token)
    }

    private fun isExpires(expiresIn: Long, created: Date?): Boolean {
        // sub one minute 1000 * 60
        return created!!.time + expiresIn * 1000 - 1000 * 60 < System.currentTimeMillis()
    }

    private fun saveToken(token: KeycloakTokenInfo) {
        this.token = token
        storage!!.storeToken(token)
    }

    //
    //  private Optional<String> refreshToken(){
    //    return getToken().flatMap(t -> isExpires(token.getRefreshExpiresIn(),token.getCreated()) ? Optional.empty() : Optional.of(t.getRefreshToken()));
    //  }
    private fun validToken(token: KeycloakTokenInfo): Optional<KeycloakTokenInfo> {
        if (isExpires(token.expiresIn, token.created)) {
            Log.d("Keycloak", "token is expires")
            if (isExpires(token.refreshExpiresIn, token.created)) {
                Log.d("Keycloak", "refresh token is expires")
                return Optional.empty()
            }
            return refreshToken(token.refreshToken)
        }
        return Optional.of(token)
    }

    override val accessToken: Optional<String>
        get() = getToken()
            .flatMap { token: KeycloakTokenInfo -> validToken(token) }
            .map { t: KeycloakTokenInfo -> t.tokenType + " " + t.accessToken }

    override fun hasToken(): Boolean {
        return getToken().map { t: KeycloakTokenInfo ->
            val expires = isExpires(t.expiresIn, t.created) && isExpires(
                t.refreshExpiresIn, t.created
            )
            if (expires) {
                clearToken()
            }
            !expires
        }.orElse(false)
    }

    override fun clearToken() {
        token = null
        storage!!.removeToken()
    }

    override fun sendAuthenticationCode(phoneNumber: String?, callback: CodeRequestCallback?) {
        sendCode(authenticationCodeUrl, phoneNumber, callback)
    }

    override fun sendRegistrationCode(phoneNumber: String?, callback: CodeRequestCallback?) {
        sendCode(registrationCodeUrl, phoneNumber, callback)
    }

    override fun sendVerificationCode(phoneNumber: String?, callback: CodeRequestCallback?) {
        sendCode(verificationCodeUrl, phoneNumber, callback)
    }

    override fun verificationCode(
        phoneNumber: String?,
        code: String?,
        callback: VerificationCallback?
    ) {
        val myAccessToken = accessToken
        val myVerificationCodeUrl = verificationCodeUrl

        if (!myAccessToken.isPresent) {
            callback!!.onError(
                phoneNumber,
                KeycloakErrorResult("no token","must be have a token")
            )
            return
        }

        if(myVerificationCodeUrl == null){
            throw ExceptionInInitializerError("verificationCodeUrl not set!")
        }

        val url = HttpUrl.parse(myVerificationCodeUrl)?.newBuilder()?.
            addQueryParameter("phoneNumber", phoneNumber)?.
            addQueryParameter("code", code)?.
            build() ?: throw NullPointerException("verificationCodeUrl")

        val body = RequestBody.create(null, ByteArray(0))
        val request = Request.Builder()
            .addHeader("authorization", myAccessToken.orElseThrow { IllegalArgumentException() })
            .url(url)
            .post(body)
            .build()
        okHttpClient!!.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback!!.onFailure(phoneNumber)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    callback!!.onSuccess(phoneNumber)
                } else {
                    callback!!.onFailure(phoneNumber)
                }
            }
        })
    }

    override fun requireToken(credential: Credential?, callback: AuthenticationCallback?) {
        val myClientId = clientId ?: throw ExceptionInInitializerError("client id not set!")
        val myAccessTokenUrl = accessTokenUrl ?: throw ExceptionInInitializerError("accessTokenUrl not set!")

        val formBody = FormBody.Builder().apply {
            add("client_id", myClientId)
            add("grant_type", "password")

            credential?.context?.forEach(BiConsumer { k: String?, v: String? -> add(k, v) })

            clientSecret?.let { add("client_secret", it) }
            scope?.let { add("scope", it) }
        }

        val request = Request.Builder()
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .url(myAccessTokenUrl)
            .post(formBody.build())
            .build()
        okHttpClient?.newCall(request)?.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(LOG_TAG, "failure", e)
                callback?.onFailure(KeycloakErrorResult(e.javaClass.simpleName, e.message))
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    assert(response.body() != null)
                    saveToken(parseToken(response.body()!!.string()))
                    callback?.onSuccess()
                } else {
                    if (response.body() != null) {
                        val body = response.body()?.string()
                        Log.e(LOG_TAG, body ?: "N/A")
                        callback?.onFailure(parseErrorResult(body))
                    } else {
                        Log.e(LOG_TAG, "unknown error")
                        callback?.onFailure(KeycloakErrorResult("unknown", "unknown error"))
                    }
                }
            }
        })
    }

    override fun refreshToken(): Optional<String> {
        return getToken().flatMap {
            refreshToken(token!!.refreshToken)
        }.map { t: KeycloakTokenInfo -> t.tokenType + " " + t.accessToken }
    }

    private fun refreshToken(refreshToken: String?): Optional<KeycloakTokenInfo> {
        Log.d("Keycloak", "refresh token")
        val myClientId = clientId ?: throw ExceptionInInitializerError("client id not set!")
        val myAccessTokenUrl = accessTokenUrl ?: throw ExceptionInInitializerError("accessTokenUrl not set!")

        Log.d("Keycloak", "refresh token for:$accessTokenUrl")
        val formBody = FormBody.Builder().apply {
            add("client_id", myClientId)
            add("grant_type", "refresh_token")
            add("refresh_token", refreshToken ?: "")

            clientSecret?.let { add("client_secret", it) }
            scope?.let { add("scope", it) }
        }

        val request = Request.Builder()
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .url(myAccessTokenUrl)
            .post(formBody.build())
            .build()
        try {
            val response = okHttpClient!!.newCall(request).execute()
            if (response.isSuccessful) {
                Log.d(LOG_TAG, "token is refresh")
                assert(response.body() != null)
                val token = parseToken(response.body()!!.string())
                saveToken(token)
                return Optional.of(token)
            } else {
                if (response.body() != null) {
                    Log.e(
                        LOG_TAG, "refresh token fail! body is:" + response.body()!!
                            .string()
                    )
                } else {
                    Log.e(LOG_TAG, "refresh token fail! body is null!")
                }
                //remove this token? maybe only network fail!
            }
        } catch (e: IOException) {
            Log.e(LOG_TAG, "refresh token fail! body is:" + e.message, e)
        }
        return Optional.empty()
    }

    private fun sendCode(
        requestUrl: String?,
        phoneNumber: String?,
        callback: CodeRequestCallback?
    ) {
        val myRequestUrl = requestUrl ?: throw ExceptionInInitializerError("requestUrl not set!")
        val url = HttpUrl.parse(myRequestUrl)?.newBuilder()?.addQueryParameter("phoneNumber", phoneNumber)?.build()
        val request = Request.Builder()
            .addHeader("Accept", "application/json; charset=utf-8")
            .addHeader("Cache-Control", "no-cache")
            .url(url)
            .build()
        okHttpClient!!.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback?.onFailure(phoneNumber, KeycloakErrorResult("unknown"))
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    assert(response.body() != null)
                    callback?.onSuccess(phoneNumber, parseCodeResult(response.body()!!.string()))
                } else {
                    if (response.body() != null) {
                        callback?.onFailure(
                            phoneNumber,
                            parseErrorResult(response.body()!!.string())
                        )
                    } else {
                        callback?.onFailure(phoneNumber, KeycloakErrorResult("unknow"))
                    }
                }
            }
        })
    }

    private fun parseErrorResult(json: String?): KeycloakErrorResult {
        if (json == null || json.trim { it <= ' ' }.isEmpty()) {
            return KeycloakErrorResult("unknow")
        }
        val gsonBuilder = GsonBuilder()
        gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        gsonBuilder.setLenient()
        return gsonBuilder.create().fromJson(json, KeycloakErrorResult::class.java)
    }

    private fun parseToken(json: String): KeycloakTokenInfo {
        Log.d("Keycloak", "parseToken$json")
        val gsonBuilder = GsonBuilder()
        gsonBuilder.setLenient()
        gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        val result = gsonBuilder.create().fromJson(json, KeycloakTokenInfo::class.java)
        result.created = Date()
        return result
    }

    private fun parseCodeResult(json: String): KeycloakCodeResult {
        val gsonBuilder = GsonBuilder()
        gsonBuilder.setLenient()
        gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        val result = gsonBuilder.create().fromJson(json, KeycloakCodeResult::class.java)
        result.created = Date()
        return result
    }

    companion object {
        private const val LOG_TAG = "KeycloakTokenHolder"
    }
}