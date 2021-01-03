package cc.coopersoft.accesstoken.api;


import cc.coopersoft.accesstoken.api.callback.AuthenticationCallback;

import java.util.Optional;

public interface AccessTokenHolder {

  Optional<String> getAccessToken();

  Optional<String> refreshToken();

  void clearToken();

  boolean hasToken();

  void requireToken(Credential credential, AuthenticationCallback callback);


}
