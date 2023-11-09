package cc.coopersoft.accesstoken.keycloak.model

import cc.coopersoft.accesstoken.api.callback.ErrorResult

class KeycloakErrorResult(override val error: String?, override val errorDescription: String?) :
    ErrorResult {
    constructor():this(null, null)
    constructor(error: String?) : this(error, null)
}