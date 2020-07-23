package com.example.wi_ficollector.service;

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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.wi_ficollector.notification.AppNotification;
import com.example.wi_ficollector.notification.ForegroundServiceNotification;
import com.example.wi_ficollector.receiver.GPSStateReceiver;
import com.example.wi_ficollector.receiver.WiFiReceiver;
import com.example.wi_ficollector.repository.WifiLocationRepository;
import com.example.wi_ficollector.wrapper.WifiLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.time.LocalTime;
import java.util.List;

import static com.example.wi_ficollector.utils.Constants.*;

// todo remove numOfWifiLocation from constants make it private here,
// todo then add it into the intent in wifi broadcast receiver
// todo  pass is as argument in save
// todo  add it into the intent in local broadcast receiver and start the thread with argument
public class ForegroundWifiLocationService extends Service {

    private WifiManager mWifiManager;
    private BroadcastReceiver mWifiReceiver;
    private GPSStateReceiver mGPSStateReceiver;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;
    private WifiLocationRepository mWifiLocationRepository;
    private WifiLocation mWifiLocation;
    private LocalBroadcastManager mLocalBroadcastManager;
    private Intent mLocalBroadcastIntent;

    @Override
    public void onCreate() {
        super.onCreate();
        createLocationRequest();

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        mWifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        mWifiLocationRepository = new WifiLocationRepository(this);
        mWifiLocation = new WifiLocation();
        mGPSStateReceiver = new GPSStateReceiver();
        mWifiReceiver = new WiFiReceiver(mWifiLocationRepository, mWifiLocation);
        mLocalBroadcastIntent = new Intent(ACTION);
        AppNotification appNotification = new ForegroundServiceNotification(this);
        NotificationCompat.Builder notificationBuilder = appNotification.createNotification();
        int foregroundServiceNotificationId = 721;

        startForeground(foregroundServiceNotificationId, notificationBuilder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        implementLocationResultCallback();
        requestLocationUpdates();
        registerReceiver(mWifiReceiver, SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(mGPSStateReceiver, PROVIDERS_CHANGED_ACTION);

        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopServiceWork();
        stopSelf();
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

    public void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        mLocationRequest.setInterval(FIVE_SECONDS);
        mLocationRequest.setFastestInterval(THREE_SECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void registerReceiver(BroadcastReceiver broadcastReceiver, String action) {
        IntentFilter intentFilter = new IntentFilter(action);

        registerReceiver(broadcastReceiver, intentFilter);
    }

    public void requestLocationUpdates() {
        Looper mainLooper = Looper.getMainLooper();
        try {
            mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, mainLooper);
        } catch (SecurityException securityException) {
            stopServiceWork();
            stopSelf();
        }
    }

    public void implementLocationResultCallback() {
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
                    mWifiLocation.setLocation(location);
                    startWifiScanning();
                }
            }
        };
    }

    private void startWifiScanning() {
        boolean isWifiScanningSucceeded = mWifiManager.startScan();

        if (!isWifiScanningSucceeded) {
            mWifiLocationRepository.save(mWifiLocation);
        } else {
            mLocalBroadcastManager.sendBroadcast(mLocalBroadcastIntent);
        }
    }

    private void stopServiceWork() {
        try {
            unregisterReceiver(mWifiReceiver);
            unregisterReceiver(mGPSStateReceiver);
        } catch (IllegalArgumentException illegalArgumentException) {
            Log.d(ILLEGAL_ARGUMENT_EXCEPTION_THROWN_TAG, ILLEGAL_ARGUMENT_EXCEPTION_THROWN_MSG);
        }
        try {
            mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
        } catch (NullPointerException npe) {
            Log.d(NULL_POINTER_EXCEPTION_THROWN_TAG, NULL_POINTER_EXCEPTION_THROWN_MESSAGE);
        }
        mWifiLocationRepository.closeFileOutputStream();
    }
}