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

import com.example.wi_ficollector.notification.ApplicationNotification;
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
    private Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
//        Log.d("SERVICE", "ON CREATE");
        mContext = this;
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mContext);
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        mWifiLocationRepository = new WifiLocationRepository(mContext);
        mWifiLocation = new WifiLocation();
        mGPSStateReceiver = new GPSStateReceiver();
        mWifiReceiver = new WiFiReceiver(mWifiLocationRepository, mWifiLocation);
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        createLocationRequest();
        ApplicationNotification applicationNotification = new ForegroundServiceNotification(mContext);
        NotificationCompat.Builder notificationBuilder = applicationNotification.createNotification();
        int foregroundServiceNotificationId = 721;

        startForeground(foregroundServiceNotificationId, notificationBuilder.build());
    }

    public void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        mLocationRequest.setInterval(FIVE_SECONDS);
        mLocationRequest.setFastestInterval(THREE_SECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
   //     Log.d("SERVICE", "ON START");
        implementLocationResultCallback();
        requestLocationUpdates();
        registerReceiver(mWifiReceiver, SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(mGPSStateReceiver, PROVIDERS_CHANGED_ACTION);

        return START_STICKY;
    }

    private void registerReceiver(BroadcastReceiver broadcastReceiver, String action) {
        IntentFilter intentFilter = new IntentFilter(action);

        registerReceiver(broadcastReceiver, intentFilter);
    }

    public void requestLocationUpdates() {
        try {
            mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback,
                    Looper.getMainLooper());
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
                LocalTime localTime = LocalTime.now();
                mWifiLocation.setLocalTime(localTime);
       //         Log.d("Found", "Location - " + String.valueOf(localTime) + "");

                for (Location location : locations) {
                    mWifiLocation.setLocation(location);
                    startWifiScanning();
                }
            }
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
      //  Log.d("SERVICE", "ON DESTROY");
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
        //    Log.d("SERVICE Scanning", "Failed");
            mWifiLocationRepository.save(mWifiLocation);
        } else {
            Intent intent = new Intent("UI_UPDATE");
            mLocalBroadcastManager.sendBroadcast(intent);
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

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopServiceWork();
        stopSelf();
    }
}