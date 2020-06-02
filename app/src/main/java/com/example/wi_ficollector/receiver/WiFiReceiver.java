package com.example.wi_ficollector.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.wi_ficollector.thread.WifiReceiverTask;

public class WiFiReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        WifiReceiverTask wiFiReceiverTask = new WifiReceiverTask(context, intent);
        Thread thread = new Thread(wiFiReceiverTask);

        thread.start();
    }
}