package com.example.wi_ficollector.application;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class WifiCollectorApplication extends Application {

    private static final String NAME;
    private static final String KEY;
    private SharedPreferences mSharedPreference;

    static {
        NAME = "First time launching";
        KEY = "is intro dialog shown";
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mSharedPreference = getApplicationContext().getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    public boolean isAppFirstTimeLaunched() {
        return !mSharedPreference.contains(KEY);
    }

    public void addKeyForShownIntroDialog() {
        SharedPreferences.Editor editor = mSharedPreference.edit();

        editor.putBoolean(KEY, true);
        editor.apply();
    }
}