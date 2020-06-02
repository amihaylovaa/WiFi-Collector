package com.example.wi_ficollector.thread;

import android.widget.TextView;

import static com.example.wi_ficollector.utils.Constants.FIVE_SECONDS;
import static com.example.wi_ficollector.utils.Constants.numberFoundWifiNetworks;

public class UIUpdateTask implements Runnable {

    private TextView tv;

    public UIUpdateTask(TextView tv) {
        this.tv = tv;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(FIVE_SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        tv.post(() -> {
            tv.invalidate();
            tv.setText(String.valueOf(numberFoundWifiNetworks));
        });
    }
}
