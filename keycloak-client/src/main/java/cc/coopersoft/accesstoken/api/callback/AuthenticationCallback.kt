package cc.coopersoft.accesstoken.api.callback

interface AuthenticationCallback {
    fun onFailure(error: ErrorResult)
    fun onSuccess()
}