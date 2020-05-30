package com.example;


import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import com.example.wi_ficollector.wrapper.WifiLocation;

import java.util.List;

import static com.example.wi_ficollector.utils.Constants.countDownLatch;
import static com.example.wi_ficollector.utils.Constants.isAlreadyScanned;

public class WifiReceiverThread implements Runnable {

    private WifiManager mWifiManager;
    private Intent mIntent;
    private Context mContext;

    public WifiReceiverThread(Context mContext, Intent mIntent) {
        this.mContext = mContext;
        this.mIntent = mIntent;
    }

    @Override
    public void run() {
        mWifiManager = (WifiManager) this.mContext.getSystemService(Context.WIFI_SERVICE);
        boolean hasSuccess = mIntent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);

        if (!isAlreadyScanned) {
            if (hasSuccess) {
                List<ScanResult> results = mWifiManager.getScanResults();

                if (results.size() > 0) {
                    WifiLocation.setScanResults(results);
                }
            }
        }
        isAlreadyScanned = true;
        if (countDownLatch != null) {
            countDownLatch.countDown();
        }
    }
}