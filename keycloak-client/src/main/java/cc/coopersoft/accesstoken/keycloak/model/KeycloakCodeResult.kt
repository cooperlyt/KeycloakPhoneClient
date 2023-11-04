package cc.coopersoft.accesstoken.keycloak.model

import cc.coopersoft.accesstoken.api.callback.CodeRequestCallback
import java.util.Date

data class KeycloakCodeResult(override val expiresIn: Long, override var created: Date?) :
    CodeRequestCallback.Result {
    constructor() : this(0, null)
}