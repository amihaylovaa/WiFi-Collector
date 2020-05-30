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

public class LocationThread implements Runnable {

    private Location mLocation;
    private WifiLocationRepository mWifiLocationRepository;
    private FileOutputStream mFileOutputStream;

    public LocationThread(Location mLocation, WifiLocationRepository mWifiLocationRepository, FileOutputStream mFileOutputStream) {
        this.mLocation = mLocation;
        this.mWifiLocationRepository = mWifiLocationRepository;
        this.mFileOutputStream = mFileOutputStream;
    }

    @Override
    public void run() {
        countDownLatch = new CountDownLatch(1);
        WifiLocation.setLatitude(mLocation.getLatitude());
        WifiLocation.setLongitude(mLocation.getLongitude());
        isAlreadyScanned = false;
        try {
            countDownLatch.await();
            mWifiLocationRepository.saveWiFiLocation(mFileOutputStream);
        } catch (TransformerException | ParserConfigurationException | InterruptedException e) {
            e.printStackTrace();
        }
        WifiLocation.clearFields();
        countDownLatch = null;
    }
}
