package com.example.wi_ficollector.utility;

import android.Manifest;
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
    public static final String FINE_LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final int SERVER_ERROR_CODE = 503;
    public static final String PROTOCOL = "http";
    public static final String HOST = "xxx.xxx.x.xxx";
    public static final int PORT = 0;
    public static final String PATH = "/wifi/locations";
    public static final String REQUEST_METHOD = "POST";
    public static final String CONTENT_TYPE = "Content-type";
    public static final String ACCEPT="Accept";
    public static final String TYPE = "application/json";
    public static final String PROVIDERS_CHANGED = "android.location.PROVIDERS_CHANGED";
    public static final String ANDROID_GPS_DIALOG_SHOWN_KEY = "ANDROID_GPS_DIALOG_SHOWN";
    public static final String ANDROID_LOCATION_PERMISSION_DIALOG_SHOWN_KEY = "ANDROID_BACKGROUND_PERMISSION_DIALOG_SHOWN";
    public static final String ANDROID_SERVICE_STARTED_KEY = "ANDROID_SERVICE_STARTED";
    public static final String GPS_DIALOG_TAG = "GPS Dialog";
    public static final String INTRO_DIALOG_TAG = "Intro dialog";
    public static final String ACTION = "UI_UPDATE";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String EXTRA_NAME = "WAP COUNT";
    public static final String LOCATION_PERMISSION_DIALOG_TAG = "Background permission dialog";
    public static final String WIFI_SCAN_RESULTS = "wifiScanResults";
    public static final String SCAN_RESULTS_AVAILABLE_ACTION = WifiManager.SCAN_RESULTS_AVAILABLE_ACTION;
    public static final String PROVIDERS_CHANGED_ACTION = LocationManager.PROVIDERS_CHANGED_ACTION;
    public static final String DATE_TIME = "localDateTime";
    public static final String XML_PULL_PARSER_EXCEPTION_TAG = "XML pull parser exception has been thrown";
    public static final String XML_PULL_PARSER_EXCEPTION_MESSAGE = "An exception occurred during working with xml pull parser";
    public static final String JSON_EXCEPTION_TAG = "JSON exception has been thrown";
    public static final String JSON_EXCEPTION_MESSAGE = "An exception occurred during working with JSON format";
    public static final String FILE_NOT_FOUND_EXCEPTION_TAG = "File not found exception thrown";
    public static final String FILE_NOT_FOUND_EXCEPTION_MSG = "File does not exist";
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
    public static final String TRACK_POINT_TAG = "trkpt";
    public static final String NO_NAMESPACE = "";
    public static final String ENCODING = "UTF-8";
    public static final String TIME_TAG = "time";
    public static final String LATITUDE_ATTRIBUTE = "lat";
    public static final String LONGITUDE_ATTRIBUTE = "lon";
    public static final String EXTENSIONS_TAG = "extensions";
    public static final String WIFI_TAG = "wap";
    public static final String BSSID_TAG = "bssid";
    public static final String SSID_TAG = "ssid";
    public static final String RSSI_TAG = "rssi";
    public static final String CAPABILITIES_TAG = "capabilities";
    public static final String FREQUENCY_TAG = "frequency";
    public static final String FILE_NAME = "Location_Wifi.gpx";
    public static final String XMLNS_ATTRIBUTE = "xmlns";
    public static final String GPX_NAMESPACE = "http://www.topografix.com/GPX/1/1";
    public static final String XSI_SCHEMA_LOCATION = "xsi:schemaLocation";
    public static final String SCHEMA_LOCATION = "http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd";
    public static final String XMLNS_XSI = "xmlns:xsi";
    public static final String XML_INSTANCE = "http://www.w3.org/2001/XMLSchema-instance";
}
