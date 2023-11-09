package cc.coopersoft.accesstoken.keycloak.credential

import cc.coopersoft.accesstoken.api.Credential

class PhoneCredential (phone: String, code: String) : Credential {
    override val context: MutableMap<String, String> = HashMap(2)

    init {
        setCode(code)
        setPhoneNumber(phone)
    }

    fun setPhoneNumber(number: String) {
        context[PHONE_NUMBER_PARAM_NAME] = number
    }

    val phoneNumber: String?
        get() = context[PHONE_NUMBER_PARAM_NAME]

    fun setCode(code: String) {
        context[CODE_PARAM_NAME] = code
    }

    val code: String?
        get() = context[CODE_PARAM_NAME]

    companion object {
        private const val PHONE_NUMBER_PARAM_NAME = "phoneNumber"
        private const val CODE_PARAM_NAME = "code"
    }
}