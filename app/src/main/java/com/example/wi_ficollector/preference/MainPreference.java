package com.example.wi_ficollector.preference;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.wi_ficollector.R;

public class MainPreference {

    private SharedPreferences mSharedPreference;

    public MainPreference(Context context) {
        mSharedPreference = context.getSharedPreferences(String.valueOf(R.string.launching_key), Context.MODE_PRIVATE);
    }

    public boolean isActivityFirstTimeLaunched() {
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