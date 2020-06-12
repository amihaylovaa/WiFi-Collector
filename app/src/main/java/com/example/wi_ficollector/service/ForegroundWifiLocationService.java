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

import static com.example.wi_ficollector.utils.Constants.FIVE_SECONDS;
import static com.example.wi_ficollector.utils.Constants.IO_EXCEPTION_THROWN_MESSAGE;
import static com.example.wi_ficollector.utils.Constants.IO_EXCEPTION_THROWN_TAG;
import static com.example.wi_ficollector.utils.Constants.THREE_SECONDS;

public class ForegroundWifiLocationService extends Service {

    private WifiManager mWifiManager;
    private BroadcastReceiver mWifiReceiver;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;
    private WifiLocationRepository mWifiLocationRepository;
    private WifiLocation mWifiLocation;
    private Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        String CHANNEL_ID = "channel";
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Wifi-collector",
                NotificationManager.IMPORTANCE_DEFAULT);

        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Wi-Fi collector")
                .setContentText("Application continues to collect your location and wireless access points around you")
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .build();

        startForeground(1, notification);
        mContext = this;
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mContext);
        mWifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        mWifiLocationRepository = new WifiLocationRepository(mContext);
        mWifiLocation = WifiLocation.getWifiLocation();
    }

    private void registerWiFiReceiver() {
        mWifiReceiver = new WiFiReceiver(mWifiLocationRepository, mWifiLocation);
        IntentFilter intentFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

        registerReceiver(mWifiReceiver, intentFilter);
    }

    public void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        mLocationRequest.setInterval(FIVE_SECONDS);
        mLocationRequest.setFastestInterval(THREE_SECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void requestLocationUpdates() {
        try {
            mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.getMainLooper());
        } catch (SecurityException securityException) {
        }
    }

    private void receiveLocationResults() {
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
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    mWifiLocation.setLongitude(longitude);
                    mWifiLocation.setLatitude(latitude);
                    startWifiScanning();
                }
            }
        };
    }

    private void startWifiScanning() {
        boolean isWifiScanningSucceeded = mWifiManager.startScan();

        if (!isWifiScanningSucceeded) {
            try {
                mWifiLocationRepository.save();
            } catch (IOException e) {
                Log.d(IO_EXCEPTION_THROWN_TAG, IO_EXCEPTION_THROWN_MESSAGE);
            }
        } else {
            //    UIUpdateTask uiUpdateTask = new UIUpdateTask(tv);
            //    new Thread(uiUpdateTask).start();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createLocationRequest();
        receiveLocationResults();
        requestLocationUpdates();
        registerWiFiReceiver();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //    stopForeground(true);
        //   stopSelf();
        unregisterReceiver(mWifiReceiver);
        mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
        mWifiLocationRepository.closeFileOutputStream();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
