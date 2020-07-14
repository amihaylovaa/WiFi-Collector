package com.example.wi_ficollector.receiver;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.wi_ficollector.notification.ApplicationNotification;
import com.example.wi_ficollector.notification.GSPNotification;
import com.example.wi_ficollector.service.ForegroundWifiLocationService;

public class GPSStateReceiver extends android.content.BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        LocationManager locationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
        String action = intent.getAction();
        boolean isProviderEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (action.equals("android.location.PROVIDERS_CHANGED") && !isProviderEnabled) {
            context.stopService(new Intent(context, ForegroundWifiLocationService.class));

            ApplicationNotification disabledGPSNotification = new GSPNotification(context);
            NotificationCompat.Builder builder = disabledGPSNotification.createNotification();
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            int notificationId = 182;

            notificationManager.notify(notificationId, builder.build());
        }
    }
}
