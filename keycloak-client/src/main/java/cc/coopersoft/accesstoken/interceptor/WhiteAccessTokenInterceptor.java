package cc.coopersoft.accesstoken.interceptor;

import cc.coopersoft.accesstoken.api.AccessTokenHolder;
import okhttp3.Response;

import java.io.IOException;
import java.util.List;

public class WhiteAccessTokenInterceptor extends AccessTokenInterceptor {

  private final List<String> whiteList;

  public WhiteAccessTokenInterceptor(AccessTokenHolder tokenHolder, List<String> whiteList) {
    super(tokenHolder);
    this.whiteList = whiteList;
  }


  @Override
  public Response intercept(Chain chain) throws IOException {
    if (whiteList.stream().anyMatch(s -> chain.request().url().toString().matches(s))){
      return accessTokenRequest(chain);
    }
    return chain.proceed(chain.request());
  }
}
