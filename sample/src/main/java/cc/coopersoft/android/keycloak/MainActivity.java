package cc.coopersoft.android.keycloak;

import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.databinding.DataBindingUtil;
import cc.coopersoft.accesstoken.api.callback.CodeRequestCallback;
import cc.coopersoft.accesstoken.api.callback.ErrorResult;
import cc.coopersoft.accesstoken.api.callback.VerificationCallback;
import cc.coopersoft.android.keycloak.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

  private ActivityMainBinding binding;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = DataBindingUtil.setContentView(this,R.layout.activity_main);
    initView();
  }

  private void toast(String text) {
    Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
    toast.setGravity(Gravity.TOP, 0, 0);
    toast.show();
  }

  private void initView() {

    binding.phoneNumber.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        binding.requestAuthCode.setEnabled(charSequence.toString().length() == 11);
      }

      @Override
      public void afterTextChanged(Editable editable) {

      }
    });

    // send  verification code
    binding.requestAuthCode.setOnClickListener(view -> {
      binding.authCode.setFocusable(true);
      binding.requestAuthCode.setEnabled(false);
      KeycloakClient.accessTokenHolder().sendVerificationCode(binding.phoneNumber.getText().toString(), new CodeRequestCallback() {
        @Override
        public void onFailure(String phoneNumber, ErrorResult error) {
          binding.requestAuthCode.setEnabled(true);
          runOnUiThread(()-> toast("send code fail!"));
        }

        @Override
        public void onSuccess(String phoneNumber, Result result) {
          Looper.prepare();
          new CountDownTimer(result.getExpiresIn() * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
              binding.requestAuthCode.setEnabled(false);
              binding.requestAuthCode.setText("send(" + millisUntilFinished / 1000 + ")");

            }

            @Override
            public void onFinish() {
              binding.requestAuthCode.setEnabled(true);
              binding.requestAuthCode.setText("resend");

            }
          }.start();
          Looper.loop();
        }
      });
    });

    // verification action
    binding.verification.setOnClickListener(view -> {
      Runnable verificationTask = () -> KeycloakClient.accessTokenHolder().verificationCode(binding.phoneNumber.getText().toString(), binding.authCode.getText().toString(), new VerificationCallback() {
        @Override
        public void onError(String phoneNumber, ErrorResult error) {
          runOnUiThread(()-> toast("valid fail!" + error.getErrorDescription()));
        }

        @Override
        public void onFailure(String phoneNumber) {
          runOnUiThread(()-> toast("valid fail!"));
        }

        @Override
        public void onSuccess(String phoneNumber) {
          runOnUiThread(()-> toast("valid success"));
        }
      });

      new Thread(verificationTask).start();


    });


    //logout action
    binding.logout.setOnClickListener(view -> {
      KeycloakClient.accessTokenHolder().clearToken();
      LoginActivity.start(this);
    });

  }

  public static void start(Context context) {
    context.startActivity(new Intent(context, MainActivity.class));
  }


}