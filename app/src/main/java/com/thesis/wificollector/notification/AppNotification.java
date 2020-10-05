package com.thesis.wificollector.notification;

import android.app.Notification;
import android.content.Context;
import android.os.Build;

public abstract class AppNotification {

    protected Context mContext;

    public AppNotification(Context mContext) {
        this.mContext = mContext;
    }

    public boolean shouldCreateNotificationChannel() {
       return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    public abstract Notification createNotification();

    public abstract void createNotificationChannel(String id);
}
