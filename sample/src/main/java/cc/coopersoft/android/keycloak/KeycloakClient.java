package cc.coopersoft.android.keycloak;

import cc.coopersoft.accesstoken.keycloak.KeycloakTokenHolder;
import cc.coopersoft.accesstoken.keycloak.KeycloakTokenStorage;


public class KeycloakClient {

  private volatile static KeycloakTokenHolder accessTokenHolder;

  public static void init(KeycloakTokenStorage storage){
    accessTokenHolder = new KeycloakTokenHolder.Builder()
            .host("www.litle.fun") // root address: like "www.XXX.com"
            .realms("havechat")  // realms: config in your keycloak
            .clientId("mobile") // clientId: config in your keycloak
            .clientSecret("c0843a31-2309-452b-b1bf-1f4ebfcfcb54") //clientSecret : clientId: config in your keycloak
            .storage(storage) // storage: you can custom for cc.coopersoft.accesstoken.keycloak.KeycloakTokenStorage
            .build();
  }

  public static KeycloakTokenHolder accessTokenHolder() {
    if (accessTokenHolder == null) {
      synchronized (KeycloakClient.class) {
      }
    }
    if (accessTokenHolder == null) {
      throw new IllegalStateException("must init first");
    }
    return accessTokenHolder;
  }
}
