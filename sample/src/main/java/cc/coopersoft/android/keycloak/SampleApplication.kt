package cc.coopersoft.android.keycloak

import android.app.Application
import cc.coopersoft.accesstoken.android.storage.SharedPreferencesStorage
import cc.coopersoft.android.keycloak.KeycloakClient.init

class SampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        //storage: you can custom for cc.coopersoft.accesstoken.keycloak.KeycloakTokenStorage
        //like tencent mmkv
        init(
            SharedPreferencesStorage(
                getSharedPreferences("keycloak-sample", MODE_PRIVATE),
                ACCESS_TOKEN_STORE_KEY
            )
        )

//      new KeycloakTokenStorage() {
//    @Override
//    public void storeToken(KeycloakTokenInfo token) {
//      MmkvHelper.getInstance().putObject(ACCESS_TOKEN_STORAGE_KEY, token);
//    }
//
//    @Override
//    public KeycloakTokenInfo loadToken() {
//      return MmkvHelper.getInstance().getObject(ACCESS_TOKEN_STORAGE_KEY, KeycloakTokenInfo.class);
//    }
//
//    @Override
//    public void removeToken() {
//      MmkvHelper.getInstance().remove(ACCESS_TOKEN_STORAGE_KEY);
//    }
//    }
    }

    companion object {
        private const val ACCESS_TOKEN_STORE_KEY = "access_token"
    }
}