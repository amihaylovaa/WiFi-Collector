package com.example.wi_ficollector.notification;

import android.app.Notification;
import android.content.Context;

public abstract class ApplicationNotification {

    protected Context mContext;

    public ApplicationNotification(Context mContext) {
        this.mContext = mContext;
    }

    public abstract Notification createNotification();
    public abstract void createNotificationChannel();
}
