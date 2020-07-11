package com.example.wi_ficollector.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.example.wi_ficollector.R;
import com.example.wi_ficollector.activity.ScanActivity;

public class LocationPermissionNotification extends ApplicationNotification {

    public LocationPermissionNotification(Context mContext) {
        super(mContext);
    }

    @Override
    public NotificationCompat.Builder createNotification() {
        String channelId = "Permission denied";
        CharSequence contentText = mContext.getString(R.string.denied_location_permission_content_text);
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.bigText(contentText);
        Intent intent = new Intent(mContext, ScanActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);

        if (shouldCreateNotificationChannel()) {
            createNotificationChannel(channelId);
        }

        return new NotificationCompat.Builder(mContext, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setStyle(bigTextStyle)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
    }

    @Override
    public void createNotificationChannel(String id) {
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        CharSequence name = mContext.getString(R.string.denied_location_permission_channel_name);
        NotificationChannel channel = new NotificationChannel(id, name, importance);
        NotificationManager notificationManager = ((NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE));

        notificationManager.createNotificationChannel(channel);
    }
}
