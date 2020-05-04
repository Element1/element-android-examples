package com.element.element_web_ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.element.element_web_ui_lib.CheckUserExistTask;

public class MainActivity extends Activity implements CheckUserExistTask.Callback {
    private static final String KEY_USER_ID = "USER_ID";
    private static final String SHARED_PREF_NAME = "cookie";
    private static final int REQUEST_CODE = 333;
    public static final int RESULT_CODE_ENROLL_SUCCESS = 10001;

    private Button button;
    private EditText userIdText;
    private EditText firstNameText;
    private EditText lastNameText;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        button = findViewById(R.id.buttonUrl);
        userIdText = findViewById(R.id.userId);
        button = findViewById(R.id.buttonUrl);
        firstNameText = findViewById(R.id.firstName);
        lastNameText = findViewById(R.id.lastName);
        userIdText = findViewById(R.id.userId);

        // Store the userId
        SharedPreferences sharedPref = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        final String userId = sharedPref.getString(KEY_USER_ID, "");
        userIdText.setText(userId);

        showUI(false);

        if (!checkPermission()) {
            requestPermission();
        }
    }

    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 777);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    showMessageOKCancel("You need to allow access permissions",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    requestPermission();
                                }
                            });
                }
            }
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, okListener)
                .setNegativeButton(android.R.string.cancel, null)
                .create()
                .show();
    }

    private void showAlert(@NonNull String msg) {
        new AlertDialog.Builder(this)
                .setMessage(msg)
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showUI(boolean doEnroll) {
        if (doEnroll) {
            button.setText(getText(R.string.buttonTextEnroll));
            firstNameText.setVisibility(View.VISIBLE);
            lastNameText.setVisibility(View.VISIBLE);
            userIdText.setEnabled(false);
            button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    String firstName = firstNameText.getText().toString().trim();
                    String lastName = lastNameText.getText().toString().trim();
                    String userId = userIdText.getText().toString().trim();
                    if (firstName.isEmpty()) {
                        showAlert("Please input first name");
                        return;
                    }
                    if (lastName.isEmpty()) {
                        showAlert("Please input last name");
                        return;
                    }
                    WebViewActivity.start(MainActivity.this, REQUEST_CODE, true, userId, firstName, lastName);
                }
            });
        } else {
            button.setText(getText(R.string.buttonTextVerify));
            firstNameText.setVisibility(View.GONE);
            lastNameText.setVisibility(View.GONE);
            userIdText.setEnabled(true);
            button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    String userId = userIdText.getText().toString().trim();
                    if(userId.isEmpty()) {
                        showAlert("Please input userId");
                    } else {
                        new CheckUserExistTask(MainActivity.this, MainActivity.this,
                                ElementApiImplement.getInstance()).execute(userId);
                    }
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_CODE_ENROLL_SUCCESS) {
           showUI(false);
        }
    }

    @Override
    public void onNetworkError() {
        showAlert("Network error");
    }

    @Override
    public void onSucceed(boolean userExists, @NonNull final String userId) {
        if (userExists) {
            SharedPreferences sharedPref = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(KEY_USER_ID, userId);
            editor.apply();
            WebViewActivity.start(this, REQUEST_CODE, false, userId, null, null);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("User Does Not Exist")
                    .setPositiveButton("Enroll", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences sharedPref = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
                            sharedPref.edit()
                                    .putString(KEY_USER_ID, userId)
                                    .apply();
                            showUI(true);
                        }
                    });

            builder.setMessage(String.format("Do you want to enroll a new user with userId %s?", userId));
            AlertDialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setCancelable(false);
            alertDialog.show();
        }
    }

    @Override
    public void onFail(String msg) {
        showAlert(msg);
    }
}