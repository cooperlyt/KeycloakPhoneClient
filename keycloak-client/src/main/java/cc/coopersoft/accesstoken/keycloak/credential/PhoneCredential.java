package cc.coopersoft.accesstoken.keycloak.credential;

import cc.coopersoft.accesstoken.api.Credential;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
public class PhoneCredential implements Credential {

  private static final String PHONE_NUMBER_PARAM_NAME = "phoneNumber";

  private static final String CODE_PARAM_NAME = "code";

  Map<String,String> context = new HashMap<>(2);


  @Builder
  public PhoneCredential(String phone, String code) {
    setCode(code);
    setPhoneNumber(phone);
  }

  @Override
  public Map<String, String> getContext() {
    return context;
  }

  public void setPhoneNumber(String number){
    context.put(PHONE_NUMBER_PARAM_NAME,number);
  }

  public String getPhoneNumber(){
    return context.get(PHONE_NUMBER_PARAM_NAME);
  }

  public void setCode(String code){
    context.put(CODE_PARAM_NAME,code);
  }

  public String getCode(){
    return context.get(CODE_PARAM_NAME);
  }
}
