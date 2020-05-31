package com.example.wi_ficollector.thread;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import com.example.wi_ficollector.wrapper.WifiLocation;

import java.util.List;

import static com.example.wi_ficollector.utils.Constants.countDownLatch;
import static com.example.wi_ficollector.utils.Constants.isAlreadyScanned;

public class WifiReceiverThread implements Runnable {

    private Intent mIntent;
    private Context mContext;

    public WifiReceiverThread(Context mContext, Intent mIntent) {
        this.mContext = mContext;
        this.mIntent = mIntent;
    }

    @Override
    public void run() {
        WifiManager wifiManager = (WifiManager) this.mContext.getSystemService(Context.WIFI_SERVICE);
        boolean hasSuccess = mIntent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);

        if (!isAlreadyScanned && hasSuccess) {
            List<ScanResult> results = wifiManager.getScanResults();

            if (results != null && results.size() > 0) {
                WifiLocation.setScanResults(results);
            }
        }
        isAlreadyScanned = true;
        if (countDownLatch != null) {
            countDownLatch.countDown();
        }
    }
}