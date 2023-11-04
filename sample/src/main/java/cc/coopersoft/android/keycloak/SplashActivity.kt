package cc.coopersoft.android.keycloak

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import cc.coopersoft.android.keycloak.KeycloakClient.accessTokenHolder

class SplashActivity : AppCompatActivity() {
    private val mHandler = Handler(Looper.getMainLooper())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        mHandler.postDelayed({ this.start() }, 3000)
    }

    private fun start() {
        if (accessTokenHolder()!!.hasToken()) {
            MainActivity.start(this)
        } else {
            LoginActivity.start(this)
        }
    }
}