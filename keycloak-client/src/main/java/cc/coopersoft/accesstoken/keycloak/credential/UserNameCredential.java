package cc.coopersoft.accesstoken.keycloak.credential;

import cc.coopersoft.accesstoken.api.Credential;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
public class UserNameCredential implements Credential {

  private static final String USER_NAME_PARAM_NAME = "username";

  private static final String PASSWORD_PARAM_NAME = "password";

  Map<String,String> context = new HashMap<>(2);

  @Builder
  public UserNameCredential(String username, String password) {
    setPassword(password);
    setUserName(username);
  }

  @Override
  public Map<String, String> getContext() {
    return context;
  }

  public void setUserName(String userName){
    context.put(USER_NAME_PARAM_NAME,userName);
  }

  public String getUserName(){
    return context.get(USER_NAME_PARAM_NAME);
  }

  public void setPassword(String password){
    context.put(PASSWORD_PARAM_NAME,password);
  }

  public String getPassword(){
    return context.get(PASSWORD_PARAM_NAME);
  }
}
