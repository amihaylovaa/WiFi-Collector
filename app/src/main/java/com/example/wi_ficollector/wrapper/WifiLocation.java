package com.example.wi_ficollector.wrapper;

import android.net.wifi.ScanResult;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class WifiLocation implements Serializable {
    private double latitude;
    private double longitude;
    private LocalTime localTime;
    private List<ScanResult> wifiScanResults;
    private static WifiLocation mWifiLocation;

    public WifiLocation() {
        latitude = 0.00;
        longitude = 0.00;
        wifiScanResults = new ArrayList<>();
    }

    public static WifiLocation getWifiLocation() {
        if (mWifiLocation == null) {
            mWifiLocation = new WifiLocation();
        }
        return mWifiLocation;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public List<ScanResult> getScanResults() {
        return wifiScanResults;
    }

    public LocalTime getLocalTime() {
        return localTime;
    }

    public void setLocalTime(LocalTime localTime) {
        this.localTime = localTime;
    }

    public void setScanResults(List<ScanResult> scanResults) {
        wifiScanResults = new ArrayList<>(scanResults);
    }

    public void clearResults() {
        latitude = 0.00;
        longitude = 0.00;
        wifiScanResults.clear();
    }
}