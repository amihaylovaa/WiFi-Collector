package com.example.wi_ficollector.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.example.wi_ficollector.R;
import com.example.wi_ficollector.dialogfragment.IntroDialogFragment;
import com.example.wi_ficollector.http.HttpRequest;
import com.example.wi_ficollector.preference.MainPreference;
import com.example.wi_ficollector.repository.WifiLocationInput;

import org.json.JSONArray;
import org.json.JSONException;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import static com.example.wi_ficollector.utility.Constants.INTRO_DIALOG_TAG;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, IntroDialogFragment.IntroDialogFragmentListener {

    private IntroDialogFragment mIntroDialogFragment;
    private FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFragmentManager = getSupportFragmentManager();
        MainPreference mainPreference = new MainPreference(this);
        Button startScanningBtn = findViewById(R.id.start_scanning_button);
        Button sendDataBtn = findViewById(R.id.send_data_button);

        if (savedInstanceState != null) {
            restorePreviousState(savedInstanceState);
        }

        if (mainPreference.isActivityFirstTimeLaunched()) {
            showIntroDialog(mainPreference);
        }

        startScanningBtn.setOnClickListener(this);
        sendDataBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int buttonId = v.getId();

        if (buttonId == R.id.start_scanning_button) {
            Intent intent = new Intent(this, ScanActivity.class);
            startActivity(intent);
        } else {
            WifiLocationInput wifiLocationInput = new WifiLocationInput(this);

            try {
                wifiLocationInput.read();
            } catch (XmlPullParserException | IOException | JSONException e) {
                // TODO add handling
            }
            JSONArray wifiLocations = wifiLocationInput.getJsonArray();
            HttpRequest httpRequest = new HttpRequest();

            httpRequest.send(wifiLocations, this);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mIntroDialogFragment != null && mIntroDialogFragment.isAdded()) {
            mFragmentManager.putFragment(outState, INTRO_DIALOG_TAG, mIntroDialogFragment);
        }
    }

    @Override
    public void ok() {
        // This method is called when intro dialog is shown explaining how an app is supposed to work
    }

    public void showIntroDialog(MainPreference mainPreference) {
        if (mIntroDialogFragment == null) {
            mIntroDialogFragment = IntroDialogFragment.newInstance();

            mIntroDialogFragment.setCancelable(false);
            mIntroDialogFragment.show(mFragmentManager, INTRO_DIALOG_TAG);
            mainPreference.addIntroKey();
        }
    }

    private void restorePreviousState(Bundle savedInstanceState) {
        mIntroDialogFragment = (IntroDialogFragment) mFragmentManager
                .getFragment(savedInstanceState, INTRO_DIALOG_TAG);
    }
}