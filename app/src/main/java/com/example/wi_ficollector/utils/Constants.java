package com.example.wi_ficollector.utils;

import android.Manifest;

import java.util.concurrent.CountDownLatch;

public class Constants {
    private Constants() {
    }

    public static CountDownLatch countDownLatch;
    public static final String ACCESS_FINE_LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String ACCESS_BACKGROUND_LOCATION_PERMISSION = Manifest.permission.ACCESS_BACKGROUND_LOCATION;
    public static boolean isAlreadyScanned = true;
    public static boolean hasWorkFailed = false;
    public static final int ONE_MINUTE = 60 * 1000;
    public static final int TWO_MINUTES = 2 * 60 * 1000;
    public static final String FILE_NAME = "Location_Wifi.gpx";
    public static final int FINE_LOCATION_PERMISSION_CODE = 87;
    public static final int BACKGROUND_LOCATION_PERMISSION_CODE = 43;
    public static final int REQUEST_LOCATION_SETTINGS_CODE = 104;

    public static final String INTRO = "This application collect " +
            "your location and wireless access points based on your current location.\n" +
            "This data is stored on your device and is expected to be sent later.";

    public static final String BACKGROUND_PERMISSION_REQUEST_RATIONALE =
            "It is recommended to grant all time permission to access your location, " +
                    "otherwise application will not be able to work on background";

    public static final String GPS_REQUIREMENTS = "Turning GPS on is required in order to scan WiFi and find your location";
}
