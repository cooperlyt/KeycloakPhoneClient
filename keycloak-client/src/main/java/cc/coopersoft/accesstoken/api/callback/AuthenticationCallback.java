package cc.coopersoft.accesstoken.api.callback;

public interface AuthenticationCallback {

  void onFailure(ErrorResult error);

  void onSuccess();

}
