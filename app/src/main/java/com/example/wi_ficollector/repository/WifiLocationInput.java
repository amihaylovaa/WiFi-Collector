package com.example.wi_ficollector.repository;

import android.content.Context;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.util.Log;


import com.example.wi_ficollector.wrapper.WifiLocation;
import com.example.wi_ficollector.wrapper.WifiScanResult;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static com.example.wi_ficollector.utils.Constants.BSSID_TAG;
import static com.example.wi_ficollector.utils.Constants.ENCODING;
import static com.example.wi_ficollector.utils.Constants.EXTENSIONS_TAG;
import static com.example.wi_ficollector.utils.Constants.FILE_NAME;
import static com.example.wi_ficollector.utils.Constants.FILE_NOT_FOUND_EXCEPTION_MSG;
import static com.example.wi_ficollector.utils.Constants.FILE_NOT_FOUND_EXCEPTION_TAG;
import static com.example.wi_ficollector.utils.Constants.FREQUENCY_TAG;
import static com.example.wi_ficollector.utils.Constants.LATITUDE;
import static com.example.wi_ficollector.utils.Constants.LONGITUDE;
import static com.example.wi_ficollector.utils.Constants.RSSI_TAG;
import static com.example.wi_ficollector.utils.Constants.SSID_TAG;
import static com.example.wi_ficollector.utils.Constants.TIME_TAG;
import static com.example.wi_ficollector.utils.Constants.TRACK_POINT_TAG;

public class WifiLocationInput implements InputOperation {

    private FileInputStream mFileInputStream;
    private Context mContext;
    private XmlPullParser xpp;
    private List<WifiLocation> mWifiLocations;
    private boolean isOutputSet;
    private WifiLocation wifiLocation;

    public WifiLocationInput(Context mContext) {
        this.mContext = mContext;
        mWifiLocations = new ArrayList<>();
        isOutputSet = false;
        openFileInputStream();
    }

    private void prepareRead() {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            xpp = factory.newPullParser();
            if (!isOutputSet) {
                xpp.setInput(mFileInputStream, ENCODING);
                isOutputSet = true;
            }
        } catch (XmlPullParserException e) {
            ;
        }
    }

    private void readLocation() {
        double latitude = Double.parseDouble(xpp.getAttributeValue(null, LATITUDE));
        double longitude = Double.parseDouble(xpp.getAttributeValue(null, LONGITUDE));
        wifiLocation = new WifiLocation();
        wifiLocation.setLongitude(longitude);
        wifiLocation.setLatitude(latitude);
    }

    @Override
    public void read() {
        try {
            prepareRead();
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = xpp.getName();
                if (eventType == XmlPullParser.START_TAG && tagName.equals(TRACK_POINT_TAG)) {
                    readLocation();
                    eventType = xpp.next();
                    tagName = xpp.getName();
                }
                if (eventType == XmlPullParser.START_TAG && tagName.equals(TIME_TAG)) {
                    LocalDateTime localDateTime = LocalDateTime.parse(xpp.nextText());
                    wifiLocation.setLocalDateTime(localDateTime);
                    eventType = xpp.next();
                    tagName = xpp.getName();
                }
                if (eventType == XmlPullParser.START_TAG && tagName.equals(EXTENSIONS_TAG)) {
                    eventType = xpp.next();
                    tagName = xpp.getName();
                    List<WifiScanResult> wifiScanResults = new ArrayList<>();
                    while (eventType != XmlPullParser.END_TAG && !tagName.equals(EXTENSIONS_TAG)) {
                        WifiScanResult scanResult = new WifiScanResult();
                        eventType = xpp.next();
                        tagName = xpp.getName();
                        if (eventType == XmlPullParser.START_TAG && tagName.equals(SSID_TAG)) {
                            scanResult.setSSID(xpp.nextText());
                            eventType = xpp.next();
                            tagName = xpp.getName();
                        }
                        if (eventType == XmlPullParser.START_TAG && tagName.equals(BSSID_TAG)) {
                            scanResult.setBSSID(xpp.nextText());
                            eventType = xpp.next();
                            tagName = xpp.getName();
                        }
                        if (eventType == XmlPullParser.START_TAG && tagName.equals(RSSI_TAG)) {
                            scanResult.setRSSI(xpp.nextText());
                            eventType = xpp.next();
                            tagName = xpp.getName();
                        }
                        if (eventType == XmlPullParser.START_TAG && tagName.equals(FREQUENCY_TAG)) {
                            scanResult.setFrequency(Integer.parseInt(xpp.nextText()));
                            wifiScanResults.add(scanResult);
                            eventType = xpp.next();
                            tagName = xpp.getName();
                        }
                        eventType = xpp.next();
                        tagName = xpp.getName();
                    }
                    wifiLocation.setWifiScanResults(wifiScanResults);
                    mWifiLocations.add(wifiLocation);
                }
                eventType = xpp.next();
            }
        } catch (XmlPullParserException | IOException e) {

        }
        int x = 0;
    }

    public void openFileInputStream() {
        try {
            mFileInputStream = mContext.openFileInput(FILE_NAME);
        } catch (FileNotFoundException exception) {
            Log.d(FILE_NOT_FOUND_EXCEPTION_TAG, FILE_NOT_FOUND_EXCEPTION_MSG);
        }
    }

    private void sendData() {
        String host = "localhost";
        String port = "3306";


    }
}