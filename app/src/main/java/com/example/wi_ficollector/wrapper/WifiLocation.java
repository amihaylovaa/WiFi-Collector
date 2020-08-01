package com.example.wi_ficollector.wrapper;


import android.net.wifi.ScanResult;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class WifiLocation {
    private double latitude;
    private double longitude;
    private LocalDateTime localDateTime;
    private List<WifiScanResult> wifiScanResults;

    public List<WifiScanResult> getWifiScanResults() {
        return wifiScanResults;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public void setLocalDateTime(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
    }

    public void setWifiScanResults(List<WifiScanResult> wifiScanResults) {
        this.wifiScanResults = wifiScanResults;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void clearResults() {
        latitude = 0.00;
        longitude = 0.00;
        if (wifiScanResults != null) {
            wifiScanResults.clear();
        }
        localDateTime = null;
    }
}