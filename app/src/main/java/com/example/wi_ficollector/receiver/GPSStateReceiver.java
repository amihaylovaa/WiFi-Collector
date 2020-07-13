package com.example.wi_ficollector.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.wi_ficollector.notification.ApplicationNotification;
import com.example.wi_ficollector.notification.GSPNotification;
import com.example.wi_ficollector.service.ForegroundWifiLocationService;

import static com.example.wi_ficollector.activity.ScanActivity.isForeground;

public class GPSStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        LocationManager locationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
        String action = intent.getAction();
        boolean isProviderEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (action.equals("android.location.PROVIDERS_CHANGED") && !isProviderEnabled) {
            context.stopService(new Intent(context, ForegroundWifiLocationService.class));

            ApplicationNotification disabledGPSNotification = new GSPNotification(context);
            NotificationCompat.Builder builder = disabledGPSNotification.createNotification();
            int notificationId = 182;

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(notificationId, builder.build());
        }
    }
}
