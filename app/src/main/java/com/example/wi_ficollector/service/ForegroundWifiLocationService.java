package com.example.wi_ficollector.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.wi_ficollector.R;
import com.example.wi_ficollector.receiver.WiFiReceiver;
import com.example.wi_ficollector.repository.WifiLocationRepository;
import com.example.wi_ficollector.wrapper.WifiLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.time.LocalTime;
import java.util.List;

import static com.example.wi_ficollector.utils.Constants.*;


// todo add notification class
public class ForegroundWifiLocationService extends Service {

    private WifiManager mWifiManager;
    private BroadcastReceiver mWifiReceiver;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;
    private WifiLocationRepository mWifiLocationRepository;
    private WifiLocation mWifiLocation;

    @Override
    public void onCreate() {
        super.onCreate();

        Context context = this;
        Notification notification = createNotification(context);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mWifiLocationRepository = new WifiLocationRepository(context);
        mWifiLocation = WifiLocation.getWifiLocation();

        startForeground(FOREGROUND_SERVICE_NOTIFICATION_ID, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createLocationRequest();
        receiveLocationResults();
        requestLocationUpdates();
        registerWiFiReceiver();

        return START_STICKY;
    }

    public void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        mLocationRequest.setInterval(FIVE_SECONDS);
        mLocationRequest.setFastestInterval(THREE_SECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void requestLocationUpdates() {
        try {
            mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.getMainLooper());
        } catch (SecurityException securityException) {
            stopSelf();
            // todo show notification for denied permission
        }
    }

    public void receiveLocationResults() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult == null) {
                    return;
                }
                List<Location> locations = locationResult.getLocations();
                mWifiLocation.setLocalTime(LocalTime.now());

                for (Location location : locations) {
                    Log.d("Found location", "Service");
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    mWifiLocation.setLongitude(longitude);
                    mWifiLocation.setLatitude(latitude);
                    startWifiScanning();
                }
            }
        };
    }

    private void registerWiFiReceiver() {
        mWifiReceiver = new WiFiReceiver(mWifiLocationRepository, mWifiLocation);
        IntentFilter intentFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

        registerReceiver(mWifiReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopServiceWork();
        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startWifiScanning() {
        boolean isWifiScanningSucceeded = mWifiManager.startScan();

        if (!isWifiScanningSucceeded) {
            try {
                mWifiLocationRepository.save();
            } catch (IOException e) {
                Log.d(IO_EXCEPTION_THROWN_TAG, IO_EXCEPTION_THROWN_MESSAGE);
            }
        }
    }

    private void stopServiceWork() {
        unregisterReceiver(mWifiReceiver);
        mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
        mWifiLocationRepository.closeFileOutputStream();
    }

    private Notification createNotification(Context context) {
        CharSequence contentText = getString(R.string.foreground_service_notification_text);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }

        return new NotificationCompat.Builder(context, FOREGROUND_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();
    }

    private void createNotificationChannel() {
        CharSequence channelName = getString(R.string.channel_name);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(FOREGROUND_CHANNEL_ID, channelName, importance);
        NotificationManager notificationManager = ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE));

        notificationManager.createNotificationChannel(channel);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopServiceWork();
        mWifiLocationRepository.closetags();
        stopSelf();
    }
}