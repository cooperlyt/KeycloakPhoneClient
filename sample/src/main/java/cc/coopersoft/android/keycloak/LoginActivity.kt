package cc.coopersoft.android.keycloak

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import cc.coopersoft.accesstoken.api.callback.AuthenticationCallback
import cc.coopersoft.accesstoken.api.callback.CodeRequestCallback
import cc.coopersoft.accesstoken.api.callback.ErrorResult
import cc.coopersoft.accesstoken.keycloak.credential.PhoneCredential
import cc.coopersoft.android.keycloak.KeycloakClient.accessTokenHolder
import cc.coopersoft.android.keycloak.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private var binding: ActivityLoginBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        initView()
    }

    private fun toast(text: String) {
        val toast = Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
    }

    private fun loading(loading: Boolean) {
        runOnUiThread {
            binding?.login?.isEnabled = !loading
            binding?.loginProgress?.visibility =
                if (loading) View.VISIBLE else View.GONE
            binding?.login?.text = if (loading) "" else "登录"
        }
    }

    private fun initView() {
        binding?.phoneNumber?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                binding?.requestAuthCode?.isEnabled = charSequence.toString().length == 11
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        binding?.requestAuthCode?.setOnClickListener { view: View? ->
            binding?.authCode?.isFocusable = true
            binding?.requestAuthCode?.isEnabled = false
            accessTokenHolder()?.sendAuthenticationCode(
                    binding?.phoneNumber?.text.toString(),
                    object : CodeRequestCallback {
                        override fun onFailure(phoneNumber: String?, error: ErrorResult) {
                            runOnUiThread {
                                binding?.requestAuthCode?.isEnabled = true
                                error.error?.let { Log.e(TAG, it) }
                                toast("send code fail!")
                            }
                        }

                        override fun onSuccess(phoneNumber: String?, result: CodeRequestCallback.Result) {
                            Looper.prepare()
                            object : CountDownTimer(result.expiresIn * 1000, 1000) {
                                override fun onTick(millisUntilFinished: Long) {
                                    runOnUiThread {
                                        binding?.requestAuthCode?.isEnabled = false
                                        binding?.requestAuthCode?.text =
                                            "send(" + millisUntilFinished / 1000 + ")"
                                    }
                                }

                                override fun onFinish() {
                                   runOnUiThread {
                                       binding?.requestAuthCode?.isEnabled = true
                                       binding?.requestAuthCode?.text = "resend"
                                   }
                                }
                            }.start()
                            Looper.loop()
                        }
                    })
        }
        binding?.login?.setOnClickListener { view: View? ->
            val phone = binding?.phoneNumber?.text.toString().trim { it <= ' ' }
            val code = binding?.authCode?.text.toString().trim { it <= ' ' }
            if (phone.length != 11) {
                toast("phone number error")
                return@setOnClickListener
            }
            if (code.length != 6) {
                toast("code error")
                return@setOnClickListener
            }
            loading(true)
            accessTokenHolder()!!
                .requireToken(
                    PhoneCredential(phone,code),
                    object : AuthenticationCallback {
                        override fun onFailure(error: ErrorResult) {
                            runOnUiThread {
                                toast("login fail! ${error.error}")
                                loading(false)
                            }
                        }

                        override fun onSuccess() {
                            MainActivity.start(this@LoginActivity)
                            finish()
                        }
                    })
        }
    }

    companion object {
        const val TAG = "LoginActivity"
        @JvmStatic
        fun start(context: Context) {
            context.startActivity(Intent(context, LoginActivity::class.java))
        }
    }
}