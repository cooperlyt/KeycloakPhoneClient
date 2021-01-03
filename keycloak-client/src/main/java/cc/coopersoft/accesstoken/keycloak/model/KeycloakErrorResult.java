package cc.coopersoft.accesstoken.keycloak.model;

import cc.coopersoft.accesstoken.api.callback.ErrorResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public
class KeycloakErrorResult implements ErrorResult {

  public KeycloakErrorResult(String error) {
    this.error = error;
  }

  private String error;
  private String errorDescription;
}
