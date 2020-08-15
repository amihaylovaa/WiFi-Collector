package com.example.wi_ficollector.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.example.wi_ficollector.R;
import com.example.wi_ficollector.application.WifiCollectorApplication;
import com.example.wi_ficollector.dialogfragment.IntroDialogFragment;
import com.example.wi_ficollector.http.HttpRequest;
import com.example.wi_ficollector.repository.WifiLocationInput;

import org.json.JSONArray;

import static com.example.wi_ficollector.utility.Constants.INTRO_DIALOG_TAG;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, IntroDialogFragment.IntroDialogFragmentListener {

    private IntroDialogFragment mIntroDialogFragment;
    private FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button scanningBtn = findViewById(R.id.scanning_button);
        Button sendDataBtn = findViewById(R.id.sending_button);
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
            startScanning();
        } else {
            sendCollectedData();
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

    private void startScanning() {
        Intent intent = new Intent(MainActivity.this, ScanActivity.class);

        startActivity(intent);
    }

    private void sendCollectedData() {
        WifiLocationInput wifiLocationInput = new WifiLocationInput(MainActivity.this);
        JSONArray wifiLocations = wifiLocationInput.read();

        if (wifiLocations.length() == 0) {
            Toast.makeText(MainActivity.this, " No data found, please scan", Toast.LENGTH_SHORT).show();
        } else {
            HttpRequest httpRequest = new HttpRequest();

            httpRequest.send(wifiLocations, MainActivity.this);
            wifiLocationInput.deleteSendData();
        }
    }

    private void showIntroDialog() {
        if (mIntroDialogFragment == null) {
            mIntroDialogFragment = IntroDialogFragment.newInstance();

            mIntroDialogFragment.setCancelable(false);
            mIntroDialogFragment.show(mFragmentManager, INTRO_DIALOG_TAG);
        }
    }
}