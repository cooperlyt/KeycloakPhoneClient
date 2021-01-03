package cc.coopersoft.accesstoken.keycloak;

import android.util.Log;
import cc.coopersoft.accesstoken.api.*;
import cc.coopersoft.accesstoken.api.callback.AuthenticationCallback;
import cc.coopersoft.accesstoken.api.callback.CodeRequestCallback;
import cc.coopersoft.accesstoken.api.callback.VerificationCallback;
import cc.coopersoft.accesstoken.keycloak.model.KeycloakCodeResult;
import cc.coopersoft.accesstoken.keycloak.model.KeycloakErrorResult;
import cc.coopersoft.accesstoken.keycloak.model.KeycloakTokenInfo;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

public class KeycloakTokenHolder implements PhoneSupportAccessTokenHolder {

  public static class Builder {
    private String authenticationCodeUrl = null;

    private String verificationCodeUrl = null;

    private String registrationCodeUrl = null;

    private String accessTokenUrl = null;



    private String host = "";

    private Integer port = null;

    private String realms = null;

    private String scheme = "https";

    private String clientId;

    private String clientSecret;

    private String scope = null;

    private final OkHttpClient.Builder okHttpClientBuilder;

    private KeycloakTokenStorage storage;


    public Builder() {
      okHttpClientBuilder = new OkHttpClient.Builder();
    }

    private String getRootUrl(){
      if (host == null || realms == null){
        throw new ExceptionInInitializerError("please set host info!");
      }

      return scheme + "://" +
              host + Optional.ofNullable(port).map(p -> ":" + port).orElse("") +
              "/auth/realms/" + realms + "/";
    }

    public String getAuthenticationCodeUrl(){
      return Optional.ofNullable(authenticationCodeUrl).orElse(getRootUrl() + "sms/authentication-code" );
    }

    public String getVerificationCodeUrl(){
      return Optional.ofNullable(verificationCodeUrl).orElse(getRootUrl() + "sms/verification-code");
    }

    public String getRegistrationCodeUrl(){
      return Optional.ofNullable(registrationCodeUrl).orElse(getRootUrl() + "sms/registration-code");
    }

    public String getAccessTokenUrl(){
      return Optional.ofNullable(accessTokenUrl).orElse(getRootUrl() + "protocol/openid-connect/token");
    }

    public KeycloakTokenStorage getStorage() {
      return storage;
    }


    public String getClientId() {
      return clientId;
    }

    public String getClientSecret() {
      return clientSecret;
    }

    public String getScope() {
      return scope;
    }

    public OkHttpClient.Builder okHttpClientBuilder() {
      return okHttpClientBuilder;
    }

    public Builder scheme(@NotNull String scheme){
      this.scheme = scheme;
      return this;
    }

    public Builder host(@Nullable String host){
      this.host = host;
      return this;
    }

    public Builder port(Integer port){
      this.port = port;
      return this;
    }

    public Builder realms(@Nullable String realms){
      this.realms = realms;
      return this;
    }

    public Builder authenticationCodeUrl(@Nullable String url){
      this.authenticationCodeUrl = url;
      return this;
    }

    public Builder verificationCodeUrl(@Nullable String url){
      this.verificationCodeUrl = url;
      return this;
    }

    public Builder registrationCodeUrl(@Nullable String url){
      this.registrationCodeUrl = url;
      return this;
    }

    public Builder accessTokenUrl(@Nullable String url){
      this.accessTokenUrl = url;
      return this;
    }

    public Builder clientId(@Nullable String clientId){
      this.clientId = clientId;
      return this;
    }

    public Builder clientSecret(String clientSecret){
      this.clientSecret = clientSecret;
      return this;
    }

    public Builder clientScope(String scope){
      this.scope = scope;
      return this;
    }

    public Builder storage(KeycloakTokenStorage storage){
      this.storage = storage;
      return this;
    }

    public KeycloakTokenHolder build(){
      return new KeycloakTokenHolder(this);
    }
  }

  private static final String LOG_TAG = "Keycloak";

  private KeycloakTokenInfo token;

  private OkHttpClient okHttpClient;
  private String authenticationCodeUrl;
  private String verificationCodeUrl;
  private String registrationCodeUrl;
  private String accessTokenUrl;
  private KeycloakTokenStorage storage;
  private String clientId;
  private String clientSecret;
  private String scope;


