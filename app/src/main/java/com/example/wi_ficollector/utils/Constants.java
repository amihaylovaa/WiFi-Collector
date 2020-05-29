package com.example.wi_ficollector.utils;

import android.Manifest;
import android.content.Context;

import java.io.FileOutputStream;

public class Constants {
    private Constants() {
    }
    public static FileOutputStream fileOutputStream;
    public static final String ACCESS_FINE_LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String ACCESS_BACKGROUND_LOCATION_PERMISSION = Manifest.permission.ACCESS_BACKGROUND_LOCATION;
    public static boolean isAlreadyScanned = true;
    public static boolean hasWorkFailed = false;
    public static final int ONE_MINUTE = 1 * 60 * 1000;
    public static final int TWO_MINUTES = 2 * 60 * 1000;
    public static final String WORKER_REQUEST_TAG = "Background location worker ";
    public static final String FILE_NAME = "Location_Wifi.gpx";

    public static final String INTRO = "This application collect " +
            "your location and wireless access points based on your current location.\n" +
            "This data is stored on your device and is expected to be sent later.";

    public static final String BACKGROUND_PERMISSION_REQUEST_RATIONALE =
            "It is recommended to grant all time permission to access your location, " +
                    "otherwise application will not be able to work on background";

    public static final String GPS_REQUIREMENTS = "Turning GPS on is required in order to scan WiFi and find your location";
}
