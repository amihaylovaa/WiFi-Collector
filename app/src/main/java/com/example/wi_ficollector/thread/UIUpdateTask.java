package com.example.wi_ficollector.thread;

import android.widget.TextView;


public class UIUpdateTask implements Runnable {

    private TextView tv;
    int numOfWifiLocations;

    public UIUpdateTask(TextView tv, int numOfWifiLocations) {
        this.tv = tv;
        this.numOfWifiLocations = numOfWifiLocations;
    }

    @Override
    public void run() {
        tv.post(() -> {
            tv.invalidate();
            tv.setText(String.valueOf(numOfWifiLocations));
        });
    }
}
