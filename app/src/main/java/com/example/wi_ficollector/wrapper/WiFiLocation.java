package com.example.wi_ficollector.wrapper;

import android.net.wifi.ScanResult;

import java.util.ArrayList;
import java.util.List;

public class WiFiLocation {
    private static double locationLatitude;
    private static double locationLongitude;
    private static List<ScanResult> wifiScanResults;
    private static WiFiLocation wiFiLocation;

    private WiFiLocation() {
    }

    public static WiFiLocation getWiFiLocation() {
        if (wiFiLocation == null) {
            wiFiLocation = new WiFiLocation();
        }
        return wiFiLocation;
    }

    public static double getLatitude() {
        return locationLatitude;
    }

    public static double getLongitude() {
        return locationLongitude;
    }

    public static List<ScanResult> getScanResults() {
        return wifiScanResults;
    }

    public static void setLatitude(double latitude) {
        locationLatitude = latitude;
    }

    public static void setLongitude(double longitude) {
        locationLongitude = longitude;
    }

    public static void setScanResults(List<ScanResult> scanResults) {
        wifiScanResults = new ArrayList<>(scanResults);
    }

    public static void clearFields() {
        if (wifiScanResults != null) {
            wifiScanResults.clear();
        }
        locationLatitude = 0.00;
        locationLongitude = 0.00;
    }
}