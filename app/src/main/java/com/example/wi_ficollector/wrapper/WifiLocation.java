package com.example.wi_ficollector.wrapper;

import android.location.Location;
import android.net.wifi.ScanResult;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class WifiLocation {
    private Location mLocation;
    private LocalTime localTime;
    private List<ScanResult> wifiScanResults;
    private static WifiLocation wifiLocation;

    private WifiLocation() {

    }

    public static WifiLocation getWifiLocation() {
        if (wifiLocation == null) {
            wifiLocation = new WifiLocation();
        }
        return wifiLocation;
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

    public Location getLocation() {
        return mLocation;
    }

    public void setLocation(Location mLocation) {
        this.mLocation = mLocation;
    }

    public void clearResults() {
        mLocation = null;
        if (wifiScanResults != null) {
            wifiScanResults.clear();
        }
        localTime = null;
    }
}