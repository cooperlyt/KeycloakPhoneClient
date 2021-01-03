package cc.coopersoft.accesstoken.android.storage;

import android.content.SharedPreferences;
import cc.coopersoft.accesstoken.keycloak.KeycloakTokenStorage;
import cc.coopersoft.accesstoken.keycloak.model.KeycloakTokenInfo;
import com.google.gson.GsonBuilder;

public class SharedPreferencesStorage implements KeycloakTokenStorage {

  private final SharedPreferences sharedPreferences;
  private final String keyName;
  private GsonBuilder gsonBuilder;

  public SharedPreferencesStorage(SharedPreferences sharedPreferences, String keyName) {
    gsonBuilder = new GsonBuilder();
    this.sharedPreferences = sharedPreferences;
    this.keyName = keyName;
  }

  public void storeToken(KeycloakTokenInfo token){
    sharedPreferences
            .edit()
            .putString(keyName,gsonBuilder.create().toJson(token))
            .apply();
  }

  public KeycloakTokenInfo loadToken(){
    String token = sharedPreferences.getString(keyName,"");
    if (token.length() <=0 ){
      return null;
    }
    return gsonBuilder.create().fromJson(token,KeycloakTokenInfo.class);
  }

  public void removeToken(){
    sharedPreferences.edit().remove(keyName).apply();
  }
}
