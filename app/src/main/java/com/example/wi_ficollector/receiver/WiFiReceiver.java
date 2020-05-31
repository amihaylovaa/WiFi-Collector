package com.example.wi_ficollector.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.wi_ficollector.thread.WifiReceiverThread;

public class WiFiReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        WifiReceiverThread wiFiReceiverThread = new WifiReceiverThread(context, intent);
        Thread thread = new Thread(wiFiReceiverThread);

        thread.start();
    }
}