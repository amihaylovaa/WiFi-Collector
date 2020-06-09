package com.example.wi_ficollector.wrapper;

import android.location.Location;
import android.net.wifi.ScanResult;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class WifiLocation {
    private Location location;
    private LocalTime localTime;
    private List<ScanResult> wifiScanResults;

    public WifiLocation(WifiLocation wifiLocation) {
        this.location = wifiLocation.getLocation();
        this.localTime = wifiLocation.getLocalTime();
        this.wifiScanResults = new ArrayList<>(wifiLocation.getScanResults());
    }

    public WifiLocation() {
    }

    public List<ScanResult> getScanResults() {
        return wifiScanResults;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
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
        location = null;
        wifiScanResults.clear();
    }
}