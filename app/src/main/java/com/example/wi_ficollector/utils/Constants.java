package com.example.wi_ficollector.utils;

import android.location.LocationManager;
import android.net.wifi.WifiManager;


public final class Constants {

    private Constants() {
    }

    public static final int FINE_LOCATION_PERMISSION_CODE = 87;
    public static final int LOCATION_SETTINGS_CODE = 104;
    public static final int FIVE_SECONDS = 5000;
    public static final int THREE_SECONDS = 3000;
    public static final double ZERO = 0.00;
    public static final String GPS_DIALOG_TAG = "GPS Dialog";
    public static final String ACTION = "UI_UPDATE";
    public static final String EXTRA_NAME = "WAP COUNT";
    public static final String LOCATION_PERMISSION_DIALOG_TAG = "Background permission dialog";
    public static final String SCAN_RESULTS_AVAILABLE_ACTION = WifiManager.SCAN_RESULTS_AVAILABLE_ACTION;
    public static final String PROVIDERS_CHANGED_ACTION = LocationManager.PROVIDERS_CHANGED_ACTION;
    public static final String FILE_NOT_FOUND_EXCEPTION_TAG = "File not found exception thrown";
    public static final String FILE_NOT_FOUND_EXCEPTION_MSG = "File does not exist,  but is going to be created";
    public static final String ILLEGAL_ARGUMENT_EXCEPTION_THROWN_TAG = "Illegal argument exception thrown";
    public static final String ILLEGAL_ARGUMENT_EXCEPTION_THROWN_MSG = "Broadcast receiver has not been registered";
    public static final String NULL_POINTER_EXCEPTION_THROWN_TAG = "Null pointer exception thrown";
    public static final String NULL_POINTER_EXCEPTION_THROWN_MESSAGE = "Listener has not been registered and is null";
    public static final String IO_EXCEPTION_THROWN_TAG = "IO exception thrown";
    public static final String SEND_INTENT_EXCEPTION_THROWN_TAG = "SendIntent exception thrown";
    public static final String SEND_INTENT_EXCEPTION_EXCEPTION_THROWN_MSG = "Request could not be executed";
    public static final String IO_EXCEPTION_THROWN_MESSAGE = "IO exception has been thrown during working with file stream";
    public static final String GPX_TAG = "gpx";
    public static final String TRACK_TAG = "trk";
    public static final String TRACK_SEGMENT_TAG = "trkseg";
    public static String TRACK_POINT_TAG = "trkpt";
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
}
