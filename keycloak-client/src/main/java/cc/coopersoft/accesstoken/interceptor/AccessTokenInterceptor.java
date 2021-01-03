package cc.coopersoft.accesstoken.interceptor;

import android.util.Log;
import cc.coopersoft.accesstoken.api.AccessTokenHolder;
import okhttp3.*;

import java.io.IOException;
import java.util.Optional;

public abstract class AccessTokenInterceptor implements Interceptor {

  private AccessTokenHolder tokenHolder;

  public AccessTokenInterceptor(AccessTokenHolder tokenHolder) {
    this.tokenHolder = tokenHolder;
  }


  protected Response noTokenResponse(Request request){
    Log.w("Keycloak","no Token return 401");
    return new Response.Builder()
            .code(401)
            .protocol(Protocol.HTTP_2)
            .message("Unauthorized")
            .request(request)
            .body(ResponseBody.create(null,new byte[0]))
            .build();
  }

  private Response proceed(Chain chain, String token )throws IOException{
    Log.d("AccessTokenInterceptor", "add authorization to header");
    return chain.proceed(chain.request().newBuilder()
            .addHeader("authorization", token).build());
  }

  protected Response accessTokenRequest(Chain chain) throws IOException {
    if (tokenHolder.hasToken()){
      Optional<String> token = tokenHolder.getAccessToken();

      if (token.isPresent()) {
        Response response = proceed(chain,token.get());
        if (response.code() == 401 ){
          Log.w("AccessTokenInterceptor", "server return 401 !  refresh token retry");
          token = tokenHolder.refreshToken();
          if (token.isPresent()){
            return proceed(chain,token.get());
          }
        }
        return response;
      }
    }
    return noTokenResponse(chain.request());
  }


}
