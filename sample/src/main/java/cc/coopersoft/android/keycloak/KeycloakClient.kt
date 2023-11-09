package cc.coopersoft.android.keycloak

import cc.coopersoft.accesstoken.keycloak.KeycloakTokenHolder
import cc.coopersoft.accesstoken.keycloak.KeycloakTokenStorage

object KeycloakClient {
    @Volatile
    private var accessTokenHolder: KeycloakTokenHolder? = null
    @JvmStatic
    fun init(storage: KeycloakTokenStorage?) {
        accessTokenHolder = KeycloakTokenHolder.Builder()
            .host("www.XXX.com")
            .preRealmsPath(null)
            .realms("realms")
            .clientId("client")
            .clientSecret("XXXXX-XXXXX-XXXX-XXXX-1XXXXX")
            .storage(storage)
            .build()
    }

    @JvmStatic
    fun accessTokenHolder(): KeycloakTokenHolder? {
        if (accessTokenHolder == null) {
            synchronized(KeycloakClient::class.java) {}
        }
        checkNotNull(accessTokenHolder) { "must init first" }
        return accessTokenHolder
    }
}