package com.example.wi_ficollector.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.wi_ficollector.R;

import static com.example.wi_ficollector.utils.Constants.FOREGROUND_CHANNEL_ID;

public class ForegroundServiceNotification {

    Context context;

    public ForegroundServiceNotification(Context context) {
        this.context = context;
    }

    public Notification createNotification() {
        CharSequence contentText = "The app continues to collect your location and wireless access points around you";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }

        return new NotificationCompat.Builder(context, FOREGROUND_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();
    }

    public void createNotificationChannel() {
        CharSequence channelName = "Foreground service execution";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(FOREGROUND_CHANNEL_ID, channelName, importance);
        NotificationManager notificationManager = ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE));

        notificationManager.createNotificationChannel(channel);
    }
}