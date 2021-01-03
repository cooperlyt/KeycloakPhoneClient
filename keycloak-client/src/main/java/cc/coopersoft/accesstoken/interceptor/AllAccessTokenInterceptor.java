package cc.coopersoft.accesstoken.interceptor;

import cc.coopersoft.accesstoken.api.AccessTokenHolder;
import okhttp3.Response;

import java.io.IOException;

public class AllAccessTokenInterceptor extends AccessTokenInterceptor {

  public AllAccessTokenInterceptor(AccessTokenHolder tokenHolder) {
    super(tokenHolder);
  }

  @Override
  public Response intercept(Chain chain) throws IOException {
    return accessTokenRequest(chain);
  }
}
