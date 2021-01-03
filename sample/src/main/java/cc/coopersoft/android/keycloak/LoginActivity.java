package cc.coopersoft.android.keycloak;

import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.databinding.DataBindingUtil;
import cc.coopersoft.accesstoken.api.callback.AuthenticationCallback;
import cc.coopersoft.accesstoken.api.callback.CodeRequestCallback;
import cc.coopersoft.accesstoken.api.callback.ErrorResult;
import cc.coopersoft.accesstoken.keycloak.credential.PhoneCredential;
import cc.coopersoft.android.keycloak.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

  private ActivityLoginBinding binding;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
    initView();
  }

  public static void start(Context context) {
    context.startActivity(new Intent(context, LoginActivity.class));
  }

  private void toast(String text) {
    Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
    toast.setGravity(Gravity.CENTER, 0, 0);
    toast.show();
  }

  private void loading(boolean loading){
    runOnUiThread(() -> {
      binding.login.setEnabled(!loading);
      binding.loginProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
      binding.login.setText(loading ? "" : "登录");
    });
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

    binding.requestAuthCode.setOnClickListener(view -> {
      binding.authCode.setFocusable(true);
      binding.requestAuthCode.setEnabled(false);
      KeycloakClient.accessTokenHolder().sendAuthenticationCode(binding.phoneNumber.getText().toString(), new CodeRequestCallback() {
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

    binding.login.setOnClickListener(view -> {
      String phone = binding.phoneNumber.getText().toString().trim();
      String code = binding.authCode.getText().toString().trim();
      if (phone.length() != 11) {
        toast("phone number error");
        return;
      }
      if (code.length() != 6) {
        toast("code error");
        return;
      }

      loading(true);

      KeycloakClient.accessTokenHolder().requireToken(PhoneCredential.builder().phone(phone).code(code).build(), new AuthenticationCallback() {

        @Override
        public void onFailure(ErrorResult error) {
          runOnUiThread(() -> toast("login fail!"));
          loading(false);
        }

        @Override
        public void onSuccess() {
          MainActivity.start(LoginActivity.this);
          finish();
        }
      });
    });
  }
}