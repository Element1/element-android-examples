package com.element.facex;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.element.camera.ProviderUtil;
import com.element.camera.UserInfo;

import java.util.HashMap;

public class UserDataFormFragment extends DialogFragment {

    private MainActivity mainActivity;

    private Handler handler;

    protected EditText firstName;

    protected EditText lastName;

    protected Button enroll;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, 0);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_user_data_form, container, false);

        firstName = rootView.findViewById(R.id.firstName);
        lastName = rootView.findViewById(R.id.lastName);

        TextView.OnEditorActionListener onEditorActionListener = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                if (view == firstName) {
                    lastName.requestFocus();
                } else if (view == lastName) {
                    onClickEnroll();
                }
                return true;
            }
        };

        firstName.setImeActionLabel(getString(R.string.signUp), EditorInfo.IME_ACTION_DONE);
        firstName.setOnEditorActionListener(onEditorActionListener);
        lastName.setImeActionLabel(getString(R.string.signUp), EditorInfo.IME_ACTION_DONE);
        lastName.setOnEditorActionListener(onEditorActionListener);

        enroll = rootView.findViewById(R.id.enroll);
        enroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public synchronized void onClick(View view) {
                onClickEnroll();
            }
        });

        return rootView;
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.handler = new Handler();
    }

    public synchronized void onClickEnroll() {
        enroll.setEnabled(false);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                enroll.setEnabled(true);
            }
        }, 500);

        String fStr = firstName.getText().toString();
        String lStr = lastName.getText().toString();

        if (TextUtils.isEmpty(fStr)) {
            Toast.makeText(mainActivity, R.string.error_empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        ProviderUtil.deleteAllUsers(mainActivity.getBaseContext(), BuildConfig.APPLICATION_ID);

        UserInfo userInfo = UserInfo.enrollNewUser(
                mainActivity.getBaseContext(),
                mainActivity.getPackageName(),
                fStr,
                lStr,
                new HashMap<String, String>());

        mainActivity.startEnroll(userInfo.userId);
        dismiss();
    }
}