package com.example.wi_ficollector.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.example.wi_ficollector.repository.WifiLocationRepository;
import com.example.wi_ficollector.wrapper.WifiLocation;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import static com.example.wi_ficollector.utils.Constants.numberFoundWifiNetworks;

public class WiFiReceiver extends BroadcastReceiver {

    private WifiLocation mWifiLocation;
    private FileOutputStream fileOutputStream;
    private WifiLocationRepository mWifiLocationRepository;
    private Context context;

    public WiFiReceiver(WifiLocationRepository mWifiLocationRepository, FileOutputStream fileOutputStream) {
        this.mWifiLocationRepository = mWifiLocationRepository;
        this.fileOutputStream = fileOutputStream;
        this.mWifiLocation = mWifiLocationRepository.getmWifiLocation();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        boolean hasSuccess = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
        if (hasSuccess) {
            List<ScanResult> scanResults = wifiManager.getScanResults();
            setScanResults(scanResults);
        }
    }

    private void setScanResults(List<ScanResult> scanResults) {
        Log.d("Receiver data", String.valueOf(scanResults.size()));
        if (scanResults != null && scanResults.size() > 0) {
            mWifiLocation.setScanResults(scanResults);
            LocalTime foundNetworksTime = LocalTime.now();
            LocalTime savedLocationTime = mWifiLocation.getLocalTime();
            if (savedLocationTime != null) {
                // when there's no location (location null)
                // todo fix bug with time
                long difference = ChronoUnit.SECONDS.between(savedLocationTime, foundNetworksTime);

                if (difference <= 3) {
                    try {
                        numberFoundWifiNetworks += scanResults.size();
                        mWifiLocationRepository.saveWiFiLocation(fileOutputStream, context);
                    } catch (IOException | TransformerException | ParserConfigurationException e) {

                    }
                }
            }
        }
    }
}