package cc.coopersoft.accesstoken.android.storage

import android.content.SharedPreferences
import cc.coopersoft.accesstoken.keycloak.KeycloakTokenStorage
import cc.coopersoft.accesstoken.keycloak.model.KeycloakTokenInfo
import com.google.gson.GsonBuilder

class SharedPreferencesStorage(sharedPreferences: SharedPreferences, keyName: String) :
    KeycloakTokenStorage {
    private val sharedPreferences: SharedPreferences
    private val keyName: String
    private val gsonBuilder: GsonBuilder = GsonBuilder()

    init {
        this.sharedPreferences = sharedPreferences
        this.keyName = keyName
    }

    override fun storeToken(token: KeycloakTokenInfo) {
        sharedPreferences
            .edit()
            .putString(keyName, gsonBuilder.create().toJson(token))
            .apply()
    }

    override fun loadToken(): KeycloakTokenInfo? {
        val token = sharedPreferences.getString(keyName, "")
        return if (token!!.isEmpty()) null else gsonBuilder.create()
            .fromJson(token, KeycloakTokenInfo::class.java)
    }

    override fun removeToken() {
        sharedPreferences.edit().remove(keyName).apply()
    }
}