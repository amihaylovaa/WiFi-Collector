package com.thesis.wificollector.utility;

import android.location.LocationManager;
import android.net.wifi.WifiManager;


public final class Constants {

    private Constants() {
    }

    public static final int NEGATIVE_INTEGER = -1;
    public static final int ZERO_INTEGER = 0;
    public static final int POSITIVE_INTEGER = 1;
    public static final int FIVE_SECONDS = 5000;
    public static final int THREE_SECONDS = 3000;
    public static final long MINIMUM_FILE_SIZE = 398L;
    public static final long LONG_ZERO = 0L;
    public static final String WIFI_TAG = "wap";
    public static final String ACTION = "UI_UPDATE";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String EXTRA_NAME = "WAP COUNT";
    public static final String GPX_TAG = "gpx";
    public static final String BSSID_TAG = "bssid";
    public static final String SSID_TAG = "ssid";
    public static final String RSSI_TAG = "rssi";
    public static final String CAPABILITIES_TAG = "capabilities";
    public static final String FREQUENCY_TAG = "frequency";
    public static final String TRACK_TAG = "trk";
    public static final String TRACK_SEGMENT_TAG = "trkseg";
    public static final String TRACK_POINT_TAG = "trkpt";
    public static final String EMPTY_STRING = "";
    public static final String ENCODING = "UTF-8";
    public static final String TIME_TAG = "time";
    public static final String LATITUDE_ATTRIBUTE = "lat";
    public static final String LONGITUDE_ATTRIBUTE = "lon";
    public static final String EXTENSIONS_TAG = "extensions";
    public static final String LOCATION_PERMISSION_DIALOG_TAG = "Background permission dialog";
    public static final String WIFI_SCAN_RESULTS = "wifiScanResults";
    public static final String SCAN_RESULTS_AVAILABLE_ACTION = WifiManager.SCAN_RESULTS_AVAILABLE_ACTION;
    public static final String PROVIDERS_CHANGED_ACTION = LocationManager.PROVIDERS_CHANGED_ACTION;
    public static final String DATE_TIME = "localDateTime";
    public static final String FILE_NAME = "Location_Wifi.gpx";
    public static final String XML_PULL_PARSER_EXCEPTION_TAG = "XML pull parser exception has been thrown";
    public static final String XML_PULL_PARSER_EXCEPTION_MESSAGE = "An exception occurred during working with xml pull parser";
    public static final String JSON_EXCEPTION_TAG = "JSON exception has been thrown";
    public static final String JSON_EXCEPTION_MESSAGE = "An exception occurred during working with JSON format";
    public static final String FILE_NOT_FOUND_EXCEPTION_TAG = "File not found exception thrown";
    public static final String FILE_NOT_FOUND_EXCEPTION_MSG = "File does not exist";
    public static final String FILE_NOT_FOUND_EXCEPTION_OUTPUT_MSG = "File does not exist but is being created";
    public static final String IO_EXCEPTION_INPUT_MSG = "File does not exist, operation could not be processed";
    public static final String ILLEGAL_ARGUMENT_EXCEPTION_THROWN_TAG = "Illegal argument exception thrown";
    public static final String ILLEGAL_ARGUMENT_EXCEPTION_THROWN_MSG = "Broadcast receiver has not been registered";
    public static final String IO_EXCEPTION_THROWN_TAG = "IO exception thrown";
    public static final String IO_EXCEPTION_THROWN_MESSAGE = "IO exception has been thrown during working with file stream";
}
