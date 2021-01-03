package cc.coopersoft.accesstoken.keycloak.model;

import cc.coopersoft.accesstoken.api.callback.CodeRequestCallback;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public final class KeycloakCodeResult implements CodeRequestCallback.Result {
  private long expiresIn;
  private Date created;
}
