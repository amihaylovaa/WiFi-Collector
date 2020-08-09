package com.example.wi_ficollector.activity;

import android.Manifest;
import android.app.Activity;
import android.content.*;
import android.content.pm.PackageManager;

import android.os.*;

import android.util.Log;
import android.widget.TextView;

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

import com.example.wi_ficollector.service.ForegroundWifiLocationService;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.*;
import com.google.android.gms.tasks.Task;

import static com.example.wi_ficollector.utility.Constants.*;

public class ScanActivity extends AppCompatActivity implements
        GPSRequirementDialogFragment.GPSRequirementsListener,
        LocationRequestRationaleDialogFragment.LocationRequestRationaleListener {

    private static final String FINE_LOCATION_PERMISSION;
    private static final String ANDROID_GPS_DIALOG_SHOWN_KEY;
    private static final String ANDROID_LOCATION_PERMISSION_DIALOG_SHOWN_KEY;
    private boolean isLocationRequestDialogShown;
    private boolean isGPSRequestDialogShown;
    private boolean isServiceStarted;
    private DialogFragment mGPSRequirementsDialogFragment;
    private DialogFragment mLocationPermissionDialogFragment;
    private ResolvableApiException mResolvableApiException;
    private Intent mIntent;
    private TextView tv;
    private BroadcastReceiver mUIUpdateReceiver;
    private FragmentManager mFragmentManager;
    private LocalBroadcastManager mLocalBroadcastManager;

    static {
        FINE_LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION;
        ANDROID_GPS_DIALOG_SHOWN_KEY = "ANDROID_GPS_DIALOG_SHOWN";
        ANDROID_LOCATION_PERMISSION_DIALOG_SHOWN_KEY = "ANDROID_BACKGROUND_PERMISSION_DIALOG_SHOWN";
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        initializeFields();
        implementUIUpdateReceiver();

        if (savedInstanceState != null) {
            restorePreviousState(savedInstanceState);
        }
        enableGPS();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mGPSRequirementsDialogFragment != null && mGPSRequirementsDialogFragment.isAdded()) {
            mFragmentManager.putFragment(outState, GPS_DIALOG_TAG, mGPSRequirementsDialogFragment);
        }
        if (mLocationPermissionDialogFragment != null && mLocationPermissionDialogFragment.isAdded()) {
            mFragmentManager.putFragment(outState, LOCATION_PERMISSION_DIALOG_TAG, mLocationPermissionDialogFragment);
        }
        outState.putBoolean(ANDROID_GPS_DIALOG_SHOWN_KEY, isGPSRequestDialogShown);
        outState.putBoolean(ANDROID_LOCATION_PERMISSION_DIALOG_SHOWN_KEY, isLocationRequestDialogShown);
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
                isLocationRequestDialogShown = true;
                handleFineLocationPermissionRequestResults(permissions[i], grantResults[i]);
            }
        }
    }

    private void handleFineLocationPermissionRequestResults(String permission, int grantResult) {
        if (grantResult == PackageManager.PERMISSION_GRANTED) {
            startForegroundService();
        } else {
            isLocationRequestDialogShown = false;
            if (shouldShowRequestPermissionRationale(permission)) {
                showLocationPermissionRequestRationale();
            }
        }
    }

    @Override
    public void startGPSResolution() {
        try {
            if (mResolvableApiException != null) {
                mResolvableApiException.startResolutionForResult(this, LOCATION_SETTINGS_CODE);
                isGPSRequestDialogShown = true;
            }
        } catch (IntentSender.SendIntentException sendEx) {
            Log.d(SEND_INTENT_EXCEPTION_THROWN_TAG, SEND_INTENT_EXCEPTION_EXCEPTION_THROWN_MSG);
        }
    }

    @Override
    public void positiveButton() {
        requestFineLocationPermission();
    }

    @Override
    public void negativeButton() {
        mLocationPermissionDialogFragment = null;
        isLocationRequestDialogShown = false;
    }

    private void startForegroundService() {
        if (!isServiceStarted) {
            isServiceStarted = true;
            IntentFilter intentFilter = new IntentFilter(ACTION);
            mLocalBroadcastManager.registerReceiver(mUIUpdateReceiver, intentFilter);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(this, mIntent);
            } else {
                startService(mIntent);
            }
        }
    }

    public void enableGPS() {
        LocationRequest locationRequest = createLocationRequest();
        LocationSettingsRequest locationSettingsRequest = createLocationSettingsRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(locationSettingsRequest);

        task.addOnFailureListener(this, e ->
        {
            if (e instanceof ResolvableApiException) {
                mResolvableApiException = (ResolvableApiException) e;
                showGPSRequirements();
            }
        }).addOnSuccessListener(e -> requestLocationPermission());
    }

    private LocationSettingsRequest createLocationSettingsRequest(LocationRequest locationRequest) {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);

        return builder.build();
    }

    private LocationRequest createLocationRequest() {
        return new LocationRequest()
                .setFastestInterval(THREE_SECONDS)
                .setInterval(FIVE_SECONDS)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void requestFineLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{FINE_LOCATION_PERMISSION},
                FINE_LOCATION_PERMISSION_CODE);
    }

    public boolean isFineLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission
                (this, FINE_LOCATION_PERMISSION) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestLocationPermission() {
        if (!isFineLocationPermissionGranted()) {
            if (!isLocationRequestDialogShown) {
                requestFineLocationPermission();
            }
        } else {
            startForegroundService();
        }
    }

    public void showLocationPermissionRequestRationale() {
        if (!isLocationRequestDialogShown && mLocationPermissionDialogFragment == null) {
            mLocationPermissionDialogFragment = LocationRequestRationaleDialogFragment.newInstance();

            mLocationPermissionDialogFragment.setCancelable(false);
            mLocationPermissionDialogFragment.show(mFragmentManager, LOCATION_PERMISSION_DIALOG_TAG);
            isLocationRequestDialogShown = true;
        }
    }

    private void showGPSRequirements() {
        if (!isGPSRequestDialogShown && mGPSRequirementsDialogFragment == null) {
            mGPSRequirementsDialogFragment = GPSRequirementDialogFragment.newInstance();

            mGPSRequirementsDialogFragment.setCancelable(false);
            mGPSRequirementsDialogFragment.show(mFragmentManager, GPS_DIALOG_TAG);
        }
    }

    private void restorePreviousState(Bundle savedInstanceState) {
        isGPSRequestDialogShown = savedInstanceState.getBoolean(ANDROID_GPS_DIALOG_SHOWN_KEY);
        isLocationRequestDialogShown = savedInstanceState.getBoolean(ANDROID_LOCATION_PERMISSION_DIALOG_SHOWN_KEY);

        mGPSRequirementsDialogFragment = (GPSRequirementDialogFragment) mFragmentManager
                .getFragment(savedInstanceState, GPS_DIALOG_TAG);

        mLocationPermissionDialogFragment = (LocationRequestRationaleDialogFragment) mFragmentManager
                .getFragment(savedInstanceState, LOCATION_PERMISSION_DIALOG_TAG);
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

    private void initializeFields() {
        mFragmentManager = getSupportFragmentManager();
        mIntent = new Intent(this, ForegroundWifiLocationService.class);
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        tv = findViewById(R.id.numberOfWifiNetworks);
    }

    private void stopForegroundService() {
        stopService(mIntent);
        isServiceStarted = false;
        try {
            mLocalBroadcastManager.unregisterReceiver(mUIUpdateReceiver);
        } catch (IllegalArgumentException illegalArgumentException) {
            Log.d(ILLEGAL_ARGUMENT_EXCEPTION_THROWN_TAG, ILLEGAL_ARGUMENT_EXCEPTION_THROWN_MSG);
        }
    }
}