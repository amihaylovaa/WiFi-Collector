package com.example.wi_ficollector.utils;

import android.Manifest;

public final class Constants {

    private Constants() {
    }

    public static int numberFoundWifiNetworks = 0;
    public static final double ZERO = 0.00;
    public static final String TRACK_POINTS_FILE_NAME = "TrackPoints.gpx";
    public static final String FILE_NOT_FOUND_EXCEPTION_TAG = "File not found exception thrown";
    public static final String FILE_NOT_FOUND_EXCEPTION_MESSAGE = "File does not exist,  but is going to be created";
    public static final String IO_EXCEPTION_THROWN_TAG = "IO exception thrown";
    public static final String IO_EXCEPTION_THROWN_MESSAGE = "IO exception has been thrown during working with file stream";
    public static final String ACCESS_FINE_LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String ACCESS_BACKGROUND_LOCATION_PERMISSION = Manifest.permission.ACCESS_BACKGROUND_LOCATION;
    public static final int FIVE_SECONDS = 5000;
    public static final int THREE_SECONDS = 3000;
    public static final int TEN_SECONDS = 10_000;
    public static final String GPX_TAG = "gpx";
    public static final String TRACK_TAG = "trk";
    public static final String TRACK_SEGMENT_TAG = "trkseg";
    public static final String TRACK_POINT_TAG = "trkpt";
    public static final String NO_NAMESPACE = "";
    public static final String ENCODING = "UTF-8";
    public static final String TIME_TAG = "time";
    public static final String LATITUDE = "lat";
    public static final String LONGITUDE = "lon";
    public static final String EXTENSIONS_TAG = "extensions";
    public static final String WIFI_TAG = "wap";
    public static final String BSSID_TAG = "BSSID";
    public static final String SSID_TAG = "SSID";
    public static final String RSSI_TAG = "RSSI";
    public static final String FREQUENCY_TAG = "frequency";
    public static final String FILE_NAME = "Location_Wifi.gpx";
    public static final String XMLNS_ATTRIBUTE = "xmlns";
    public static final String GPX_NAMESPACE = "http://www.topografix.com/GPX/1/1";
    public static final String XSI_SCHEMA_LOCATION = "xsi:schemaLocation";
    public static final String SCHEMA_LOCATION = "http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd";
    public static final String XMLNS_XSI = "xmlns:xsi";
    public static final String XML_INSTANCE = "http://www.w3.org/2001/XMLSchema-instance";
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
