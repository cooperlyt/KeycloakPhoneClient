package cc.coopersoft.accesstoken.keycloak.model

import java.util.Date

class KeycloakTokenInfo {
    val accessToken: String? = null
    val expiresIn: Long = 0
    val refreshToken: String? = null
    val refreshExpiresIn: Long = 0
    val tokenType: String? = null
    val notBeforePolicy = 0
    val sessionState: String? = null
    val scope: String? = null
    var created: Date? = null
}