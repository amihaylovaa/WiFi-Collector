package com.example.wi_ficollector.activity;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.example.wi_ficollector.listener.IntroDialogFragmentListener;
import com.example.wi_ficollector.R;
import com.example.wi_ficollector.application.WifiCollectorApplication;
import com.example.wi_ficollector.dialogfragment.IntroDialogFragment;
import com.example.wi_ficollector.http.HttpRequest;
import com.example.wi_ficollector.repository.WifiLocationInput;

import org.json.JSONArray;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.example.wi_ficollector.utility.Constants.INTRO_DIALOG_TAG;
import static com.example.wi_ficollector.utility.Constants.SERVER_ERROR_CODE;
import static com.example.wi_ficollector.utility.Constants.SUCCESS_STATUS_CODE;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, IntroDialogFragmentListener {

    private IntroDialogFragment mIntroDialogFragment;
    private FragmentManager mFragmentManager;
    private WifiManager mWifiManager;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button scanningBtn = findViewById(R.id.scanning_button);
        Button sendDataBtn = findViewById(R.id.sending_button);
        mFragmentManager = getSupportFragmentManager();
        mWifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        mHandler = new Handler(Looper.getMainLooper());

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
            sendLocalStoredData();
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

    private void sendLocalStoredData() {
        Executor executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            WifiLocationInput wifiLocationInput = new WifiLocationInput(MainActivity.this);
            JSONArray wifiLocations = wifiLocationInput.read();

            if (wifiLocations.length() == 0) {
                showToastMessage(R.string.no_data_found);
            } else {
                sendRequest(wifiLocationInput, wifiLocations);
            }
        });
    }

    private void sendRequest(WifiLocationInput wifiLocationInput, JSONArray wifiLocations) {
        if (!mWifiManager.isWifiEnabled()) {
            showToastMessage(R.string.internet_connection_disabled);
        } else {
            HttpRequest httpRequest = new HttpRequest();
            int responseCode = httpRequest.send(wifiLocations);

            if (responseCode == SUCCESS_STATUS_CODE) {
                showToastMessage(R.string.send_data_success);
                wifiLocationInput.deleteLocalStoredData();
            } else if (responseCode == SERVER_ERROR_CODE) {
                showToastMessage(R.string.no_service_available);
            }
            wifiLocationInput.closeFileInputStream();
        }
    }

    private void showToastMessage(int text) {
        mHandler.post(() ->
                Toast.makeText(MainActivity.this, text, Toast.LENGTH_LONG).show());
    }


    private void showIntroDialog() {
        if (mIntroDialogFragment == null) {
            mIntroDialogFragment = IntroDialogFragment.newInstance();

            mIntroDialogFragment.setCancelable(false);
            mIntroDialogFragment.show(mFragmentManager, INTRO_DIALOG_TAG);
        }
    }
}