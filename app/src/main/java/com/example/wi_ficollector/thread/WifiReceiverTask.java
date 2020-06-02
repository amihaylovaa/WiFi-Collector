package com.example.wi_ficollector.thread;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import com.example.wi_ficollector.wrapper.WifiLocation;

import java.util.List;

import static com.example.wi_ficollector.utils.Constants.countDownLatch;
import static com.example.wi_ficollector.utils.Constants.isAlreadyScanned;
import static com.example.wi_ficollector.utils.Constants.numberFoundWifiNetworks;

public class WifiReceiverTask implements Runnable {

    private Intent mIntent;
    private Context mContext;

    public WifiReceiverTask(Context mContext, Intent mIntent) {
        this.mContext = mContext;
        this.mIntent = mIntent;
    }

    @Override
    public void run() {
        WifiManager wifiManager = (WifiManager) this.mContext.getSystemService(Context.WIFI_SERVICE);
        boolean hasSuccess = mIntent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);

        if (!isAlreadyScanned && hasSuccess) {
            List<ScanResult> scanResults = wifiManager.getScanResults();
            setScanResults(scanResults);
        }
        isAlreadyScanned = true;
        if (countDownLatch != null) {
            countDownLatch.countDown();
        }
    }

    private void setScanResults(List<ScanResult> scanResults) {
        if (scanResults != null && scanResults.size() > 0) {
            numberFoundWifiNetworks += scanResults.size();
            WifiLocation.setScanResults(scanResults);
        }
    }
}