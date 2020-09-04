package com.example.wi_ficollector.activity;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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
import org.json.JSONException;

import java.net.HttpURLConnection;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.example.wi_ficollector.utility.Constants.ZERO_INTEGER;
import static com.example.wi_ficollector.utility.Constants.INTRO_DIALOG_TAG;
import static com.example.wi_ficollector.utility.Constants.JSON_EXCEPTION_MESSAGE;
import static com.example.wi_ficollector.utility.Constants.JSON_EXCEPTION_TAG;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, IntroDialogFragmentListener {

    private IntroDialogFragment mIntroDialogFragment;
    private FragmentManager mFragmentManager;
    private ExecutorService mExecutorService;
    private WifiManager mWifiManager;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFragmentManager = getSupportFragmentManager();
        mWifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        mHandler = new Handler(Looper.getMainLooper());
        mExecutorService = Executors.newSingleThreadExecutor();
        Button scanningBtn = findViewById(R.id.scanning_button);
        Button sendDataBtn = findViewById(R.id.sending_button);

        scanningBtn.setOnClickListener(MainActivity.this);
        sendDataBtn.setOnClickListener(MainActivity.this);

        if (((WifiCollectorApplication) getApplication()).isAppFirstTimeLaunched()) {
            showIntroDialog();
            ((WifiCollectorApplication) getApplication()).addKeyForShownIntroDialog();
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mIntroDialogFragment = (IntroDialogFragment) mFragmentManager.getFragment(savedInstanceState, INTRO_DIALOG_TAG);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mExecutorService.shutdown();
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
        mExecutorService.execute(() -> {

            WifiLocationInput wifiLocationInput = new WifiLocationInput(MainActivity.this);
            JSONArray wifiLocations;

            try {
                wifiLocations = wifiLocationInput.read();
            } catch (JSONException e) {
                Log.d(JSON_EXCEPTION_TAG, JSON_EXCEPTION_MESSAGE);
                wifiLocations = new JSONArray();
            }

            if (!mWifiManager.isWifiEnabled()) {
                showToastMessage(R.string.internet_connection_disabled);
            } else {
                if (wifiLocations.length() == ZERO_INTEGER) {
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
                wifiLocationInput.closeFileInputStream();
                wifiLocationInput.deleteLocalStoredData();
                break;
            case HttpURLConnection.HTTP_CLIENT_TIMEOUT:
                showToastMessage(R.string.send_request_timeout);
                wifiLocationInput.closeFileInputStream();
                break;
            case HttpURLConnection.HTTP_NOT_FOUND:
                wifiLocationInput.closeFileInputStream();
                break;
            case HttpURLConnection.HTTP_INTERNAL_ERROR:
                wifiLocationInput.closeFileInputStream();
                showToastMessage(R.string.internal_server_error);
                break;
            case ZERO_INTEGER:
                showToastMessage(R.string.data_send_waiting_for_response);
                wifiLocationInput.closeFileInputStream();
                wifiLocationInput.deleteLocalStoredData();
                break;
            default:
                showToastMessage(R.string.lost_internet_connection);
                wifiLocationInput.closeFileInputStream();
                break;
        }
    }

    private void showToastMessage(int message) {
        mHandler.post(() -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show());
    }

    private void showIntroDialog() {
        if (mIntroDialogFragment == null) {
            mIntroDialogFragment = IntroDialogFragment.newInstance();

            mIntroDialogFragment.setCancelable(false);
            mIntroDialogFragment.show(mFragmentManager, INTRO_DIALOG_TAG);
        }
    }
}