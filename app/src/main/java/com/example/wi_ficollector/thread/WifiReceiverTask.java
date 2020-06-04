package com.example.wi_ficollector.thread;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import com.example.wi_ficollector.wrapper.WifiLocation;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import static com.example.wi_ficollector.utils.Constants.numberFoundWifiNetworks;

public class WifiReceiverTask implements Runnable {

    private Intent mIntent;
    private Context mContext;
    private CountDownLatch mCountDownLatch;
    private WifiLocation mWifiLocation;

    public WifiReceiverTask(Intent mIntent, Context mContext, CountDownLatch mCountDownLatch, WifiLocation mWifiLocation) {
        this.mIntent = mIntent;
        this.mContext = mContext;
        this.mCountDownLatch = mCountDownLatch;
        this.mWifiLocation = mWifiLocation;
    }

    @Override
    public void run() {
        WifiManager wifiManager = (WifiManager) this.mContext.getSystemService(Context.WIFI_SERVICE);
        boolean hasSuccess = mIntent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
        if (hasSuccess) {
            List<ScanResult> scanResults = wifiManager.getScanResults();
            setScanResults(scanResults);
            if (mCountDownLatch != null) {
                mCountDownLatch.countDown();
            }
        }
    }

    private void setScanResults(List<ScanResult> scanResults) {
        if (scanResults != null && scanResults.size() > 0) {
            numberFoundWifiNetworks += scanResults.size();
            mWifiLocation.setScanResults(scanResults);
        }
    }
}