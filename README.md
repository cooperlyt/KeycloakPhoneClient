# Keycloak phone client

+ login and get token from keycloak
+ add token to http header, access protected resource
+ auto refresh token

if you need phone support for keycloak , try my
project: [keycloak-phone-provider](https://github.com/cooper-lyt/keycloak-phone-provider), this sample is base
on: [keycloak-phone-provider](https://github.com/cooper-lyt/keycloak-phone-provider). this sample is android client,
nothing stop you from implementing other java program.

## Usage

copy keycloak-client to your project

**Initialize:**

```java

accessTokenHolder=new KeycloakTokenHolder.Builder()
        .host("www.XXX.com") // root address: like "www.XXX.com"
        .realms("realms")  // realms: config in your keycloak
        .clientId("clientId") // clientId: config in your keycloak
        .clientSecret("XXXX-XXXX-XXXX-XXXX-XXXXXXXXXX") //clientSecret : clientId: config in your keycloak
        .storage(new SharedPreferencesStorage(getSharedPreferences("keycloak-sample",Context.MODE_PRIVATE),ACCESS_TOKEN_STORE_KEY)) // storage: you can custom for cc.coopersoft.accesstoken.keycloak.KeycloakTokenStorage
        .build();

```

storage: you can custom storage from KeycloakTokenStorage, in my project ,i use tencent mmkv.

**Login:**

1. Required auth code

```java

KeycloakClient.accessTokenHolder().sendAuthenticationCode(binding.phoneNumber.getText().toString(),new CodeRequestCallback(){
@Override
public void onFailure(String phoneNumber,ErrorResult error){
        binding.requestAuthCode.setEnabled(true);
        runOnUiThread(()->toast("send code fail!"));
        }

@Override
public void onSuccess(String phoneNumber,Result result){
        Looper.prepare();
        new CountDownTimer(result.getExpiresIn()*1000,1000){
@Override
public void onTick(long millisUntilFinished){
        binding.requestAuthCode.setEnabled(false);
        binding.requestAuthCode.setText("send("+millisUntilFinished/1000+")");

        }

@Override
public void onFinish(){
        binding.requestAuthCode.setEnabled(true);
        binding.requestAuthCode.setText("resend");

        }
        }.start();
        Looper.loop();
        }
        });

```

2. Required token

```java
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
```


**Access protected resource:**

```java
okHttpClientBuilder.addInterceptor(new AllAccessTokenInterceptor());
```

about Interceptor:
 + AllAccessTokenInterceptor 
 + WhiteAccessTokenInterceptor
 + you can custom Interceptor on AccessTokenInterceptor

in my project, I use Retrofit implement a dynamic Interceptor annotation like this:

```java

  @POST("pyramid/task/receive/{id}")
  @RequireInterceptor({"accessToken"})
  Observable<User> receive(@Path("id") int id);

```

**Token auth refresh:**

1. auto refresh on token is expires.
2. auto refresh on http required first return 401.
3. refreshToke is expires, return 401.

**verification phone number:**

1. Required verification code

```java
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
```

2. valid code

```java
KeycloakClient.accessTokenHolder().verificationCode(binding.phoneNumber.getText().toString(), binding.authCode.getText().toString(), new VerificationCallback() {
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
```

**logout (clear token):**

```java
  KeycloakClient.accessTokenHolder().clearToken();
```

**other:**
 look for PhoneSupportAccessTokenHolder.


