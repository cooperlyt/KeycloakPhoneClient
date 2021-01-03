package cc.coopersoft.accesstoken.api.callback;

public interface VerificationCallback {

  void onError(String phoneNumber, ErrorResult error);

  void onFailure(String phoneNumber);

  void onSuccess(String phoneNumber);
}
