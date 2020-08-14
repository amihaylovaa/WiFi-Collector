package com.example.wi_ficollector.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.example.wi_ficollector.R;
import com.example.wi_ficollector.application.WifiCollectorApplication;
import com.example.wi_ficollector.dialogfragment.IntroDialogFragment;
import com.example.wi_ficollector.http.HttpRequest;
import com.example.wi_ficollector.repository.WifiLocationInput;

import org.json.JSONArray;

import java.io.IOException;

import static com.example.wi_ficollector.utility.Constants.INTRO_DIALOG_TAG;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, IntroDialogFragment.IntroDialogFragmentListener {

    private IntroDialogFragment mIntroDialogFragment;
    private FragmentManager mFragmentManager;
    private WifiLocationInput mWifiLocationInput;
    private Button sendDataBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button scanningBtn = findViewById(R.id.scanning_button);
        sendDataBtn = findViewById(R.id.sending_button);
        mWifiLocationInput = new WifiLocationInput(MainActivity.this);
        mFragmentManager = getSupportFragmentManager();

        if (((WifiCollectorApplication) getApplication()).isAppFirstTimeLaunched()) {
            showIntroDialog();
            ((WifiCollectorApplication) getApplication()).addIntroKey();
        }

        scanningBtn.setOnClickListener(MainActivity.this);
        sendDataBtn.setOnClickListener(MainActivity.this);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mIntroDialogFragment = (IntroDialogFragment) mFragmentManager.getFragment(savedInstanceState, INTRO_DIALOG_TAG);
    }

    @Override
    public void onClick(View v) {
        int buttonId = v.getId();

        if (buttonId == R.id.scanning_button) {
            startScanningActivity();
        } else {
            try {
                if (mWifiLocationInput.isFileEmpty()) {
                    sendDataBtn.setEnabled(false);
                } else {
                    sendDataBtn.setEnabled(true);
                    sendCollectedData();
                }
            } catch (IOException e) {
                ;
            }
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

    public void startScanningActivity() {
        Intent intent = new Intent(MainActivity.this, ScanActivity.class);
        startActivity(intent);
    }

    private void sendCollectedData() {
        mWifiLocationInput.read();

        JSONArray wifiLocations = mWifiLocationInput.getWifiLocations();
        HttpRequest httpRequest = new HttpRequest();

        httpRequest.send(wifiLocations, MainActivity.this);
    }

    public void showIntroDialog() {
        if (mIntroDialogFragment == null) {
            mIntroDialogFragment = IntroDialogFragment.newInstance();

            mIntroDialogFragment.setCancelable(false);
            mIntroDialogFragment.show(mFragmentManager, INTRO_DIALOG_TAG);
        }
    }
}