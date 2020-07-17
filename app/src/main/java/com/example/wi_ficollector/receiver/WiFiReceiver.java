package com.example.wi_ficollector.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;


import com.example.wi_ficollector.repository.WifiLocationRepository;
import com.example.wi_ficollector.wrapper.WifiLocation;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class WiFiReceiver extends BroadcastReceiver {

    private WifiLocation mWifiLocation;
    private WifiLocationRepository mWifiLocationRepository;

    public WiFiReceiver(WifiLocationRepository mWifiLocationRepository, WifiLocation mWifiLocation) {
        this.mWifiLocationRepository = mWifiLocationRepository;
        this.mWifiLocation = mWifiLocation;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        boolean hasSuccess = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
        if (wifiManager != null && hasSuccess) {
            List<ScanResult> scanResults = wifiManager.getScanResults();
            setScanResults(scanResults);
        }
    }

    private void setScanResults(List<ScanResult> scanResults) {
        if (scanResults != null && !scanResults.isEmpty() && shouldSaveScanResults()) {
            mWifiLocation.setScanResults(scanResults);
            Log.d("Number of wifi", String.valueOf(scanResults.size()));
            if (mWifiLocationRepository != null) {
                mWifiLocationRepository.save(mWifiLocation);
            }
        }
    }


    private boolean shouldSaveScanResults() {
        LocalTime foundNetworksTime = LocalTime.now();
        LocalTime savedLocationTime = mWifiLocation.getLocalTime();
        Log.d("Found wifi", " -  " + String.valueOf(foundNetworksTime) + "");

        long difference = -1L;

        if (savedLocationTime != null) {
            difference = ChronoUnit.SECONDS.between(savedLocationTime, foundNetworksTime);
        }

        Log.d("Time difference", String.valueOf(difference));
        return (difference <= 5L && difference != -1L);
    }
}