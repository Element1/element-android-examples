package com.element.element_web_ui;

import android.app.Application;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ElementApiImplement.init(
                BuildConfig.ELEMENT_CLIENT_ID,
                BuildConfig.ELEMENT_API_KEY,
                BuildConfig.ELEMENT_SERVER_URL);
    }
}
