package cc.coopersoft.accesstoken.api;

import cc.coopersoft.accesstoken.api.callback.CodeRequestCallback;
import cc.coopersoft.accesstoken.api.callback.VerificationCallback;
import cc.coopersoft.accesstoken.interceptor.AccessTokenInterceptor;

public interface PhoneSupportAccessTokenHolder extends AccessTokenHolder{

  void sendAuthenticationCode(String phoneNumber, CodeRequestCallback callback);

  void sendVerificationCode(String phoneNumber, CodeRequestCallback callback);

  void sendRegistrationCode(String phoneNumber, CodeRequestCallback callback);

  void verificationCode(String phoneNumber, String code, VerificationCallback callback);

}
