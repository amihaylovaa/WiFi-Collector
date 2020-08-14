package com.example.wi_ficollector.application;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.example.wi_ficollector.R;

public class WifiCollectorApplication extends Application {

    private SharedPreferences mSharedPreference;

    @Override
    public void onCreate() {
        super.onCreate();

        Context context = this.getApplicationContext();
        String key = String.valueOf(R.string.launching_key);
        mSharedPreference = context.getSharedPreferences(key, Context.MODE_PRIVATE);
    }

    public boolean isAppFirstTimeLaunched() {
        String showDialogIntro = String.valueOf(R.string.launching_key);

        return !mSharedPreference.contains(showDialogIntro);
    }

    public void addIntroKey() {
        SharedPreferences.Editor editor = mSharedPreference.edit();
        String launchingKey = String.valueOf(R.string.launching_key);

        editor.putBoolean(launchingKey, true);
        editor.apply();
    }
}
