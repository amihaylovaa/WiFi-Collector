package com.example.wi_ficollector.thread;

import android.widget.TextView;

import static com.example.wi_ficollector.utils.Constants.numOfWiFi;

public class UIUpdateTask implements Runnable {

    private TextView tv;

    public UIUpdateTask(TextView tv) {
        this.tv = tv;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        tv.post(() -> {
            tv.invalidate();
            tv.setText(String.valueOf(numOfWiFi));
        });
    }
}
