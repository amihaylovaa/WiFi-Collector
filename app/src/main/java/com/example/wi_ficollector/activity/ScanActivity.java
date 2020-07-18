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
import com.example.wi_ficollector.dialog_fragment.BackgroundPermissionDialogFragment;
import com.example.wi_ficollector.dialog_fragment.GPSRequirementDialogFragment;
import com.example.wi_ficollector.preference.ScanPreference;

import com.example.wi_ficollector.service.ForegroundWifiLocationService;
import com.example.wi_ficollector.thread.*;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.*;
import com.google.android.gms.tasks.Task;


import static com.example.wi_ficollector.utils.Constants.*;
// todo fix rotation issues
public class ScanActivity extends AppCompatActivity implements
        GPSRequirementDialogFragment.GPSDialogListener,
        BackgroundPermissionDialogFragment.BackgroundPermissionRationaleListener {

    private static final String ACCESS_FINE_LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String ACCESS_BACKGROUND_LOCATION_PERMISSION = Manifest.permission.ACCESS_BACKGROUND_LOCATION;
    private LocationRequest mLocationRequest;
    private DialogFragment mGPSRequirementsDialogFragment;
    private DialogFragment mBackgroundPermissionDialogFragment;
    private ScanPreference mScanPreference;
    private ResolvableApiException mResolvableApiException;
    private Intent mIntent;
    private TextView tv;
    private boolean isBackgroundPermissionGranted;
    private BroadcastReceiver UIUpdate;
    private LocalBroadcastManager mLocalBroadcastManager;
    private boolean isServiceStarted;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        initializeFields();
        Log.d("ON", "CREATE");
        UIUpdate = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                startUpdateUIThread();
            }
        };

        if (mScanPreference.isActivityFirstTimeLaunched()) {
            mScanPreference.addBackgroundPermissionRationaleKey();
        }

        if (savedInstanceState != null) {
            restoreState(savedInstanceState);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        FragmentManager mFragmentManager = getSupportFragmentManager();
        if (mGPSRequirementsDialogFragment != null && mGPSRequirementsDialogFragment.isAdded()) {
            mFragmentManager.putFragment(outState, GPS_DIALOG_TAG, mGPSRequirementsDialogFragment);
        }
        if (mBackgroundPermissionDialogFragment != null && mBackgroundPermissionDialogFragment.isAdded()) {
            mFragmentManager.putFragment(outState, BACKGROUND_PERMISSION_DIALOG, mBackgroundPermissionDialogFragment);
        }
        outState.putBoolean("FOREGROUND_SERVICE", isServiceStarted);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("ON", "START");
        enableGPS();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (IS_BACKGROUND_PERMISSION_REQUEST_REQUIRED && !isBackgroundPermissionGranted) {
            stopService(mIntent);
            isServiceStarted = false;
            mLocalBroadcastManager.unregisterReceiver(UIUpdate);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_LOCATION_SETTINGS_CODE && resultCode == Activity.RESULT_OK) {
            requestLocationPermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, @NonNull int[] grantResults) {
        for (int i = 0; i < permissions.length; i++) {
            switch (requestCode) {
                case FINE_LOCATION_PERMISSION_CODE:
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        handleFineLocationPermissionGranted();
                    }
                    break;
                case BACKGROUND_LOCATION_PERMISSION_CODE:
                    handleBackgroundPermissionRequestResult(grantResults[i], permissions[i]);
                    break;
                default:
                    Log.d(UNRECOGNIZED_PERMISSION_TAG, UNRECOGNIZED_PERMISSION_MSG);
            }
        }
    }

    @Override
    public void startResolution() {
        try {
            if (mResolvableApiException != null) {
                mResolvableApiException.startResolutionForResult(this, REQUEST_LOCATION_SETTINGS_CODE);
            }
        } catch (IntentSender.SendIntentException sendEx) {
            Log.d(SEND_INTENT_EXCEPTION_THROWN_TAG, SEND_INTENT_EXCEPTION_EXCEPTION_THROWN_MSG);
        }
    }

    @Override
    public void showRationale() {
        requestBackgroundLocationPermission();
    }

    private void handleFineLocationPermissionGranted() {
        if (IS_BACKGROUND_PERMISSION_REQUEST_REQUIRED) {
            requestBackgroundPermission();
        }
        if (!isServiceStarted) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(this, mIntent);
            } else {
                startService(mIntent);
            }
            isServiceStarted = true;
        }
        mLocalBroadcastManager.registerReceiver(UIUpdate, new IntentFilter("UI_UPDATE"));
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

    public void enableGPS() {
        createLocationRequest();

        LocationSettingsRequest.Builder builder = createLocationSettingsRequest();
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnFailureListener(this, e -> {
            if (e instanceof ResolvableApiException) {
                mResolvableApiException = (ResolvableApiException) e;
                showGPSRequirements();
            }
        }).addOnSuccessListener(e -> requestLocationPermission());
    }


    public void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        mLocationRequest.setInterval(THREE_SECONDS);
        mLocationRequest.setFastestInterval(FIVE_SECONDS);
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
            handleFineLocationPermissionGranted();
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
        if (mBackgroundPermissionDialogFragment == null) {
            FragmentManager mFragmentManager = getSupportFragmentManager();
            mBackgroundPermissionDialogFragment = BackgroundPermissionDialogFragment.newInstance();

            mBackgroundPermissionDialogFragment.setCancelable(false);
            mBackgroundPermissionDialogFragment.show(mFragmentManager, BACKGROUND_PERMISSION_DIALOG);
        }
    }

    private void showGPSRequirements() {
        if (mGPSRequirementsDialogFragment == null) {
            FragmentManager mFragmentManager = getSupportFragmentManager();
            mGPSRequirementsDialogFragment = GPSRequirementDialogFragment.newInstance();

            mGPSRequirementsDialogFragment.setCancelable(false);
            mGPSRequirementsDialogFragment.show(mFragmentManager, GPS_DIALOG_TAG);
        }
    }

    private void startUpdateUIThread() {
        UIUpdateTask uiUpdateTask = new UIUpdateTask(tv);

        new Thread(uiUpdateTask).start();
    }

    private void restoreState(Bundle savedInstanceState) {
        FragmentManager mFragmentManager = getSupportFragmentManager();
        mGPSRequirementsDialogFragment = (DialogFragment) mFragmentManager
                .getFragment(savedInstanceState, GPS_DIALOG_TAG);
        mBackgroundPermissionDialogFragment = (DialogFragment) mFragmentManager
                .getFragment(savedInstanceState, BACKGROUND_PERMISSION_DIALOG);
        isServiceStarted = savedInstanceState.getBoolean("FOREGROUND_SERVICE");
    }

    private void initializeFields() {
        isBackgroundPermissionGranted = false;
        mScanPreference = new ScanPreference(this);
        mIntent = new Intent(this, ForegroundWifiLocationService.class);
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        tv = findViewById(R.id.numberOfWifiNetworks);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            mLocalBroadcastManager.unregisterReceiver(UIUpdate);
        } catch (IllegalArgumentException illegalArgumentException) {
            Log.d(ILLEGAL_ARGUMENT_EXCEPTION_THROWN_TAG, ILLEGAL_ARGUMENT_EXCEPTION_THROWN_MESSAGE);
        }
    }
}