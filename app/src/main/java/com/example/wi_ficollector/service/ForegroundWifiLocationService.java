package com.example.wi_ficollector.service;

import android.app.Notification;
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.wi_ficollector.notification.AppNotification;
import com.example.wi_ficollector.notification.ForegroundServiceNotification;
import com.example.wi_ficollector.receiver.GPSStateReceiver;
import com.example.wi_ficollector.receiver.WiFiReceiver;
import com.example.wi_ficollector.repository.WifiLocationOutput;
import com.example.wi_ficollector.wrapper.WifiLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.wi_ficollector.utility.Constants.*;

public class ForegroundWifiLocationService extends Service {

    private BroadcastReceiver mWifiReceiver;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private GPSStateReceiver mGPSStateReceiver;
    private Intent mLocalBroadcastIntent;
    private LocalBroadcastManager mLocalBroadcastManager;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;
    private WifiManager mWifiManager;
    private WifiLocationOutput mWifiLocationOutput;
    private WifiLocation mWifiLocation;

    @Override
    public void onCreate() {
        super.onCreate();
        createLocationRequest();

        int foregroundServiceNotificationId = 721;
        AppNotification appNotification = new ForegroundServiceNotification(ForegroundWifiLocationService.this);
        Notification foregroundServiceNotification = appNotification.createNotification();
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(ForegroundWifiLocationService.this);
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(ForegroundWifiLocationService.this);
        mWifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        mWifiLocationOutput = new WifiLocationOutput(ForegroundWifiLocationService.this);
        mWifiLocation = new WifiLocation();
        mGPSStateReceiver = new GPSStateReceiver();
        mWifiReceiver = new WiFiReceiver(mWifiLocationOutput, mWifiLocation);
        mLocalBroadcastIntent = new Intent();

        startForeground(foregroundServiceNotificationId, foregroundServiceNotification);
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

    public void registerReceiver(BroadcastReceiver broadcastReceiver, String action) {
        IntentFilter intentFilter = new IntentFilter(action);

        registerReceiver(broadcastReceiver, intentFilter);
    }

    public void requestLocationUpdates() {
        try {
            mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.getMainLooper());
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

                sendBroadcast();
                List<Location> locations = locationResult.getLocations();

                for (Location location : locations) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    mWifiLocation.setLocalDateTime(LocalDateTime.now());
                    mWifiLocation.setLatitude(latitude);
                    mWifiLocation.setLongitude(longitude);
                    mWifiManager.startScan();
                }
            }
        };
    }

    private void sendBroadcast() {
        int numOfWifiLocations = mWifiLocationOutput.getNumOfWifiLocations();

        mLocalBroadcastIntent.putExtra(EXTRA_NAME, numOfWifiLocations);
        mLocalBroadcastIntent.setAction(ACTION);
        mLocalBroadcastManager.sendBroadcast(mLocalBroadcastIntent);
    }

    private void stopServiceWork() {
        if (mFusedLocationProviderClient != null) {
            mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
        }

        try {
            unregisterReceiver(mWifiReceiver);
            unregisterReceiver(mGPSStateReceiver);
        } catch (IllegalArgumentException illegalArgumentException) {
            Log.d(ILLEGAL_ARGUMENT_EXCEPTION_THROWN_TAG, ILLEGAL_ARGUMENT_EXCEPTION_THROWN_MSG);
        }

        mWifiLocationOutput.closeFileOutputStream();
        mWifiLocationOutput.stopExecutorService();
    }
}