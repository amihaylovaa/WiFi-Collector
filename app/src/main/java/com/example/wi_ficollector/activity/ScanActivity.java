package com.example.wi_ficollector.activity;

import android.Manifest;
import android.app.Activity;
import android.content.*;
import android.content.pm.PackageManager;

import android.os.*;

import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.wi_ficollector.R;
import com.example.wi_ficollector.dialogfragment.LocationRequestRationaleDialogFragment;
import com.example.wi_ficollector.dialogfragment.GPSRequirementDialogFragment;

import com.example.wi_ficollector.listener.GPSRequirementsListener;
import com.example.wi_ficollector.listener.LocationPermissionRequestRationaleListener;
import com.example.wi_ficollector.service.ForegroundWifiLocationService;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.*;
import com.google.android.gms.tasks.Task;

import static com.example.wi_ficollector.utility.Constants.*;

public class ScanActivity extends AppCompatActivity implements GPSRequirementsListener, LocationPermissionRequestRationaleListener {

    private boolean isLocationRequestRationaleDialogShown;
    private boolean isGPSRequestDialogShown;
    private boolean isServiceStarted;
    private LocationRequest mLocationRequest;
    private DialogFragment mGPSRequirementsDialogFragment;
    private DialogFragment mLocationPermissionDialogFragment;
    private ResolvableApiException mResolvableApiException;
    private Intent mIntent;
    private TextView tv;
    private BroadcastReceiver mUIUpdateReceiver;
    private FragmentManager mFragmentManager;
    private LocalBroadcastManager mLocalBroadcastManager;
    private static final int FINE_LOCATION_PERMISSION_CODE;
    private static final int LOCATION_SETTINGS_CODE;
    private static final String FINE_LOCATION_PERMISSION;
    private static final String ANDROID_GPS_DIALOG_SHOWN_KEY;
    private static final String ANDROID_LOCATION_PERMISSION_DIALOG_SHOWN_KEY;
    private static final String ANDROID_SERVICE_STARTED_KEY;
    private static final String GPS_REQUIREMENTS_DIALOG_TAG;