  KeycloakTokenHolder(Builder keycloakServer) {
    this.okHttpClient = keycloakServer.okHttpClientBuilder.build();
    this.authenticationCodeUrl = keycloakServer.getAuthenticationCodeUrl();
    this.verificationCodeUrl = keycloakServer.getVerificationCodeUrl();
    this.registrationCodeUrl = keycloakServer.getRegistrationCodeUrl();
    this.accessTokenUrl = keycloakServer.getAccessTokenUrl();
    this.clientId = keycloakServer.clientId;
    this.clientSecret = keycloakServer.clientSecret;
    this.scope = keycloakServer.scope;
    this.storage = keycloakServer.storage;
  }

  private KeycloakTokenHolder(){

  }

  private Optional<KeycloakTokenInfo> getToken(){
    if (token == null){
      token = storage.loadToken();
    }
    Log.d("Keycloak", "getToken:" + token);
    return Optional.ofNullable(token);
  }

  private boolean isExpires(long expiresIn, Date created){
    // sub one minute 1000 * 60
    return created.getTime() + expiresIn * 1000 - 1000 * 60 < System.currentTimeMillis();
  }

  private void saveToken(KeycloakTokenInfo token){
    this.token = token;
    storage.storeToken(token);
  }

//
//  private Optional<String> refreshToken(){
//    return getToken().flatMap(t -> isExpires(token.getRefreshExpiresIn(),token.getCreated()) ? Optional.empty() : Optional.of(t.getRefreshToken()));
//  }

  private Optional<KeycloakTokenInfo> validToken(@NotNull KeycloakTokenInfo token){
    if (isExpires(token.getExpiresIn(),token.getCreated())){
      Log.d("Keycloak", "token is expires" );
      if (isExpires(token.getRefreshExpiresIn(),token.getCreated())){
        Log.d("Keycloak", "refresh token is expires" );
        return Optional.empty();
      }
      return refreshToken(token.getRefreshToken());
    }
    return Optional.of(token);
  }

  @Override
  public Optional<String> getAccessToken(){
    return getToken()
            .flatMap(this::validToken)
            .map(t -> t.getTokenType() + " " + t.getAccessToken());
  }

  @Override
  public boolean hasToken(){
    return getToken().map(t -> {
      boolean expires = isExpires(token.getExpiresIn(),token.getCreated()) && isExpires(token.getRefreshExpiresIn(),token.getCreated());
      if (expires){
        clearToken();
      }
      return !expires;
    }).orElse(false);
  }

  @Override
  public void clearToken() {
    token = null;
    storage.removeToken();
  }

  @Override
  public void sendAuthenticationCode(String phoneNumber, CodeRequestCallback callback){
    sendCode(authenticationCodeUrl,phoneNumber, callback);
  }

  @Override
  public void sendRegistrationCode(String phoneNumber, CodeRequestCallback callback){
    sendCode(registrationCodeUrl,phoneNumber, callback);
  }

  @Override
  public void sendVerificationCode(String phoneNumber, CodeRequestCallback callback){
    sendCode(verificationCodeUrl,phoneNumber, callback);
  }

