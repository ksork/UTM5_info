package com.example.UTM5_info;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

/**
 * Created by k on 23.07.14.
 */
public class SettingsActivity extends Activity {
    private final String LOG_TAG = "Settings_log";
    private EditText edLogin;
    private EditText edPassword;
    private String userLogin;
    private String userPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);
        edLogin = (EditText) findViewById(R.id.edLogin);
        edPassword = (EditText) findViewById(R.id.edPassword);

        Intent intent = getIntent();
        userLogin = intent.getStringExtra("userLogin");
        userPassword = intent.getStringExtra("userPassword");

        edLogin.setText(userLogin);
        edPassword.setText(userPassword);
    }

    public void onBtnSaveSettingsClick(View view) {
        userLogin = edLogin.getText().toString();
        userPassword = edPassword.getText().toString();
        Intent intent = new Intent();
        intent.putExtra("userLogin", userLogin);
        intent.putExtra("userPassword", userPassword);
        setResult(RESULT_OK, intent);
        finish();
    }
}
