package cc.coopersoft.android.keycloak;

import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class SplashActivity extends AppCompatActivity {

  private Handler mHandler = new Handler(Looper.getMainLooper());

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_splash);
    mHandler.postDelayed(this::start, 3000);
  }


  private void start(){
    if (KeycloakClient.accessTokenHolder().hasToken()){
      MainActivity.start(this);
    }else{
      LoginActivity.start(this);
    }
  }
}