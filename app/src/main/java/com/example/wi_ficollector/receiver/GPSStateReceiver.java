package com.example.wi_ficollector.receiver;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;

import androidx.core.app.NotificationManagerCompat;

import com.example.wi_ficollector.notification.AppNotification;
import com.example.wi_ficollector.notification.GPSNotification;
import com.example.wi_ficollector.service.ForegroundWifiLocationService;

public class GPSStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String PROVIDERS_CHANGED = "android.location.PROVIDERS_CHANGED";
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        String action = intent.getAction();
        boolean isProviderEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (action.equals(PROVIDERS_CHANGED) && !isProviderEnabled) {
            Intent serviceIntent = new Intent(context, ForegroundWifiLocationService.class);

            context.stopService(serviceIntent);
            showDisabledGPSNotification(context);
        }
    }

    private void showDisabledGPSNotification(Context context) {
        int notificationId = 182;
        AppNotification disabledGPSNotification = new GPSNotification(context);
        Notification notification = disabledGPSNotification.createNotification();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        notificationManager.notify(notificationId, notification);
    }
}