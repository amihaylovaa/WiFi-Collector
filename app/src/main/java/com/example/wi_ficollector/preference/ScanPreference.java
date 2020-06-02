package com.example.wi_ficollector.preference;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.wi_ficollector.R;

public class ScanPreference {

    private SharedPreferences mSharedPreference;

    public ScanPreference(Context context) {
        mSharedPreference = context.getSharedPreferences(String.valueOf(R.string.rationale_dialog_key),Context.MODE_PRIVATE);
    }

    public boolean isActivityFirstTimeLaunched() {
        String backgroundPermissionRationaleKey = String.valueOf(R.string.rationale_dialog_key);

        return !mSharedPreference.contains(backgroundPermissionRationaleKey);
    }

    public void addBackgroundPermissionRationaleKey() {
        String backgroundPermissionRationaleKey = String.valueOf(R.string.rationale_dialog_key);
        SharedPreferences.Editor editor = mSharedPreference.edit();

        editor.putBoolean(backgroundPermissionRationaleKey, true);
        editor.apply();
    }

    public boolean shouldShowBackgroundPermissionRequestRationale() {
        String rationaleDialogKey = String.valueOf(R.string.rationale_dialog_key);

        return mSharedPreference.getBoolean(rationaleDialogKey, false);
    }


    public void stopShowBackgroundPermissionRequestRationale() {
        String rationaleDialogKey = String.valueOf(R.string.rationale_dialog_key);
        SharedPreferences.Editor editor = mSharedPreference.edit();

        editor.putBoolean(rationaleDialogKey, false);
        editor.apply();
    }
}
