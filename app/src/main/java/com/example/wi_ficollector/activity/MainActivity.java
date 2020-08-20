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

import java.net.HttpURLConnection;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.example.wi_ficollector.utility.Constants.INTRO_DIALOG_TAG;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, IntroDialogFragmentListener {

    private IntroDialogFragment mIntroDialogFragment;
    private FragmentManager mFragmentManager;
    private Executor mExecutor;
    private WifiManager mWifiManager;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFragmentManager = getSupportFragmentManager();
        mWifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        mHandler = new Handler(Looper.getMainLooper());
        mExecutor = Executors.newSingleThreadExecutor();
        Button scanningBtn = findViewById(R.id.scanning_button);
        Button sendDataBtn = findViewById(R.id.sending_button);

        scanningBtn.setOnClickListener(MainActivity.this);
        sendDataBtn.setOnClickListener(MainActivity.this);

        if (((WifiCollectorApplication) getApplication()).isAppFirstTimeLaunched()) {
            showIntroDialog();
            ((WifiCollectorApplication) getApplication()).addIntroKey();
        }
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
    public void accept() {
        // This method is called when intro dialog is shown explaining how an app is supposed to work
    }

    private void startScanning() {
        Intent intent = new Intent(MainActivity.this, ScanActivity.class);

        startActivity(intent);
    }

    private void sendLocalStoredData() {
        mExecutor.execute(() -> {
            WifiLocationInput wifiLocationInput = new WifiLocationInput(MainActivity.this);
            JSONArray wifiLocations = wifiLocationInput.read();

            if (!mWifiManager.isWifiEnabled()) {
                showToastMessage(R.string.internet_connection_disabled);
            } else {

                if (wifiLocations.length() == 0) {
                    showToastMessage(R.string.no_data_found);
                } else {
                    HttpRequest httpRequest = new HttpRequest();
                    int responseCode = httpRequest.send(wifiLocations);

                    handleRequestResponse(responseCode, wifiLocationInput);
                }
            }
        });
    }

    private void handleRequestResponse(int responseCode, WifiLocationInput wifiLocationInput) {
        switch (responseCode) {
            case HttpURLConnection.HTTP_OK:
                showToastMessage(R.string.send_data_success);
                wifiLocationInput.deleteLocalStoredData();
                wifiLocationInput.closeFileInputStream();
                break;
            case HttpURLConnection.HTTP_UNAVAILABLE:
                showToastMessage(R.string.no_service_available);
                break;
            case HttpURLConnection.HTTP_NOT_FOUND:
                showToastMessage(R.string.not_found_resource);
                break;
            default:
                showToastMessage(R.string.lost_internet_connection);
                break;
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