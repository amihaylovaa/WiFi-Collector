package com.thesis.wificollector.activity;

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

import com.thesis.wificollector.listener.IntroDialogFragmentListener;
import com.thesis.wificollector.R;
import com.thesis.wificollector.application.WifiCollectorApplication;
import com.thesis.wificollector.dialogfragment.IntroDialogFragment;
import com.thesis.wificollector.http.HttpRequest;
import com.thesis.wificollector.repository.WifiLocationInput;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;

import java.nio.channels.FileChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.thesis.wificollector.utility.Constants.FILE_NAME;
import static com.thesis.wificollector.utility.Constants.FILE_NOT_FOUND_EXCEPTION_MSG;
import static com.thesis.wificollector.utility.Constants.FILE_NOT_FOUND_EXCEPTION_TAG;
import static com.thesis.wificollector.utility.Constants.LONG_ZERO;
import static com.thesis.wificollector.utility.Constants.MINIMUM_FILE_SIZE;
import static com.thesis.wificollector.utility.Constants.NEGATIVE_INTEGER;
import static com.thesis.wificollector.utility.Constants.ZERO_INTEGER;
import static com.thesis.wificollector.utility.Constants.JSON_EXCEPTION_MESSAGE;
import static com.thesis.wificollector.utility.Constants.JSON_EXCEPTION_TAG;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, IntroDialogFragmentListener {

    private IntroDialogFragment mIntroDialogFragment;
    private FragmentManager mFragmentManager;
    private ExecutorService mExecutorService;
    private WifiManager mWifiManager;
    private Handler mHandler;
    private static final String INTRO_DIALOG_TAG;

    static {
        INTRO_DIALOG_TAG = "Intro dialog";
    }

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

        scanningBtn.setOnClickListener(this);
        sendDataBtn.setOnClickListener(this);

        if (savedInstanceState != null) {
            restorePreviousInstanceState(savedInstanceState);
        }

        if (((WifiCollectorApplication) getApplication()).isAppFirstTimeLaunched()) {
            showIntroDialog();
        }
    }

    private void restorePreviousInstanceState(Bundle savedInstanceState) {
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

        if (buttonId == R.id.sending_button) {
            sendLocallyStoredData();
        } else {
            long fileSize = getFileSize();

            if (fileSize == LONG_ZERO) {
                startScanning();
            } else {
                if (fileSize > MINIMUM_FILE_SIZE) {
                    showToastMessage(R.string.send_data_before_scanning_again);
                } else {
                    // clear invalid data due to scanning activity finishing execution too fast
                    WifiLocationInput wifiLocationInput = new WifiLocationInput(MainActivity.this);
                    wifiLocationInput.deleteLocallyStoredData();
                    wifiLocationInput.closeFileInputStream();
                    startScanning();
                }
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
    public void accept() {
        ((WifiCollectorApplication) getApplication()).addKeyForShownIntroDialog();
    }

    public void startScanning() {
        Intent intent = new Intent(MainActivity.this, ScanActivity.class);

        startActivity(intent);
    }

    public void sendLocallyStoredData() {
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

                    handleRequestResult(responseCode, wifiLocationInput);
                }
            }
        });
    }

    public void handleRequestResult(int responseCode, WifiLocationInput wifiLocationInput) {
        switch (responseCode) {
            case HttpURLConnection.HTTP_OK:
                showToastMessage(R.string.send_data_success);
                wifiLocationInput.closeFileInputStream();
                wifiLocationInput.deleteLocallyStoredData();
                break;
            case HttpURLConnection.HTTP_CLIENT_TIMEOUT:
                showToastMessage(R.string.send_request_timeout);
                wifiLocationInput.closeFileInputStream();
                break;
            case HttpURLConnection.HTTP_INTERNAL_ERROR:
                showToastMessage(R.string.internal_server_error);
                wifiLocationInput.closeFileInputStream();
                break;
            case ZERO_INTEGER:
                showToastMessage(R.string.data_send_waiting_for_response);
                wifiLocationInput.closeFileInputStream();
                wifiLocationInput.deleteLocallyStoredData();
                break;
            case NEGATIVE_INTEGER:
                showToastMessage(R.string.lost_internet_connection);
                wifiLocationInput.closeFileInputStream();
                break;
            default:
                wifiLocationInput.closeFileInputStream();
                break;
        }
    }

    public void showToastMessage(int message) {
        mHandler.post(() -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show());
    }

    public void showIntroDialog() {
        if (mIntroDialogFragment == null) {
            mIntroDialogFragment = IntroDialogFragment.newInstance();

            mIntroDialogFragment.setCancelable(false);
            mIntroDialogFragment.show(mFragmentManager, INTRO_DIALOG_TAG);
        }
    }

    public long getFileSize() {
        FileInputStream fileInputStream;
        long fileSize;

        try {
            fileInputStream = MainActivity.this.openFileInput(FILE_NAME);
        } catch (FileNotFoundException e) {
            Log.d(FILE_NOT_FOUND_EXCEPTION_TAG, FILE_NOT_FOUND_EXCEPTION_MSG);
            return LONG_ZERO;
        }
        FileChannel channel = fileInputStream.getChannel();

        try {
            fileSize = channel.size();
        } catch (IOException e) {
            return LONG_ZERO;
        }

        return fileSize;
    }
}