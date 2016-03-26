package com.sony.login;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sony.util.Constants;

import org.apache.http.Header;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import butterknife.ButterKnife;
import butterknife.Bind;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    public static final String MyPREFERENCES = "MyPrefs" ;
    SharedPreferences sharedpreferences;

    @Bind(R.id.input_username) EditText _userNameText;
    @Bind(R.id.input_password) EditText _passwordText;
    @Bind(R.id.btn_login) Button _loginButton;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        Constants.IS_AIS_AUTHENTICATED_VALUE=sharedpreferences.getString(Constants.IS_AUTHENTICATED, "");
        if(Constants.IS_AIS_AUTHENTICATED_VALUE.equals("true")){
            onLoginSuccess();
        }
        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

    }

    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }

        _loginButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        //commented to reduce variables
        //String userName = _userNameText.getText().toString();
        //String password = _passwordText.getText().toString();
        //String encodedUserName= null;
        //String encodedPassword=null;

        //  Authentication logic here.
        String url=null;
        RequestParams params = new RequestParams();
        try {
            url = "http://192.168.1.111:8080/RESTLogin/rest/login/dologin";
            params.add("p1",URLEncoder.encode(Base64.encodeToString(_userNameText.getText().toString().getBytes("UTF-8"), Base64.DEFAULT), "UTF-8"));
            params.add("p2", URLEncoder.encode(Base64.encodeToString(_passwordText.getText().toString().getBytes("UTF-8"), Base64.DEFAULT), "UTF-8"));

            Log.d(TAG, "url : " + url);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url,params, new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                // Hide Progress Dialog
                onLoginSuccess();
                progressDialog.dismiss();
            }

            // When the response returned by REST has Http response code other than '200'
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                // Hide Progress Dialog
                Log.d(TAG,""+statusCode);
                Log.d(TAG, error.getMessage());
                onLoginFailed();
                progressDialog.dismiss();
            }

        });

/*        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onLoginSuccess or onLoginFailed
                        onLoginSuccess();
                        // onLoginFailed();
                        progressDialog.dismiss();
                    }
                }, 3000);*/
    }



    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        _loginButton.setEnabled(true);
        Constants.IS_AIS_AUTHENTICATED_VALUE="true";
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(Constants.IS_AUTHENTICATED,Constants.IS_AIS_AUTHENTICATED_VALUE);
        editor.commit();
        finish();
    }

    public void onLogout() {
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        Constants.IS_AIS_AUTHENTICATED_VALUE="false";
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(Constants.IS_AUTHENTICATED, Constants.IS_AIS_AUTHENTICATED_VALUE);
        editor.commit();
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        _loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String userName = _userNameText.getText().toString();
        String password = _passwordText.getText().toString();

        //userName.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(userName).matches(
        if (userName.isEmpty()) {
            _userNameText.setError("Username cannot be empty!");
            valid = false;
        } else {
            _userNameText.setError(null);
        }

        if (password.isEmpty()) {
            _passwordText.setError("Password cannot be empty!");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }
}
