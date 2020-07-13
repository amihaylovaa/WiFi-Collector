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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.wi_ficollector.R;
import com.example.wi_ficollector.preference.ScanPreference;
import com.example.wi_ficollector.receiver.GPSStateReceiver;
import com.example.wi_ficollector.receiver.WiFiReceiver;
import com.example.wi_ficollector.repository.WifiLocationRepository;
import com.example.wi_ficollector.service.ForegroundWifiLocationService;
import com.example.wi_ficollector.thread.*;
import com.example.wi_ficollector.wrapper.WifiLocation;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.*;
import com.google.android.gms.tasks.Task;

import java.time.LocalTime;
import java.util.List;

import static com.example.wi_ficollector.utils.Constants.*;

public class ScanActivity extends Activity {

    private WifiManager mWifiManager;
    private BroadcastReceiver mWifiReceiver;
    private GPSStateReceiver mGPSStateReceiver;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;
    private ScanPreference mScanPreference;
    private WifiLocationRepository mWifiLocationRepository;
    private WifiLocation mWifiLocation;
    private TextView tv;
    private boolean isBackgroundPermissionRequestRequired;
    private boolean isBackgroundPermissionGranted;
    private boolean isServiceStarted;
    private Intent intent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        initializeFields();

        if (mScanPreference.isActivityFirstTimeLaunched()) {
            mScanPreference.addBackgroundPermissionRationaleKey();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        requestLocationPermission();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (isServiceStarted) {
            stopService(intent);
            isServiceStarted = false;
        }
        requestLocationPermission();
        mWifiLocationRepository.openFileOutputStream();
        startUpdateUIThread();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!isBackgroundPermissionRequestRequired || isBackgroundPermissionGranted) {
            stopActivityWork();
            ContextCompat.startForegroundService(this, intent);
            isServiceStarted = true;
        }
        if (isBackgroundPermissionRequestRequired && !isBackgroundPermissionGranted) {
            stopActivityWork();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_LOCATION_SETTINGS_CODE && resultCode == Activity.RESULT_OK) {
            startActivityWork();
        }
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
                    Log.d(UNRECOGNIZED_PERMISSION_TAG, UNRECOGNIZED_PERMISSION_MESSAGE);
            }
        }
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
        }).addOnSuccessListener(e -> {
            startActivityWork();
        });
    }

    void startActivityWork() {
        implementLocationResultCallback();
        requestLocationUpdates();
        registerReceiver(mWifiReceiver, SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(mGPSStateReceiver, PROVIDERS_CHANGED_ACTION);
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

    public void requestLocationPermission() {
        if (!isFineLocationPermissionGranted()) {
            requestFineLocationPermission();
        } else {
            if (isBackgroundPermissionRequestRequired) {
                requestBackgroundPermission();
            }
            enableGPS();
        }
    }

    public void requestBackgroundPermission() {
        if (isBackgroundLocationPermissionGranted()) {
            isBackgroundPermissionGranted = true;
        } else {
            if (mScanPreference.shouldShowBackgroundPermissionRequestRationale()) {
                showBackgroundPermissionRequestRationale();
            }
        }
    }

    public void showBackgroundPermissionRequestRationale() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setMessage(BACKGROUND_PERMISSION_REQUEST_RATIONALE);
        alertDialogBuilder.setPositiveButton(R.string.OK, (dialog, id) -> requestBackgroundLocationPermission());
        alertDialogBuilder.create().show();
    }

    private void handleBackgroundPermissionRequestResult(int grantResult, String permission) {
        if (grantResult == PackageManager.PERMISSION_GRANTED) {
            isBackgroundPermissionGranted = true;
        } else {
            if (!shouldShowRequestPermissionRationale(permission)) {
                mScanPreference.stopShowBackgroundPermissionRequestRationale();
            }
        }
    }

    private void handleFineLocationPermissionRequestResult(int grantResult) {
        if (grantResult == PackageManager.PERMISSION_GRANTED) {
            enableGPS();
            if (isBackgroundPermissionRequestRequired) {
                requestBackgroundPermission();
            }
        }
    }

    private void showGPSRequirements(ResolvableApiException resolvable) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setMessage(GPS_REQUIREMENTS);
        alertDialogBuilder.setPositiveButton(R.string.OK, (dialog, id) -> {
            try {
                resolvable.startResolutionForResult(this, REQUEST_LOCATION_SETTINGS_CODE);
            } catch (IntentSender.SendIntentException sendEx) {
                // todo some handling
                // todo window leak
            }
        });
        // todo add dismiss
        alertDialogBuilder.create().show();
    }

    private void registerReceiver(BroadcastReceiver broadcastReceiver, String action) {
        IntentFilter intentFilter = new IntentFilter(action);

        registerReceiver(broadcastReceiver, intentFilter);
    }

    private void requestLocationUpdates() {
        try {
            mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.getMainLooper());
        } catch (SecurityException securityException) {
            requestLocationPermission();
        }
    }

    private void implementLocationResultCallback() {
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
            mWifiLocationRepository.save();
        }
        startUpdateUIThread();
    }

    private void initializeFields() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mWifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        mScanPreference = new ScanPreference(this);
        mWifiLocation = WifiLocation.getWifiLocation();
        mWifiLocationRepository = new WifiLocationRepository(ScanActivity.this);
        mWifiReceiver = new WiFiReceiver(mWifiLocationRepository, mWifiLocation);
        mGPSStateReceiver = new GPSStateReceiver();
        intent = new Intent(this, ForegroundWifiLocationService.class);
        isBackgroundPermissionRequestRequired = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
        isBackgroundPermissionGranted = false;
        isServiceStarted = false;
        tv = findViewById(R.id.numberOfWifiNetworks);

        tv.setText(String.valueOf(numberFoundWifiNetworks));
    }

    private void stopActivityWork() {
        try {
            mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
        } catch (NullPointerException npe) {
            Log.d(NULL_POINTER_EXCEPTION_THROWN_TAG, NULL_POINTER_EXCEPTION_THROWN_MESSAGE);
        }
        try {
            unregisterReceiver(mWifiReceiver);
            unregisterReceiver(mGPSStateReceiver);
        } catch (IllegalArgumentException illegalArgumentException) {
            Log.d(ILLEGAL_ARGUMENT_EXCEPTION_THROWN_TAG, ILLEGAL_ARGUMENT_EXCEPTION_THROWN_MESSAGE);
        }
        mWifiLocationRepository.closeFileOutputStream();
    }

    private void startUpdateUIThread() {
        UIUpdateTask uiUpdateTask = new UIUpdateTask(tv);

        new Thread(uiUpdateTask).start();
    }
}