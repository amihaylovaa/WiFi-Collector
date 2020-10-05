package com.thesis.wificollector.service;

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

import com.thesis.wificollector.notification.AppNotification;
import com.thesis.wificollector.notification.ForegroundServiceNotification;
import com.thesis.wificollector.receiver.GPSStateReceiver;
import com.thesis.wificollector.receiver.WiFiReceiver;
import com.thesis.wificollector.repository.WifiLocationOutput;
import com.thesis.wificollector.wrapper.WifiLocation;
import com.thesis.wificollector.utility.Constants;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.time.LocalDateTime;
import java.util.List;

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
        registerReceiver(mWifiReceiver, Constants.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(mGPSStateReceiver, Constants.PROVIDERS_CHANGED_ACTION);

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

        mLocationRequest.setInterval(Constants.FIVE_SECONDS);
        mLocationRequest.setFastestInterval(Constants.THREE_SECONDS);
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

        mLocalBroadcastIntent.putExtra(Constants.EXTRA_NAME, numOfWifiLocations);
        mLocalBroadcastIntent.setAction(Constants.ACTION);
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
            Log.d(Constants.ILLEGAL_ARGUMENT_EXCEPTION_THROWN_TAG, Constants.ILLEGAL_ARGUMENT_EXCEPTION_THROWN_MSG);
        }

        mWifiLocationOutput.closeFileOutputStream();
        mWifiLocationOutput.stopExecutorService();
    }
}