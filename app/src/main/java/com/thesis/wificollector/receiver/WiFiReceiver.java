package com.thesis.wificollector.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import com.thesis.wificollector.repository.WifiLocationOutput;
import com.thesis.wificollector.wrapper.WifiLocation;

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

        if (hasSuccess) {
            List<ScanResult> scanResults = wifiManager.getScanResults();

            if (shouldSaveScanResults()) {
                mWifiLocation.setScanResults(scanResults);
                mWifiLocationOutput.write(mWifiLocation);
            }
        }
    }

    private boolean shouldSaveScanResults() {
        LocalDateTime networksTime = LocalDateTime.now();
        LocalDateTime locationTime = mWifiLocation.getLocalDateTime();
        long timeDifference = -1L;
        long maxTimeDifference = 5L;

        if (locationTime != null) {
            timeDifference = ChronoUnit.SECONDS.between(locationTime, networksTime);
        }

        return (timeDifference != -1L && timeDifference <= maxTimeDifference);
    }
}