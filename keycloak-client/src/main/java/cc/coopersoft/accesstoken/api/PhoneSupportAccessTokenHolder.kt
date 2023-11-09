package cc.coopersoft.accesstoken.api

import cc.coopersoft.accesstoken.api.callback.CodeRequestCallback
import cc.coopersoft.accesstoken.api.callback.VerificationCallback

interface PhoneSupportAccessTokenHolder : AccessTokenHolder {
    fun sendAuthenticationCode(phoneNumber: String?, callback: CodeRequestCallback?)
    fun sendVerificationCode(phoneNumber: String?, callback: CodeRequestCallback?)
    fun sendRegistrationCode(phoneNumber: String?, callback: CodeRequestCallback?)
    fun verificationCode(phoneNumber: String?, code: String?, callback: VerificationCallback?)
}