    static {
        FINE_LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION;
        FINE_LOCATION_PERMISSION_CODE = 87;
        LOCATION_SETTINGS_CODE = 104;
        ANDROID_GPS_DIALOG_SHOWN_KEY = "ANDROID_GPS_DIALOG_SHOWN";
        ANDROID_LOCATION_PERMISSION_DIALOG_SHOWN_KEY = "ANDROID_BACKGROUND_PERMISSION_DIALOG_SHOWN";
        ANDROID_SERVICE_STARTED_KEY = "ANDROID_SERVICE_STARTED";
        GPS_REQUIREMENTS_DIALOG_TAG = "GPS requirements dialog";
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        isLocationRequestRationaleDialogShown = false;
        isGPSRequestDialogShown = false;
        isServiceStarted = false;
        mFragmentManager = getSupportFragmentManager();
        mIntent = new Intent(ScanActivity.this, ForegroundWifiLocationService.class);
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(ScanActivity.this);
        mLocationRequest = createLocationRequest();
        tv = findViewById(R.id.numberOfWifiNetworks);

        implementUIUpdateReceiver();

        if (savedInstanceState != null) {
            restorePreviousInstanceState(savedInstanceState);
        }
        enableGPS();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        enableGPS();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!isChangingConfigurations()) {
            stopForegroundService();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mGPSRequirementsDialogFragment != null && mGPSRequirementsDialogFragment.isAdded()) {
            mFragmentManager.putFragment(outState, GPS_REQUIREMENTS_DIALOG_TAG, mGPSRequirementsDialogFragment);
        }
        if (mLocationPermissionDialogFragment != null && mLocationPermissionDialogFragment.isAdded()) {
            mFragmentManager.putFragment(outState, LOCATION_PERMISSION_DIALOG_TAG, mLocationPermissionDialogFragment);
        }
        outState.putBoolean(ANDROID_GPS_DIALOG_SHOWN_KEY, isGPSRequestDialogShown);
        outState.putBoolean(ANDROID_LOCATION_PERMISSION_DIALOG_SHOWN_KEY, isLocationRequestRationaleDialogShown);
        outState.putBoolean(ANDROID_SERVICE_STARTED_KEY, isServiceStarted);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LOCATION_SETTINGS_CODE && resultCode == Activity.RESULT_OK) {
            requestLocationPermission();
        }
        mGPSRequirementsDialogFragment = null;
        isGPSRequestDialogShown = false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, @NonNull int[] grantResults) {
        for (int i = 0; i < permissions.length; i++) {
            if (requestCode == FINE_LOCATION_PERMISSION_CODE) {
                isLocationRequestRationaleDialogShown = true;
                handleFineLocationPermissionRequestResult(permissions[i], grantResults[i]);
            }
        }
    }

    @Override
    public void startGPSRequirementsResolution() {
        try {
            if (mResolvableApiException != null) {
                mResolvableApiException.startResolutionForResult(ScanActivity.this, LOCATION_SETTINGS_CODE);
                isGPSRequestDialogShown = true;
            }
        } catch (IntentSender.SendIntentException sendEx) {
            Toast.makeText(ScanActivity.this, R.string.send_intent_exception, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void agree() {
        requestFineLocationPermission();
    }

    @Override
    public void disagree() {
        mLocationPermissionDialogFragment = null;
        isLocationRequestRationaleDialogShown = false;
    }

    public void handleFineLocationPermissionRequestResult(String permission, int grantResult) {
        if (grantResult == PackageManager.PERMISSION_GRANTED) {
            startForegroundService();
        } else {
            isLocationRequestRationaleDialogShown = false;
            if (shouldShowRequestPermissionRationale(permission)) {
                showLocationPermissionRequestRationale();
            }
        }
    }

    public void startForegroundService() {
        if (!isServiceStarted) {
            IntentFilter intentFilter = new IntentFilter(ACTION);
            mLocalBroadcastManager.registerReceiver(mUIUpdateReceiver, intentFilter);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(ScanActivity.this, mIntent);
            } else {
                startService(mIntent);
            }
            isServiceStarted = true;
        }
    }

    public void stopForegroundService() {
        stopService(mIntent);
        isServiceStarted = false;

        try {
            mLocalBroadcastManager.unregisterReceiver(mUIUpdateReceiver);
        } catch (IllegalArgumentException illegalArgumentException) {
            Log.d(ILLEGAL_ARGUMENT_EXCEPTION_THROWN_TAG, ILLEGAL_ARGUMENT_EXCEPTION_THROWN_MSG);
        }
    }

    public void enableGPS() {
        LocationSettingsRequest locationSettingsRequest = createLocationSettingsRequest();
        SettingsClient client = LocationServices.getSettingsClient(ScanActivity.this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(locationSettingsRequest);

        task.addOnFailureListener(ScanActivity.this, e ->
        {
            if (e instanceof ResolvableApiException) {
                mResolvableApiException = (ResolvableApiException) e;
                showGPSRequirementsRationale();
            }
        }).addOnSuccessListener(e -> requestLocationPermission());
    }

    public void requestFineLocationPermission() {
        ActivityCompat.requestPermissions(ScanActivity.this, new String[]{FINE_LOCATION_PERMISSION},
                FINE_LOCATION_PERMISSION_CODE);
    }

    public boolean isFineLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(ScanActivity.this,
                FINE_LOCATION_PERMISSION) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestLocationPermission() {
        if (isFineLocationPermissionGranted()) {
            startForegroundService();
        } else {
            if (!isLocationRequestRationaleDialogShown) {
                requestFineLocationPermission();
            }
        }
    }

    public void showLocationPermissionRequestRationale() {
        if (!isLocationRequestRationaleDialogShown && mLocationPermissionDialogFragment == null) {
            mLocationPermissionDialogFragment = LocationRequestRationaleDialogFragment.newInstance();

            mLocationPermissionDialogFragment.setCancelable(false);
            mLocationPermissionDialogFragment.show(mFragmentManager, LOCATION_PERMISSION_DIALOG_TAG);
            isLocationRequestRationaleDialogShown = true;
        }
    }

    public void showGPSRequirementsRationale() {
        if (!isGPSRequestDialogShown && mGPSRequirementsDialogFragment == null) {
            mGPSRequirementsDialogFragment = GPSRequirementDialogFragment.newInstance();

            mGPSRequirementsDialogFragment.setCancelable(false);
            mGPSRequirementsDialogFragment.show(mFragmentManager, GPS_REQUIREMENTS_DIALOG_TAG);
        }
    }

    public void restorePreviousInstanceState(Bundle savedInstanceState) {
        isGPSRequestDialogShown = savedInstanceState.getBoolean(ANDROID_GPS_DIALOG_SHOWN_KEY);
        isLocationRequestRationaleDialogShown = savedInstanceState.getBoolean(ANDROID_LOCATION_PERMISSION_DIALOG_SHOWN_KEY);
        isServiceStarted = savedInstanceState.getBoolean(ANDROID_SERVICE_STARTED_KEY);

        mGPSRequirementsDialogFragment = (GPSRequirementDialogFragment) mFragmentManager
                .getFragment(savedInstanceState, GPS_REQUIREMENTS_DIALOG_TAG);

        mLocationPermissionDialogFragment = (LocationRequestRationaleDialogFragment) mFragmentManager
                .getFragment(savedInstanceState, LOCATION_PERMISSION_DIALOG_TAG);
    }

    private LocationSettingsRequest createLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();

        return builder
                .addLocationRequest(mLocationRequest)
                .build();
    }

    private LocationRequest createLocationRequest() {
        return new LocationRequest()
                .setFastestInterval(THREE_SECONDS)
                .setInterval(FIVE_SECONDS)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void implementUIUpdateReceiver() {
        mUIUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int numOfWifiNetworks = intent.getIntExtra(EXTRA_NAME, 0);

                tv.invalidate();
                tv.setText(String.valueOf(numOfWifiNetworks));
            }
        };
    }
}