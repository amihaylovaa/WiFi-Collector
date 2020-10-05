package com.thesis.wificollector.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

import androidx.core.app.NotificationCompat;

import com.thesis.wificollector.R;

public class ForegroundServiceNotification extends AppNotification {

    public ForegroundServiceNotification(Context mContext) {
        super(mContext);
    }

    @Override
    public Notification createNotification() {
        String channelId = "Foreground service";
        CharSequence contentText = mContext.getString(R.string.foreground_service_notification_content_text);
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.bigText(contentText);

        if (shouldCreateNotificationChannel()) {
            createNotificationChannel(channelId);
        }

        return new NotificationCompat.Builder(mContext, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setStyle(bigTextStyle)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();
    }

    @Override
    public void createNotificationChannel(String id) {
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        CharSequence name = mContext.getString(R.string.foreground_service_channel_name);
        NotificationChannel channel = new NotificationChannel(id, name, importance);
        NotificationManager notificationManager = ((NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE));

        notificationManager.createNotificationChannel(channel);
    }
}