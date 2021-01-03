package cc.coopersoft.accesstoken.api.callback;

import java.util.Date;

public interface CodeRequestCallback {

  interface Result{
    long getExpiresIn();
    Date getCreated();
  }

  void onFailure(String phoneNumber, ErrorResult error);

  void onSuccess(String phoneNumber, Result result);
}
