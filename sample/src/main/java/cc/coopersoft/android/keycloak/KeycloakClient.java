package cc.coopersoft.android.keycloak;

import cc.coopersoft.accesstoken.keycloak.KeycloakTokenHolder;
import cc.coopersoft.accesstoken.keycloak.KeycloakTokenStorage;


public class KeycloakClient {

  private volatile static KeycloakTokenHolder accessTokenHolder;

  public static void init(KeycloakTokenStorage storage){
    accessTokenHolder = new KeycloakTokenHolder.Builder()
            .host("www.XXX.com") // root address: like "www.XXX.com"
            .realms("realms")  // realms: config in your keycloak
            .clientId("client") // clientId: config in your keycloak
            .clientSecret("XXXXX-XXXXX-XXXX-XXXX-1XXXXX") //clientSecret : clientId: config in your keycloak
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
