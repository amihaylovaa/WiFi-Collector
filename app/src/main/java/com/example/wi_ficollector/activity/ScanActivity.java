package com.example.wi_ficollector.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.wifi.WifiManager;
import android.os.*;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.work.*;

import com.example.wi_ficollector.R;
import com.example.wi_ficollector.*;
import com.example.wi_ficollector.preference.ScanPreference;
import com.example.wi_ficollector.receiver.WiFiReceiver;
import com.example.wi_ficollector.repository.WifiLocationRepository;
import com.example.wi_ficollector.thread.LocationThread;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.*;
import com.google.android.gms.tasks.Task;

import java.io.*;
import java.util.concurrent.TimeUnit;


import static com.example.wi_ficollector.utils.Constants.*;

public class ScanActivity extends AppCompatActivity implements LifecycleOwner {

    private FileOutputStream mFileOutputStream;
    private WifiManager mWifiManager;
    private BroadcastReceiver mWifiReceiver;
    private WorkManager mWorkManager;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;
    private ScanPreference mScanPreference;
    private WifiLocationRepository mWifiLocationRepository;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            mFileOutputStream = this.openFileOutput(FILE_NAME, MODE_APPEND);
        } catch (FileNotFoundException e) {
            File file = new File(this.getFilesDir(), FILE_NAME);
        }

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mWifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        mWorkManager = WorkManager.getInstance(getApplicationContext());
        mScanPreference = new ScanPreference(this);
        mWifiLocationRepository = new WifiLocationRepository();

        if (!isBackgroundPermissionRequestRequired()) {
            createBackgroundTask();
        } else {
            if (mScanPreference.isFirstTimeLaunched()) {
                mScanPreference.addFirstTimeLaunchingKey();
            }
        }

        requestLocationPermission();
        receiveLocationResults();
        registerWiFiReceiver();
    }

    public void enableGPS() {
        createLocationRequest();

        LocationSettingsRequest.Builder builder = createLocationSettingsRequest();
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnFailureListener(this, e -> {
            if (e instanceof ResolvableApiException) {
                ResolvableApiException resolvable = (ResolvableApiException) e;
                showGPSRequirements(resolvable);
            }
        });

        task.addOnSuccessListener(this, locationSettingsResponse -> requestLocationUpdates());
    }

    public void createLocationRequest() {
        mLocationRequest = LocationRequest.create();

        mLocationRequest.setInterval(TWO_MINUTES);
        mLocationRequest.setFastestInterval(ONE_MINUTE);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public LocationSettingsRequest.Builder createLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();

        builder.addLocationRequest(mLocationRequest);

        return builder;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_LOCATION_SETTINGS_CODE && resultCode == Activity.RESULT_OK) {
            requestLocationUpdates();
        }
    }

    public void requestFineLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{ACCESS_FINE_LOCATION_PERMISSION}, FINE_LOCATION_PERMISSION_CODE);
    }

    public void requestBackgroundLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{ACCESS_BACKGROUND_LOCATION_PERMISSION}, BACKGROUND_LOCATION_PERMISSION_CODE);
    }

    public boolean isFineLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(this,
                ACCESS_FINE_LOCATION_PERMISSION) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean isBackgroundLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(this,
                ACCESS_BACKGROUND_LOCATION_PERMISSION) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isBackgroundPermissionRequestRequired() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    }

    public void requestLocationPermission() {
        if (!isFineLocationPermissionGranted()) {
            requestFineLocationPermission();
        } else {
            new Thread(() -> enableGPS()).start();
            if (isBackgroundPermissionRequestRequired()) {
                requestBackgroundPermission();
            }
        }
    }

    public void requestBackgroundPermission() {
        if (isBackgroundLocationPermissionGranted()) {
            createBackgroundTask();
        } else {
            if (mScanPreference.shouldShowBackgroundPermissionRequestRationale()) {
                showBackgroundPermissionRequestRationale();
            }
        }
    }

    public void showBackgroundPermissionRequestRationale() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        alertDialog.setMessage(BACKGROUND_PERMISSION_REQUEST_RATIONALE);
        alertDialog.setPositiveButton(R.string.OK, (dialog, id) -> requestBackgroundLocationPermission());
        alertDialog.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, @NonNull int[] grantResults) {
        for (int i = 0; i < permissions.length; i++) {
            switch (requestCode) {
                case FINE_LOCATION_PERMISSION_CODE:
                    handleFineLocationPermissionRequestResult(grantResults[i]);
                    break;
                case BACKGROUND_LOCATION_PERMISSION_CODE:
                    handleBackgroundPermissionRequestResult(grantResults[i], permissions[i]);
                    break;
                default:
                    Toast.makeText(this, "Unrecognized permission", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handleBackgroundPermissionRequestResult(int grantResult, String permission) {
        if (grantResult == PackageManager.PERMISSION_GRANTED) {
            createBackgroundTask();
        } else {
            if (!shouldShowRequestPermissionRationale(permission)) {
                mScanPreference.stopShowBackgroundPermissionRequestRationale();
                mWorkManager.cancelAllWork();
            }
        }
    }

    private void handleFineLocationPermissionRequestResult(int grantResult) {
        if (grantResult == PackageManager.PERMISSION_GRANTED) {
            new Thread(() -> enableGPS()).start();
            if (isBackgroundPermissionRequestRequired()) {
                requestBackgroundPermission();
            }
        }
    }

    private void showGPSRequirements(ResolvableApiException resolvable) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        alertDialog.setMessage(GPS_REQUIREMENTS);
        alertDialog.setPositiveButton(R.string.OK, (dialog, id) -> {
            try {
                resolvable.startResolutionForResult(this, REQUEST_LOCATION_SETTINGS_CODE);
            } catch (IntentSender.SendIntentException sendEx) {
                // todo some handling
            }
        });
        alertDialog.create().show();
    }

    private void registerWiFiReceiver() {
        mWifiReceiver = new WiFiReceiver();
        IntentFilter intentFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

        registerReceiver(mWifiReceiver, intentFilter);
    }

    void createBackgroundTask() {
        PeriodicWorkRequest periodicWork = createPeriodicWorkRequest();

        mWorkManager.
                enqueueUniquePeriodicWork("Worker",
                        ExistingPeriodicWorkPolicy.REPLACE,
                        periodicWork);

        findBackgroundWorkRequest(periodicWork);
    }

    private PeriodicWorkRequest createPeriodicWorkRequest() {
        String workerRequestTag = "Background location worker ";
        Constraints constraints = new Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build();

        return new PeriodicWorkRequest
                .Builder(WifiLocationWorker.class, 15, TimeUnit.MINUTES)
                .addTag(workerRequestTag)
                .setConstraints(constraints)
                .setInitialDelay(5, TimeUnit.MINUTES)
                .build();
    }

    private void findBackgroundWorkRequest(PeriodicWorkRequest periodicWork) {
        mWorkManager.getWorkInfoByIdLiveData(periodicWork.getId()).observe(
                this, workInfo -> {
                    if (workInfo != null && workInfo.getState() == WorkInfo.State.FAILED) {
                        hasWorkFailed = true;
                    }
                }
        );
    }

    private void requestLocationUpdates() {
        try {
            mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.getMainLooper());
        } catch (SecurityException securityException) {
            requestLocationPermission();
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
                for (Location location : locationResult.getLocations()) {
                    startWifiScanning();
                    LocationThread locationThread = new LocationThread(location, mWifiLocationRepository, mFileOutputStream);
                    Thread thread = new Thread(locationThread);
                    thread.start();
                }
            }
        };
    }

    private void startWifiScanning() {
        boolean isWiFiScanningSucceed = mWifiManager.startScan();
        if (!isWiFiScanningSucceed) {
            ;
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        requestLocationPermission();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWorkManager.cancelAllWork();
        unregisterReceiver(mWifiReceiver);
        try {
            mFileOutputStream.close();
        } catch (IOException e) {
        }
    }
}