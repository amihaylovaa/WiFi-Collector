package com.example.wi_ficollector.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.wifi.WifiManager;
import android.os.*;

import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.example.wi_ficollector.R;
import com.example.wi_ficollector.preference.ScanPreference;
import com.example.wi_ficollector.receiver.WiFiReceiver;
import com.example.wi_ficollector.repository.WifiLocationRepository;
import com.example.wi_ficollector.thread.*;
import com.example.wi_ficollector.wrapper.WifiLocation;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.*;
import com.google.android.gms.tasks.Task;

import java.io.*;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import static com.example.wi_ficollector.utils.Constants.*;

public class ScanActivity extends AppCompatActivity implements LifecycleOwner {
    boolean isWifiScanningSucceeded;
    private FileOutputStream mFileOutputStream;
    private WifiManager mWifiManager;
    private BroadcastReceiver mWifiReceiver;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;
    private ScanPreference mScanPreference;
    private WifiLocationRepository mWifiLocationRepository;
    private Thread mGPSEnablingThread;
    private WifiLocation mWifiLocation;
    private TextView tv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        initializeFields();

        if (isBackgroundPermissionRequestRequired() && mScanPreference.isActivityFirstTimeLaunched()) {
            mScanPreference.addBackgroundPermissionRationaleKey();
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
        mLocationRequest = new LocationRequest();

        mLocationRequest.setInterval(FIVE_SECONDS);
        mLocationRequest.setFastestInterval(THREE_SECONDS);
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
            startGPSEnablingThread();
            if (isBackgroundPermissionRequestRequired()) {
                requestBackgroundPermission();
            }
        }
    }

    private void startGPSEnablingThread() {
        if (mGPSEnablingThread.getState() == Thread.State.NEW) {
            mGPSEnablingThread.start();
        }
    }

    public void requestBackgroundPermission() {
        if (isBackgroundLocationPermissionGranted()) {
            // todo handling
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
            // todo handling
        } else {
            if (!shouldShowRequestPermissionRationale(permission)) {
                mScanPreference.stopShowBackgroundPermissionRequestRationale();
                // todo handling
            }
        }
    }

    private void handleFineLocationPermissionRequestResult(int grantResult) {
        if (grantResult == PackageManager.PERMISSION_GRANTED) {
            startGPSEnablingThread();
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
        mWifiReceiver = new WiFiReceiver(mWifiLocationRepository, mFileOutputStream);
        IntentFilter intentFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

        registerReceiver(mWifiReceiver, intentFilter);
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
                List<Location> locations = locationResult.getLocations();
                LocalTime localTime = LocalTime.now();

                mWifiLocation.setLocalTime(localTime);
                for (Location location : locations) {
                    mWifiLocation.setLocation(location);
                    isWifiScanningSucceeded = mWifiManager.startScan();
                    if (!isWifiScanningSucceeded) {
                        try {
                            // wifi scan - null
                            mWifiLocationRepository.saveWiFiLocation(mFileOutputStream);
                        } catch (TransformerException | ParserConfigurationException e) {

                        }
                    } else {
                        UIUpdateTask uiUpdateTask = new UIUpdateTask(tv);
                        new Thread(uiUpdateTask).start();
                    }
                }
            }
        };
    }

    private void initializeFields() {
        openFileOutputStream();

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mWifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        mScanPreference = new ScanPreference(this);
        mGPSEnablingThread = new Thread(this::enableGPS);
        mWifiLocation = WifiLocation.getWifiLocation();
        mWifiLocationRepository = new WifiLocationRepository(mWifiLocation);
        tv = findViewById(R.id.numberOfWifiNetworks);

        tv.setText(String.valueOf(numberFoundWifiNetworks));
    }

    private void openFileOutputStream() {
        try {
            mFileOutputStream = this.openFileOutput(FILE_NAME, MODE_APPEND);
        } catch (FileNotFoundException exception) {
            Log.d(FILE_NOT_FOUND_EXCEPTION_TAG, FILE_NOT_FOUND_EXCEPTION_MESSAGE);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        requestLocationPermission();
        tv.invalidate();
        tv.setText(String.valueOf(numberFoundWifiNetworks));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mWifiReceiver);
        mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
        try {
            mFileOutputStream.close();
        } catch (IOException e) {
            Log.d(IO_EXCEPTION_THROWN_TAG, IO_EXCEPTION_THROWN_MESSAGE);
        }
    }
}