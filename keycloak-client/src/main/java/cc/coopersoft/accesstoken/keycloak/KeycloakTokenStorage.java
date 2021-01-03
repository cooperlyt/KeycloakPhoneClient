package cc.coopersoft.accesstoken.keycloak;

import cc.coopersoft.accesstoken.keycloak.model.KeycloakTokenInfo;

public interface KeycloakTokenStorage {

  void storeToken(KeycloakTokenInfo token);

  KeycloakTokenInfo loadToken();

  void removeToken();
}
