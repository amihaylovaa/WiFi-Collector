package com.example.wi_ficollector.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import com.example.wi_ficollector.repository.WifiLocationOutput;
import com.example.wi_ficollector.wrapper.WifiLocation;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class WiFiReceiver extends BroadcastReceiver {

    private WifiLocation mWifiLocation;
    private WifiLocationOutput mWifiLocationOutput;

    public WiFiReceiver(WifiLocationOutput mWifiLocationOutput, WifiLocation mWifiLocation) {
        this.mWifiLocationOutput = mWifiLocationOutput;
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
        if (scanResults != null && shouldSaveScanResults()) {
            mWifiLocationOutput.write(mWifiLocation, scanResults);
        }
    }

    private boolean shouldSaveScanResults() {
        LocalDateTime foundNetworksTime = LocalDateTime.now();
        LocalDateTime savedLocationTime = mWifiLocation.getLocalDateTime();
        long difference = -1L;

        if (savedLocationTime != null) {
            difference = ChronoUnit.SECONDS.between(savedLocationTime, foundNetworksTime);
        }

        return (difference <= 5L && difference != -1L);
    }
}