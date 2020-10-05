package com.thesis.wificollector.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.example.wi_ficollector.R;
import com.thesis.wificollector.activity.ScanActivity;

public class GPSNotification extends AppNotification {

    public GPSNotification(Context mContext) {
        super(mContext);
    }

    @Override
    public Notification createNotification() {
        String channelId = "Disabled GSP";
        Intent intent = new Intent(mContext, ScanActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
        CharSequence contentText = mContext.getString(R.string.disabled_gps_notification_content_text);
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.bigText(contentText);

        if (shouldCreateNotificationChannel()) {
            createNotificationChannel(channelId);
        }

        return new NotificationCompat.Builder(mContext, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setStyle(bigTextStyle)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();
    }

    @Override
    public void createNotificationChannel(String id) {
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        CharSequence name = mContext.getString(R.string.disabled_gps_notification_channel_name);
        NotificationChannel channel = new NotificationChannel(id, name, importance);
        NotificationManager notificationManager = ((NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE));

        notificationManager.createNotificationChannel(channel);
    }
}
