package cc.coopersoft.accesstoken.keycloak.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public
class KeycloakTokenInfo{
  private String accessToken;
  private long expiresIn;
  private String refreshToken;
  private long refreshExpiresIn;
  private String tokenType;
  private int notBeforePolicy;
  private String sessionState;
  private String scope;
  private Date created;
}
