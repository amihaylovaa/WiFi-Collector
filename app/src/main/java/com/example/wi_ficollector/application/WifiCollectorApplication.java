package com.example.wi_ficollector.application;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class WifiCollectorApplication extends Application {

    private final String name;
    private final String key;
    private SharedPreferences mSharedPreference;

    {
        name = "First time launching";
        key = "is intro dialog shown";
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mSharedPreference = getApplicationContext().getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public boolean isAppFirstTimeLaunched() {
        return !mSharedPreference.contains(key);
    }

    public void addKeyForShownIntroDialog() {
        SharedPreferences.Editor editor = mSharedPreference.edit();

        editor.putBoolean(key, true);
        editor.apply();
    }
}