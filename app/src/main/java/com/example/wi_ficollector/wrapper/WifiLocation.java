package com.example.wi_ficollector.wrapper;

import android.net.wifi.ScanResult;

import java.util.ArrayList;
import java.util.List;

public class WifiLocation {
    private double locationLatitude;
    private double locationLongitude;
    private List<ScanResult> wifiScanResults;

    public double getLatitude() {
        return locationLatitude;
    }

    public double getLongitude() {
        return locationLongitude;
    }

    public List<ScanResult> getScanResults() {
        return wifiScanResults;
    }

    public void setLatitude(double latitude) {
        locationLatitude = latitude;
    }

    public void setLongitude(double longitude) {
        locationLongitude = longitude;
    }

    public void setScanResults(List<ScanResult> scanResults) {
        wifiScanResults = new ArrayList<>();
        wifiScanResults.addAll(scanResults);
    }

    public void clearFields() {
        if (wifiScanResults != null) {
            wifiScanResults.clear();
        }
        locationLatitude = 0.00;
        locationLongitude = 0.00;
    }
}