package com.example.wi_ficollector.thread;


import com.example.wi_ficollector.repository.WifiLocationRepository;

import java.io.FileOutputStream;
import java.util.concurrent.CountDownLatch;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import static com.example.wi_ficollector.utils.Constants.isAlreadyScanned;

public class LocationTask implements Runnable {

    private WifiLocationRepository mWifiLocationRepository;
    private FileOutputStream mFileOutputStream;
    private CountDownLatch mCountDownLatch;
    private boolean isWifiScanningSucceeded;

    public LocationTask(WifiLocationRepository mWifiLocationRepository,
                        FileOutputStream mFileOutputStream, boolean isWifiScanningSucceeded, CountDownLatch mCountDownLatch) {
        this.mWifiLocationRepository = mWifiLocationRepository;
        this.mFileOutputStream = mFileOutputStream;
        this.isWifiScanningSucceeded = isWifiScanningSucceeded;
        this.mCountDownLatch = mCountDownLatch;
    }

    @Override
    public void run() {
        if (isWifiScanningSucceeded) {
            isAlreadyScanned = false;
            try {
                mCountDownLatch.await();
                mWifiLocationRepository.saveWiFiLocation(mFileOutputStream);
            } catch (InterruptedException | TransformerException | ParserConfigurationException e) {
                e.printStackTrace();
            }
            mCountDownLatch = null;
        }
    }
}