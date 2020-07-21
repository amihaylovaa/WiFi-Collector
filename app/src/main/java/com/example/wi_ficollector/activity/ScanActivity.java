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
import androidx.annotation.RequiresApi;
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

@RequiresApi(api = Build.VERSION_CODES.Q)
public class ScanActivity extends AppCompatActivity implements
        GPSRequirementDialogFragment.GPSDialogListener,
        BackgroundPermissionDialogFragment.BackgroundPermissionRationaleListener {

    private static final String FINE_LOCATION_PERMISSION;
    private static final String BACKGROUND_LOCATION_PERMISSION;
    private static final String FOREGROUND_SERVICE_KEY;
    private static final String ANDROID_GPS_DIALOG_SHOWN_KEY;
    private static final String ANDROID_BACKGROUND_PERMISSION_DIALOG_SHOWN_KEY;
    private boolean isBackgroundPermissionGranted;
    private boolean isAndroidBackgroundPermissionRequestShown;
    private boolean isServiceStarted;
    private boolean isAndroidGPSDialogShown;
    private DialogFragment mGPSRequirementsDialogFragment;
    private DialogFragment mBackgroundPermissionDialogFragment;
    private ScanPreference mScanPreference;
    private ResolvableApiException mResolvableApiException;
    private Intent mIntent;
    private TextView tv;
    private BroadcastReceiver mUIUpdateReceiver;
    private FragmentManager mFragmentManager;
    private LocalBroadcastManager mLocalBroadcastManager;

    static {
        FINE_LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION;
        BACKGROUND_LOCATION_PERMISSION = Manifest.permission.ACCESS_BACKGROUND_LOCATION;
        FOREGROUND_SERVICE_KEY = "FOREGROUND_SERVICE";
        ANDROID_GPS_DIALOG_SHOWN_KEY = "ANDROID_GPS_DIALOG_SHOWN";
        ANDROID_BACKGROUND_PERMISSION_DIALOG_SHOWN_KEY = "ANDROID_BACKGROUND_PERMISSION_DIALOG_SHOWN";
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        initializeFields();

        if (mScanPreference.isActivityFirstTimeLaunched() && IS_BACKGROUND_PERMISSION_REQUIRED) {
            mScanPreference.addBackgroundPermissionRationaleKey();
        }
        if (savedInstanceState != null) {
            restoreState(savedInstanceState);
        }
        enableGPS();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mGPSRequirementsDialogFragment != null && mGPSRequirementsDialogFragment.isAdded()) {
            mFragmentManager.putFragment(outState, GPS_DIALOG_TAG, mGPSRequirementsDialogFragment);
        }
        if (mBackgroundPermissionDialogFragment != null && mBackgroundPermissionDialogFragment.isAdded()) {
            mFragmentManager.putFragment(outState, BACKGROUND_PERMISSION_DIALOG, mBackgroundPermissionDialogFragment);
        }
        outState.putBoolean(FOREGROUND_SERVICE_KEY, isServiceStarted);
        outState.putBoolean(ANDROID_GPS_DIALOG_SHOWN_KEY, isAndroidGPSDialogShown);
        outState.putBoolean(ANDROID_BACKGROUND_PERMISSION_DIALOG_SHOWN_KEY, isAndroidBackgroundPermissionRequestShown);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        enableGPS();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (IS_BACKGROUND_PERMISSION_REQUIRED && !isBackgroundPermissionGranted) {
            stopForegroundService();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LOCATION_SETTINGS_CODE && resultCode == Activity.RESULT_OK) {
            requestLocationPermission();
        }
        if (requestCode == LOCATION_SETTINGS_CODE && resultCode == Activity.RESULT_CANCELED) {
            mGPSRequirementsDialogFragment = null;
            isAndroidGPSDialogShown = false;
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
                mResolvableApiException.startResolutionForResult(this, LOCATION_SETTINGS_CODE);
                isAndroidGPSDialogShown = true;
            }
        } catch (IntentSender.SendIntentException sendEx) {
            Log.d(SEND_INTENT_EXCEPTION_THROWN_TAG, SEND_INTENT_EXCEPTION_EXCEPTION_THROWN_MSG);
        }
    }

    @Override
    public void showRationale() {
        isAndroidBackgroundPermissionRequestShown = true;
        requestBackgroundLocationPermission();
    }

    private void handleFineLocationPermissionGranted() {
        if (IS_BACKGROUND_PERMISSION_REQUIRED) {
            requestBackgroundPermission();
        }
        if (!isServiceStarted) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(this, mIntent);
            } else {
                startService(mIntent);
            }
            isServiceStarted = true;
            mLocalBroadcastManager.registerReceiver(mUIUpdateReceiver, new IntentFilter("UI_UPDATE"));
        }
    }

    private void handleBackgroundPermissionRequestResult(int grantResult, String permission) {
        if (grantResult == PackageManager.PERMISSION_GRANTED) {
            isBackgroundPermissionGranted = true;
        } else {
            if (!shouldShowRequestPermissionRationale(permission)) {
                mScanPreference.stopShowBackgroundPermissionRequestRationale();
            }
            isAndroidBackgroundPermissionRequestShown = false;
            mBackgroundPermissionDialogFragment = null;
        }
    }

    public void enableGPS() {
        LocationRequest locationRequest = createLocationRequest();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnFailureListener(this, e -> {
            if (e instanceof ResolvableApiException) {
                mResolvableApiException = (ResolvableApiException) e;
                showGPSRequirements();
            }
        }).addOnSuccessListener(e -> requestLocationPermission());
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

    public void requestBackgroundLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{BACKGROUND_LOCATION_PERMISSION},
                BACKGROUND_LOCATION_PERMISSION_CODE);
    }

    public boolean isFineLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission
                (this, FINE_LOCATION_PERMISSION) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean isBackgroundLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(
                this, BACKGROUND_LOCATION_PERMISSION) == PackageManager.PERMISSION_GRANTED;
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
        if (!isAndroidBackgroundPermissionRequestShown && mBackgroundPermissionDialogFragment == null) {
            mBackgroundPermissionDialogFragment = BackgroundPermissionDialogFragment.newInstance();

            mBackgroundPermissionDialogFragment.setCancelable(false);
            mBackgroundPermissionDialogFragment.show(mFragmentManager, BACKGROUND_PERMISSION_DIALOG);
        }
    }

    private void showGPSRequirements() {
        if (!isAndroidGPSDialogShown && mGPSRequirementsDialogFragment == null) {
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
        isServiceStarted = savedInstanceState.getBoolean(FOREGROUND_SERVICE_KEY);
        isAndroidGPSDialogShown = savedInstanceState.getBoolean(ANDROID_GPS_DIALOG_SHOWN_KEY);
        isAndroidBackgroundPermissionRequestShown = savedInstanceState.getBoolean(ANDROID_BACKGROUND_PERMISSION_DIALOG_SHOWN_KEY);

        mGPSRequirementsDialogFragment = (GPSRequirementDialogFragment) mFragmentManager
                .getFragment(savedInstanceState, GPS_DIALOG_TAG);

        mBackgroundPermissionDialogFragment = (BackgroundPermissionDialogFragment) mFragmentManager
                .getFragment(savedInstanceState, BACKGROUND_PERMISSION_DIALOG);
    }

    private void initializeFields() {
        mFragmentManager = getSupportFragmentManager();
        isBackgroundPermissionGranted = false;
        mScanPreference = new ScanPreference(this);
        mIntent = new Intent(this, ForegroundWifiLocationService.class);
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        tv = findViewById(R.id.numberOfWifiNetworks);
        mUIUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                startUpdateUIThread();
            }
        };

        tv.setText(String.valueOf(numOfWifiNetworks));
    }

    private void stopForegroundService() {
        if (isServiceStarted) {
            stopService(mIntent);
            isServiceStarted = false;
            try {
                mLocalBroadcastManager.unregisterReceiver(mUIUpdateReceiver);
            } catch (IllegalArgumentException illegalArgumentException) {
                Log.d(ILLEGAL_ARGUMENT_EXCEPTION_THROWN_TAG, ILLEGAL_ARGUMENT_EXCEPTION_THROWN_MSG);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!isChangingConfigurations()) {
            stopForegroundService();
        }
    }
}