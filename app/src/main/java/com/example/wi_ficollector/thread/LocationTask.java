package com.example.wi_ficollector.thread;


import android.location.Location;

import com.example.wi_ficollector.repository.WifiLocationRepository;
import com.example.wi_ficollector.wrapper.WifiLocation;

import java.io.FileOutputStream;
import java.util.concurrent.CountDownLatch;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import static com.example.wi_ficollector.utils.Constants.countDownLatch;
import static com.example.wi_ficollector.utils.Constants.isAlreadyScanned;

public class LocationTask implements Runnable {

    private Location mLocation;
    private WifiLocationRepository mWifiLocationRepository;
    private FileOutputStream mFileOutputStream;
    private boolean isWifiScanningSucceeded;

    public LocationTask(Location mLocation, WifiLocationRepository mWifiLocationRepository,
                        FileOutputStream mFileOutputStream, boolean isWifiScanningSucceeded) {
        this.mLocation = mLocation;
        this.mWifiLocationRepository = mWifiLocationRepository;
        this.mFileOutputStream = mFileOutputStream;
        this.isWifiScanningSucceeded = isWifiScanningSucceeded;
    }

    @Override
    public void run() {
        if (!isWifiScanningSucceeded) {
            setLocation();
        } else {
            countDownLatch = new CountDownLatch(1);
            setLocation();
            isAlreadyScanned = false;
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
            try {
                mWifiLocationRepository.saveWiFiLocation(mFileOutputStream);
            } catch (TransformerException | ParserConfigurationException e) {

            }
            WifiLocation.clearFields();
            countDownLatch = null;
        }

    private void setLocation() {
        WifiLocation.setLatitude(mLocation.getLatitude());
        WifiLocation.setLongitude(mLocation.getLongitude());
    }
}