package com.example.wi_ficollector.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.wi_ficollector.thread.WifiReceiverTask;
import com.example.wi_ficollector.wrapper.WifiLocation;

import java.util.concurrent.CountDownLatch;

public class WiFiReceiver extends BroadcastReceiver {

    private CountDownLatch mCountDownLatch;
    private WifiLocation mWifiLocation;

    public WiFiReceiver(CountDownLatch mCountDownLatch, WifiLocation mWifiLocation) {
        this.mCountDownLatch = mCountDownLatch;
        this.mWifiLocation = mWifiLocation;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        WifiReceiverTask wiFiReceiverTask = new WifiReceiverTask(intent, context, mCountDownLatch, mWifiLocation);
        Thread wifiReceiverWorker = new Thread(wiFiReceiverTask);

        wifiReceiverWorker.start();
    }
}