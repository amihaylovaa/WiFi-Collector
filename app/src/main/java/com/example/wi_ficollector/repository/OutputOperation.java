package com.example.wi_ficollector.repository;

import android.net.wifi.ScanResult;

import com.example.wi_ficollector.wrapper.WifiLocation;

import java.util.List;


public interface OutputOperation {

    void write(WifiLocation wifiLocation, List<ScanResult> scanResultList);
}
