package com.example.wi_ficollector.repository;

import android.content.Context;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.util.Log;


import com.example.wi_ficollector.wrapper.WifiLocation;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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
import static com.example.wi_ficollector.utils.Constants.WIFI_TAG;

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
        } catch (XmlPullParserException e) {
            ;
        }
        if (!isOutputSet) {
            try {
                xpp.setInput(mFileInputStream, ENCODING);
            } catch (XmlPullParserException e) {
                ;
            }
            isOutputSet = true;
        }
    }

    // todo fix all bugs
    @Override
    public void read() {
        List<ScanResult> scanResults = new ArrayList<>();
        try {
            prepareRead();
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = xpp.getName();
                if (eventType == XmlPullParser.START_TAG && tagName.equals(TRACK_POINT_TAG)) {
                    double latitude = Double.parseDouble(xpp.getAttributeValue(null, LATITUDE));
                    double longitude = Double.parseDouble(xpp.getAttributeValue(null, LONGITUDE));
                    wifiLocation = new WifiLocation();
                    wifiLocation.setLongitude(longitude);
                    wifiLocation.setLatitude(latitude);
                }
                if (eventType == XmlPullParser.START_TAG && tagName.equals(TIME_TAG)) {
                    LocalDateTime localDateTime = LocalDateTime.parse(xpp.nextText());
                    wifiLocation.setLocalDateTime(localDateTime);
                }
                if (eventType == XmlPullParser.START_TAG && tagName.equals(WIFI_TAG)) {
                    String SSID = null;
                    String BSSID = null;
                    String RSSI = null;
                    int frequency = 0;
                    eventType = xpp.next();
                    while (eventType != XmlPullParser.END_TAG && tagName.equals(EXTENSIONS_TAG)) {
                        ScanResult scanResult = createScanResultObject();
                        tagName = xpp.getName();
                        if (eventType == XmlPullParser.START_TAG && tagName.equals(SSID_TAG)) {
                            SSID = xpp.nextText();
                            scanResult.SSID = SSID;
                            eventType = xpp.next();
                        }
                        if (eventType == XmlPullParser.START_TAG && tagName.equals(BSSID_TAG)) {
                            BSSID = xpp.nextText();
                            scanResult.BSSID = BSSID;
                            eventType = xpp.next();
                        }
                        if (eventType == XmlPullParser.START_TAG && tagName.equals(RSSI_TAG)) {
                            RSSI = xpp.nextText();
                            eventType = xpp.next();
                        }
                        if (eventType == XmlPullParser.START_TAG && tagName.equals(FREQUENCY_TAG)) {
                            frequency = Integer.parseInt(xpp.nextText());
                            scanResult.frequency = frequency;
                            eventType = xpp.next();
                            scanResults.add(scanResult);
                        }
                        eventType = xpp.next();
                    }
                    wifiLocation.setScanResults(scanResults);
                    mWifiLocations.add(wifiLocation);
                }
                eventType = xpp.next();
            }
        } catch (XmlPullParserException | IOException e) {

        }
    }

    private ScanResult createScanResultObject() {
        ScanResult scanResult = null;
        try {
            Constructor<ScanResult> scanResultConstructor = ScanResult.class.getConstructor();
            scanResult = scanResultConstructor.newInstance();
        } catch (NoSuchMethodException e) {
            ;
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return scanResult;
    }

    public void openFileInputStream() {
        try {
            mFileInputStream = mContext.openFileInput(FILE_NAME);
        } catch (FileNotFoundException exception) {
            Log.d(FILE_NOT_FOUND_EXCEPTION_TAG, FILE_NOT_FOUND_EXCEPTION_MSG);
        }
    }

}
