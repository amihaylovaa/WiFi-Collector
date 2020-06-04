package com.example.wi_ficollector.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import com.example.wi_ficollector.repository.WifiLocationRepository;
import com.example.wi_ficollector.wrapper.WifiLocation;

import java.io.FileOutputStream;
import java.time.LocalTime;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import static com.example.wi_ficollector.utils.Constants.numberFoundWifiNetworks;

public class WiFiReceiver extends BroadcastReceiver {

    private WifiLocation mWifiLocation;
    private FileOutputStream fileOutputStream;
    private WifiLocationRepository mWifiLocationRepository;

    public WiFiReceiver(WifiLocationRepository mWifiLocationRepository, FileOutputStream fileOutputStream) {
        this.mWifiLocationRepository = mWifiLocationRepository;
        this.fileOutputStream = fileOutputStream;
        this.mWifiLocation = mWifiLocationRepository.getmWifiLocation();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        boolean hasSuccess = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
        if (hasSuccess) {
            List<ScanResult> scanResults = wifiManager.getScanResults();
            setScanResults(scanResults);
        }
    }

    private void setScanResults(List<ScanResult> scanResults) {
        if (scanResults != null && scanResults.size() > 0) {
            numberFoundWifiNetworks += scanResults.size();
            mWifiLocation.setScanResults(scanResults);
            LocalTime localTime = LocalTime.now();
            if (mWifiLocation.getLocalTime() != null) {
                // when there's no location (location null)
                int secondLocation = mWifiLocation.getLocalTime().getSecond();
                int difference = localTime.getSecond() - secondLocation;

                if (difference <= 3) {
                    try {
                        mWifiLocationRepository.saveWiFiLocation(fileOutputStream);
                    } catch (TransformerException | ParserConfigurationException e) {

                    }
                }
            }
        }
    }
}