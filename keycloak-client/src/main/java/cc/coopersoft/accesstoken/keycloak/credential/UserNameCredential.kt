package cc.coopersoft.accesstoken.keycloak.credential

import cc.coopersoft.accesstoken.api.Credential

class UserNameCredential (username: String, password: String) : Credential {
    override val context: MutableMap<String, String> = HashMap(2)

    init {
        setPassword(password)
        setUserName(username)
    }

    fun setUserName(userName: String) {
        context[USER_NAME_PARAM_NAME] = userName
    }

    val userName: String?
        get() = context[USER_NAME_PARAM_NAME]

    fun setPassword(password: String) {
        context[PASSWORD_PARAM_NAME] = password
    }

    val password: String?
        get() = context[PASSWORD_PARAM_NAME]

    companion object {
        private const val USER_NAME_PARAM_NAME = "username"
        private const val PASSWORD_PARAM_NAME = "password"
    }
}