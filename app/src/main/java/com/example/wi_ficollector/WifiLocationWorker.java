package com.example.wi_ficollector;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.core.content.ContextCompat;

import androidx.work.ListenableWorker;

import androidx.work.WorkerParameters;

import com.example.wi_ficollector.repository.WifiLocationRepository;
import com.example.wi_ficollector.thread.LocationTask;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import static android.content.Context.MODE_APPEND;
import static com.example.wi_ficollector.utils.Constants.*;


public class WifiLocationWorker extends ListenableWorker {

    private WifiLocationRepository mWifiLocationRepository;
    private WifiManager mWifiManager;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Context mContext;
    private LocationCallback mLocationCallback;
    private FileOutputStream mFileOutputStream;

    public WifiLocationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        mContext = context;
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        return CallbackToFutureAdapter.getFuture(completer -> {
            if (!hasPermission()) {
                completer.set(Result.failure());
            } else {
                doWork(completer);
            }
            return completer;
        });
    }

    private void doWork(CallbackToFutureAdapter.Completer<Result> completer) {
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mContext);
        mWifiLocationRepository = new WifiLocationRepository();
        try {
            mFileOutputStream = mContext.openFileOutput(FILE_NAME, MODE_APPEND);
        } catch (FileNotFoundException exception) {
            Log.d(FILE_NOT_FOUND_EXCEPTION_TAG, FILE_NOT_FOUND_EXCEPTION_MESSAGE);
        }

        createLocationRequest();
        getLocationCallbackResult(completer);
        requestLocationUpdates(completer);
    }


    private void startWifiScanning() {
        boolean isWiFiScanningSucceed = mWifiManager.startScan();
        if (!isWiFiScanningSucceed) {
            Log.d(WIFI_SCANNING_FAIL_TAG, WIFI_SCANNING_FAIL_MESSAGE);
        } else {
            Log.d(WIFI_SCANNING_SUCCESS_TAG, WIFI_SCANNING_SUCCESS_MESSAGE);
        }
    }

    private void getLocationCallbackResult(CallbackToFutureAdapter.Completer<Result> completer) {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult == null) {
                    return;
                }
                completer.set(Result.success());
                for (Location location : locationResult.getLocations()) {
                    startWifiScanning();
                    LocationTask locationTask = new LocationTask(location, mWifiLocationRepository, mFileOutputStream);
                    Thread thread = new Thread(locationTask);
                    thread.start();
                }
            }
        };
    }

    private void requestLocationUpdates(CallbackToFutureAdapter.Completer<Result> completer) {
        try {
            mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback,
                    Looper.getMainLooper());
        } catch (SecurityException unlikely) {
            completer.set(Result.failure());
        }
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest = LocationRequest.create();
    }

    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return ContextCompat.checkSelfPermission(mContext,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(mContext,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
    }
}