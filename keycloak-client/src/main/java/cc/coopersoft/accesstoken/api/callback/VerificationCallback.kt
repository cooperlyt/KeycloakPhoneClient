package cc.coopersoft.accesstoken.api.callback

interface VerificationCallback {
    fun onError(phoneNumber: String?, error: ErrorResult)
    fun onFailure(phoneNumber: String?)
    fun onSuccess(phoneNumber: String?)
}