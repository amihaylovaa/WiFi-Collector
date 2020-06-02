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
import com.example.wi_ficollector.wrapper.WifiLocation;
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


public class WifiLocationBackgroundWorker extends ListenableWorker {

    private WifiLocationRepository mWifiLocationRepository;
    private WifiManager mWifiManager;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Context mContext;
    private LocationCallback mLocationCallback;
    private FileOutputStream mFileOutputStream;
    private WifiLocation mWifiLocation;

    public WifiLocationBackgroundWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
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
      //  mWifiLocationRepository = new WifiLocationRepository();
        mWifiLocation = new WifiLocation();
        mLocationRequest = new LocationRequest();

        try {
            mFileOutputStream = mContext.openFileOutput(FILE_NAME, MODE_APPEND);
        } catch (FileNotFoundException exception) {
            Log.d(FILE_NOT_FOUND_EXCEPTION_TAG, FILE_NOT_FOUND_EXCEPTION_MESSAGE);
        }

        getLocationCallbackResult(completer);
        requestLocationUpdates(completer);
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
           //     for (Location location : locationResult.getLocations()) {
             //       boolean isWifiScanningSucceeded = mWifiManager.startScan();
               //     LocationTask locationTask = new LocationTask(mWifiLocationRepository,
                 //           mFileOutputStream, isWifiScanningSucceeded);
                  //  new Thread(locationTask).start();
                    // todo add ui thread
            //    }
            }
        };
    }

    private void requestLocationUpdates(CallbackToFutureAdapter.Completer<Result> completer) {
        try {
            mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.getMainLooper());
        } catch (SecurityException unlikely) {
            completer.set(Result.failure());
        }
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