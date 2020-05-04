package com.element.element_web_ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.element.element_web_ui_lib.WebAppInterface;

public class WebViewActivity extends Activity implements WebAppInterface.Callback {
    private static final String KEY_FLOW = "KEY_FLOW";
    private static final String KEY_USER_ID = "KEY_USER_ID";
    private static final String KEY_FIRST_NAME = "KEY_FIRST_NAME";
    private static final String KEY_LAST_NAME = "KEY_LAST_NAME";
    private WebView webView;
    private boolean enroll;
    private String userId;
    private String firstName;
    private String lastName;

    public static void start(@NonNull Activity activity, int requestCode, boolean enroll, @NonNull String userId, @Nullable String firstName, @Nullable String lastName) {
        Intent intent = new Intent(activity, WebViewActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(KEY_FLOW, enroll);
        intent.putExtra(KEY_USER_ID, userId);
        intent.putExtra(KEY_FIRST_NAME, firstName);
        intent.putExtra(KEY_LAST_NAME, lastName);
        activity.startActivityForResult(intent, requestCode);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WebView.setWebContentsDebuggingEnabled(true);
        setContentView(R.layout.webview);

        enroll = getIntent().getBooleanExtra(KEY_FLOW, false);
        userId = getIntent().getStringExtra(KEY_USER_ID);
        firstName = getIntent().getStringExtra(KEY_FIRST_NAME);
        lastName = getIntent().getStringExtra(KEY_LAST_NAME);

        webView = findViewById(R.id.webView1);
        webView.clearCache(true);
        webView.clearHistory();
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

        webView.addJavascriptInterface(new WebAppInterface(this, webView, userId, firstName, lastName,
                ElementApiImplement.getInstance()), "Android");

        // Add this otherwise camera doesn't work well on some phones
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);

        // Using camera in Webview
        webView.setWebChromeClient(new WebChromeClient() {
            // Grant permissions for cam
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    request.grant(request.getResources());
                }
            }
        });

        webView.loadUrl("file:///android_asset/page/liveness_check.html?flow=" + (enroll ? "enroll" : "verify"));
    }

    @Override
    protected void onDestroy() {
        webView.destroy();
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    private void showMessage(@NonNull String title, @Nullable String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setPositiveButton("Go Back", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });

        builder.setMessage(msg);
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    @Override
    public void onSucceed(@NonNull String msg) {
        if (enroll) {
            setResult(MainActivity.RESULT_CODE_ENROLL_SUCCESS);
            showMessage("Enrollment Succeeded", msg);
        } else {
            showMessage("Authentication Succeeded", msg);
        }
    }

    @Override
    public void onFail(@NonNull String msg) {
        if (enroll) {
            showMessage("Enrollment Failed", msg);
        } else {
            showMessage("Authentication Failed", msg);
        }
    }

    @Override
    public void onNetworkError() {
        showMessage("Network Error", null);
    }
}