  @Override
  public void verificationCode(String phoneNumber, String code, VerificationCallback callback){

    Optional<String> accessToken = getAccessToken();
    if (!accessToken.isPresent()){
      callback.onError(phoneNumber, KeycloakErrorResult.builder().error("no token").errorDescription("must be have a token").build());
      return;
    }

    HttpUrl url = Objects.requireNonNull(HttpUrl.parse(verificationCodeUrl))
            .newBuilder()
            .addQueryParameter("phoneNumber",phoneNumber)
            .addQueryParameter("code",code)
            .build();
    RequestBody body = RequestBody.create(null, new byte[0]);
    Request request = new Request.Builder()
            .addHeader("authorization", getAccessToken().orElseThrow(IllegalArgumentException::new))
            .url(url)
            .post(body)
            .build();
    okHttpClient.newCall(request).enqueue(new Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        callback.onFailure(phoneNumber);
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        if (response.isSuccessful()){
          callback.onSuccess(phoneNumber);
        }else{
          callback.onFailure(phoneNumber);
        }
      }
    });

  }

  @Override
  public void requireToken(Credential credential, AuthenticationCallback callback){
    if (clientId == null){
      throw new ExceptionInInitializerError("client id not set!");
    }

    FormBody.Builder formBody = new FormBody.Builder();
    formBody.add("client_id",clientId)
            .add("grant_type","password");
    credential.getContext().forEach((k,v) -> formBody.add(k,v));
    Optional.ofNullable(clientSecret).ifPresent(s -> formBody.add("client_secret",s));
    Optional.ofNullable(scope).ifPresent(s -> formBody.add("scope",scope));
    Request request = new Request.Builder()
            .addHeader("Content-Type","application/x-www-form-urlencoded")
            .url(accessTokenUrl)
            .post(formBody.build())
            .build();
    okHttpClient.newCall(request).enqueue(new Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        Log.e(LOG_TAG, "failure" ,e);
        callback.onFailure(KeycloakErrorResult.builder().
                        error(e.getClass().getSimpleName()).
                        errorDescription(e.getMessage()).build());
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        if (response.isSuccessful()){
          assert response.body() != null;
          saveToken(parseToken(response.body().string()));
          callback.onSuccess();
        }else{
          if (response.body() != null){
            callback.onFailure(parseErrorResult(response.body().string()));
          }else{
            callback.onFailure(new KeycloakErrorResult("unknow","unknow error"));
          }
        }
      }
    });
  }

  @Override
  public Optional<String> refreshToken(){
    return getToken().flatMap(t -> refreshToken(token.getRefreshToken()))
            .map(t -> t.getTokenType() + " " + t.getAccessToken());
  }

  private Optional<KeycloakTokenInfo> refreshToken(String refreshToken){
    Log.d("Keycloak", "refresh token");
    if (clientId == null){
      throw new ExceptionInInitializerError("client id not set!");
    }
    Log.d("Keycloak","refresh token for:" + accessTokenUrl);
    FormBody.Builder formBody = new FormBody.Builder();
    formBody.add("client_id",clientId)
            .add("grant_type","refresh_token")
            .add("refresh_token", refreshToken);
    Optional.ofNullable(clientSecret).ifPresent(s -> formBody.add("client_secret",s));
    Optional.ofNullable(scope).ifPresent(s -> formBody.add("scope",scope));
    Request request = new Request.Builder()
            .addHeader("Content-Type","application/x-www-form-urlencoded")
            .url(accessTokenUrl)
            .post(formBody.build())
            .build();
    try {
      Response response = okHttpClient.newCall(request).execute();
      if (response.isSuccessful()) {
        Log.d(LOG_TAG, "token is refresh");
        assert response.body() != null;
        KeycloakTokenInfo token = parseToken(response.body().string());
        saveToken(token);
        return Optional.of(token);
      } else {
        if (response.body() != null) {
          Log.e(LOG_TAG,"refresh token fail! body is:" + response.body().string());
        } else {
          Log.e(LOG_TAG,"refresh token fail! body is null!");
        }
        //remove this token? maybe only network fail!
      }
    }catch (IOException e){
      Log.e(LOG_TAG,"refresh token fail! body is:" + e.getMessage(),e);
    }
    return Optional.empty();
  }


  private void sendCode(String requestUrl, String phoneNumber, CodeRequestCallback callback){
    HttpUrl url = Objects.requireNonNull(HttpUrl.parse(requestUrl)).newBuilder().addQueryParameter("phoneNumber",phoneNumber).build();
    Request request = new Request.Builder()
            .addHeader("Accept", "application/json; charset=utf-8")
            .addHeader("Cache-Control","no-cache")
            .url(url)
            .build();
    okHttpClient.newCall(request).enqueue(new Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        callback.onFailure(phoneNumber, new KeycloakErrorResult("unknow"));
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        if (response.isSuccessful()){
          assert response.body() != null;
          callback.onSuccess(phoneNumber,parseCodeResult(response.body().string()));
        }else{
          if (response.body() != null) {
            callback.onFailure(phoneNumber, parseErrorResult(response.body().string()));
          }else{
            callback.onFailure(phoneNumber, new KeycloakErrorResult("unknow"));
          }
        }
      }
    });
  }

  private KeycloakErrorResult parseErrorResult(String json){
    if (json == null || json.trim().length() <= 0){
      return new KeycloakErrorResult("unknow");
    }
    final GsonBuilder gsonBuilder = new GsonBuilder();
    gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
    gsonBuilder.setLenient();
    return gsonBuilder.create().fromJson(json, KeycloakErrorResult.class);
  }

  private KeycloakTokenInfo parseToken(String json){
    Log.d("Keycloak","parseToken" + json);
    final GsonBuilder gsonBuilder = new GsonBuilder();
    gsonBuilder.setLenient();
    gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
    KeycloakTokenInfo result = gsonBuilder.create().fromJson(json, KeycloakTokenInfo.class);
    result.setCreated(new Date());
    return result;
  }

  private KeycloakCodeResult parseCodeResult(String json){
    final GsonBuilder gsonBuilder = new GsonBuilder();
    gsonBuilder.setLenient();
    gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
    KeycloakCodeResult result = gsonBuilder.create().fromJson(json, KeycloakCodeResult.class);
    result.setCreated(new Date());
    return result;
  }

}
