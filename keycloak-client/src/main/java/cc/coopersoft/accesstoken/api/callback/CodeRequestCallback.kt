package cc.coopersoft.accesstoken.api.callback

import java.util.Date

interface CodeRequestCallback {
    interface Result {
        val expiresIn: Long
        val created: Date?
    }

    fun onFailure(phoneNumber: String?, error: ErrorResult)
    fun onSuccess(phoneNumber: String?, result: Result)
}