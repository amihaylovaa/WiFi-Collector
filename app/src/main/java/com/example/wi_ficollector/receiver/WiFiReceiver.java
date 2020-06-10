package com.example.wi_ficollector.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.example.wi_ficollector.repository.WifiLocationRepository;
import com.example.wi_ficollector.wrapper.WifiLocation;

import java.io.IOException;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;


import static com.example.wi_ficollector.utils.Constants.IO_EXCEPTION_THROWN_MESSAGE;
import static com.example.wi_ficollector.utils.Constants.IO_EXCEPTION_THROWN_TAG;
import static com.example.wi_ficollector.utils.Constants.numberFoundWifiNetworks;

public class WiFiReceiver extends BroadcastReceiver {

    private WifiLocation mWifiLocation;
    private WifiLocationRepository mWifiLocationRepository;
    private Context mContext;

    public WiFiReceiver(WifiLocationRepository mWifiLocationRepository, WifiLocation mWifiLocation) {
        this.mWifiLocationRepository = mWifiLocationRepository;
        this.mWifiLocation = mWifiLocation;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        boolean hasSuccess = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);

        if (wifiManager != null && hasSuccess) {
            List<ScanResult> scanResults = wifiManager.getScanResults();
            setScanResults(scanResults);
        }
    }

    private void setScanResults(List<ScanResult> scanResults) {
        if (scanResults != null && scanResults.size() > 0) {
            mWifiLocation.setScanResults(scanResults);
            if (shouldSaveScanResults()) {
                saveScanResults(scanResults);
            }
        }
    }

    private boolean shouldSaveScanResults() {
        LocalTime foundNetworksTime = LocalTime.now();
        LocalTime savedLocationTime = mWifiLocation.getLocalTime();
        long difference = 0L;

        // location null
        if (savedLocationTime != null) {
            difference = ChronoUnit.SECONDS.between(savedLocationTime, foundNetworksTime);
        }

        return difference <= 3L;
    }

    private void saveScanResults(List<ScanResult> scanResults) {
        try {
            numberFoundWifiNetworks += scanResults.size();
            mWifiLocationRepository.saveWifiLocation();
        } catch (IOException e) {
            Log.d(IO_EXCEPTION_THROWN_TAG, IO_EXCEPTION_THROWN_MESSAGE);
        }
    }
}