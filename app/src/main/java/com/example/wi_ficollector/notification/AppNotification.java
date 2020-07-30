package com.example.wi_ficollector.notification;

import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public abstract class AppNotification {

    protected Context mContext;

    public AppNotification(Context mContext) {
        this.mContext = mContext;
    }

    public boolean shouldCreateNotificationChannel() {
       return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    public abstract NotificationCompat.Builder createNotification();

    public abstract void createNotificationChannel(String id);
}