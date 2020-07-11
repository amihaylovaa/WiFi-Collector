package com.example.wi_ficollector.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.wi_ficollector.notification.ApplicationNotification;
import com.example.wi_ficollector.notification.ForegroundServiceNotification;
import com.example.wi_ficollector.notification.LocationPermissionNotification;
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
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;
    private WifiLocationRepository mWifiLocationRepository;
    private WifiLocation mWifiLocation;
    private Context mContext;


    @Override
    public void onCreate() {
        super.onCreate();
        initializeFields();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !isBackgroundLocationPermissionGranted()) {
            showDeniedPermissionNotification();
        } else if (!isFineLocationPermissionGranted()) {
            showDeniedPermissionNotification();
        } else {
            ApplicationNotification applicationNotification = new ForegroundServiceNotification(mContext);
            NotificationCompat.Builder notificationBuilder = applicationNotification.createNotification();
            int foregroundServiceNotificationId = 721;

            startForeground(foregroundServiceNotificationId, notificationBuilder.build());
        }
    }

    private void initializeFields() {
        mContext = this;
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mContext);
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        mWifiLocationRepository = new WifiLocationRepository(mContext);
        mWifiLocation = WifiLocation.getWifiLocation();
    }

    private void showDeniedPermissionNotification() {
        ApplicationNotification locationPermissionNotification = new LocationPermissionNotification(this);
        NotificationCompat.Builder builder = locationPermissionNotification.createNotification();
        int notificationId = 93;

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(notificationId, builder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createLocationRequest();
        implementLocationResultCallback();
        requestLocationUpdates();
        registerWiFiReceiver();
        registerGPSStateReceiver();

        return START_STICKY;
    }

    public boolean isFineLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(this,
                ACCESS_FINE_LOCATION_PERMISSION) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean isBackgroundLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(this,
                ACCESS_BACKGROUND_LOCATION_PERMISSION) == PackageManager.PERMISSION_GRANTED;
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

    private void registerGPSStateReceiver() {
        GPSStateReceiver mDisabledGPSStateReceiver = new GPSStateReceiver();
        IntentFilter intentFilter = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);

        intentFilter.addAction(Intent.ACTION_PROVIDER_CHANGED);
        registerReceiver(mDisabledGPSStateReceiver, intentFilter);
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
            mWifiLocationRepository.save();
        }
    }

    private void stopServiceWork() {
        try {
            mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
            mWifiLocationRepository.closeFileOutputStream();
            unregisterReceiver(mWifiReceiver);
        } catch (IllegalArgumentException illegalArgumentException) {
            // todo add handling for npe
            Log.d(ILLEGAL_ARGUMENT_EXCEPTION_THROWN_TAG, ILLEGAL_ARGUMENT_EXCEPTION_THROWN_MESSAGE);
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopServiceWork();
        stopSelf();
    }
}