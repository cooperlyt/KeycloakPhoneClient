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
import cc.coopersoft.accesstoken.api.callback.CodeRequestCallback
import cc.coopersoft.accesstoken.api.callback.ErrorResult
import cc.coopersoft.accesstoken.api.callback.VerificationCallback
import cc.coopersoft.android.keycloak.KeycloakClient.accessTokenHolder
import cc.coopersoft.android.keycloak.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        initView()
    }

    private fun toast(text: String) {
        val toast = Toast.makeText(applicationContext, text, Toast.LENGTH_LONG)
        toast.setGravity(Gravity.TOP, 0, 0)
        toast.show()
    }

    private fun initView() {
        binding?.phoneNumber?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                binding?.requestAuthCode?.isEnabled = charSequence.toString().length == 11
            }

            override fun afterTextChanged(editable: Editable) {}
        })

        // send  verification code
        binding?.requestAuthCode?.setOnClickListener { view: View? ->
            binding?.authCode?.isFocusable = true
            binding?.requestAuthCode?.isEnabled = false
            accessTokenHolder()!!
                .sendVerificationCode(
                    binding?.phoneNumber?.text.toString(),
                    object : CodeRequestCallback {
                        override fun onFailure(phoneNumber: String?, error: ErrorResult) {
                            binding?.requestAuthCode?.isEnabled = true
                            error.error?.let { Log.e(TAG, it) }
                            runOnUiThread { toast("send code fail!") }
                        }

                        override fun onSuccess(
                            phoneNumber: String?,
                            result: CodeRequestCallback.Result
                        ) {
                            Looper.prepare()
                            object : CountDownTimer(result!!.expiresIn * 1000, 1000) {
                                override fun onTick(millisUntilFinished: Long) {
                                    binding?.requestAuthCode?.isEnabled = false
                                    binding?.requestAuthCode?.text = "send(" + millisUntilFinished / 1000 + ")"
                                }

                                override fun onFinish() {
                                    binding?.requestAuthCode?.isEnabled = true
                                    binding?.requestAuthCode?.text = "resend"
                                }
                            }.start()
                            Looper.loop()
                        }
                    })
        }

        // verification action
        binding?.verification?.setOnClickListener { view: View? ->
            val verificationTask = Runnable {
                accessTokenHolder()!!
                    .verificationCode(
                        binding?.phoneNumber?.text.toString(),
                        binding?.authCode?.text.toString(),
                        object : VerificationCallback {
                            override fun onError(phoneNumber: String?, error: ErrorResult) {
                                runOnUiThread { toast("valid fail!" + error.errorDescription) }
                            }

                            override  fun onFailure(phoneNumber: String?) {
                                runOnUiThread { toast("valid fail!") }
                            }

                            override   fun onSuccess(phoneNumber: String?) {
                                runOnUiThread { toast("valid success") }
                            }
                        })
            }
            Thread(verificationTask).start()
        }


        //logout action
        binding?.logout?.setOnClickListener { view: View? ->
            accessTokenHolder()!!.clearToken()
            LoginActivity.start(this)
        }
    }

    companion object {
        const val TAG = "MainActivity"
        @JvmStatic
        fun start(context: Context) {
            context.startActivity(Intent(context, MainActivity::class.java))
        }
    }
}