package cc.coopersoft.android.keycloak;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import cc.coopersoft.accesstoken.android.storage.SharedPreferencesStorage;

public class SampleApplication extends Application {

  private static final String ACCESS_TOKEN_STORE_KEY ="access_token";


  @Override
  public void onCreate()
  {
    super.onCreate();

    //storage: you can custom for cc.coopersoft.accesstoken.keycloak.KeycloakTokenStorage
    //like tencent mmkv

    KeycloakClient.init(new SharedPreferencesStorage(getSharedPreferences("keycloak-sample", Context.MODE_PRIVATE),ACCESS_TOKEN_STORE_KEY));

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
}
