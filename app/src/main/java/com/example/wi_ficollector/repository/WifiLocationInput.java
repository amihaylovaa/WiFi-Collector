package com.example.wi_ficollector.repository;


import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import static com.example.wi_ficollector.utility.Constants.*;

public class WifiLocationInput implements InputOperation {

    private FileInputStream mFileInputStream;
    private Context mContext;
    private XmlPullParser mXmlPullParser;
    private JSONArray mWiFiLocations;
    private JSONObject mWiFiLocation;

    public WifiLocationInput(Context mContext) {
        this.mContext = mContext;
        mWiFiLocations = new JSONArray();
    }


    @Override
    public JSONArray read() throws JSONException {
        try {
            mFileInputStream = mContext.openFileInput(FILE_NAME);
        } catch (FileNotFoundException e) {
            Log.d(FILE_NOT_FOUND_EXCEPTION_TAG, FILE_NOT_FOUND_EXCEPTION_MSG);
            return mWiFiLocations;
        }

        try {
            prepareReading();
        } catch (XmlPullParserException e) {
            Log.d(XML_PULL_PARSER_EXCEPTION_TAG, XML_PULL_PARSER_EXCEPTION_MESSAGE);
            return mWiFiLocations;
        }

        try {
            int eventType = mXmlPullParser.getEventType();
            String tagName = mXmlPullParser.getName();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                readGpx(eventType, tagName);

                eventType = mXmlPullParser.next();
                tagName = mXmlPullParser.getName();
            }
        } catch (XmlPullParserException e) {
            Log.d(XML_PULL_PARSER_EXCEPTION_TAG, XML_PULL_PARSER_EXCEPTION_MESSAGE);
        } catch (IOException e) {
            Log.d(IO_EXCEPTION_THROWN_TAG, IO_EXCEPTION_THROWN_MESSAGE);
        }
        return mWiFiLocations;
    }

    public void deleteLocallyStoredData() {
        File file = new File(mContext.getFilesDir(), FILE_NAME);
        Path path = Paths.get(file.toString());

        if (file.exists()) {
            try {
                Files.delete(path);
            } catch (IOException e) {
                Log.d(IO_EXCEPTION_THROWN_TAG, IO_EXCEPTION_INPUT_MSG);
            }
        }
    }

    public void closeFileInputStream() {
        try {
            mFileInputStream.close();
        } catch (IOException e) {
            Log.d(IO_EXCEPTION_THROWN_TAG, IO_EXCEPTION_INPUT_MSG);
        }
    }

    private void readGpx(int eventType, String tagName) throws XmlPullParserException, IOException, JSONException {
        if (eventType == XmlPullParser.START_TAG) {

            if (tagName.equals(TRACK_POINT_TAG)) {
                readLocation();
            }
            if (tagName.equals(TIME_TAG)) {
                readLocalDateTime();
            }
            if (tagName.equals(EXTENSIONS_TAG)) {
                readExtensions();
            }
        }
    }

    private void prepareReading() throws XmlPullParserException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);

        mXmlPullParser = factory.newPullParser();
        mXmlPullParser.setInput(mFileInputStream, ENCODING);
    }

    private void readLocation() throws JSONException {
        double latitude = Double.parseDouble(mXmlPullParser.getAttributeValue(null, LATITUDE_ATTRIBUTE));
        double longitude = Double.parseDouble(mXmlPullParser.getAttributeValue(null, LONGITUDE_ATTRIBUTE));
        mWiFiLocation = new JSONObject();

        mWiFiLocation.put(LATITUDE, latitude);
        mWiFiLocation.put(LONGITUDE, longitude);
    }

    private void readLocalDateTime() throws JSONException, XmlPullParserException, IOException {
        String dateTime = mXmlPullParser.nextText();
        LocalDateTime localDateTime = LocalDateTime.parse(dateTime);

        mWiFiLocation.put(DATE_TIME, localDateTime);
    }

    private void readExtensions() throws JSONException, XmlPullParserException, IOException {
        JSONArray wifiScanResults = getWifiScanResults();

        mWiFiLocation.put(WIFI_SCAN_RESULTS, wifiScanResults);
        mWiFiLocations.put(mWiFiLocation);
    }

    private JSONArray getWifiScanResults() throws JSONException, IOException, XmlPullParserException {
        int eventType = mXmlPullParser.next();
        String tagName = mXmlPullParser.getName();
        JSONArray wifiScanResults = new JSONArray();

        while (eventType != XmlPullParser.END_TAG && !tagName.equals(EXTENSIONS_TAG)) {

            if (eventType == XmlPullParser.START_TAG && tagName.equals(WIFI_TAG)) {
                JSONObject wifiScanResult = getWifiScanResult();

                wifiScanResults.put(wifiScanResult);
            }
            eventType = mXmlPullParser.next();
            tagName = mXmlPullParser.getName();
        }
        return wifiScanResults;
    }

    private JSONObject getWifiScanResult() throws IOException, XmlPullParserException, JSONException {
        JSONObject wifiScanResult = new JSONObject();
        int eventType = mXmlPullParser.next();
        String tagName = mXmlPullParser.getName();

        while (eventType != XmlPullParser.END_TAG && !tagName.equals(WIFI_TAG)) {

            switch (tagName) {
                case BSSID_TAG:
                    wifiScanResult.put(BSSID_TAG, mXmlPullParser.nextText());
                    break;
                case RSSI_TAG:
                    wifiScanResult.put(RSSI_TAG, mXmlPullParser.nextText());
                    break;
                case SSID_TAG:
                    wifiScanResult.put(SSID_TAG, mXmlPullParser.nextText());
                    break;
                case CAPABILITIES_TAG:
                    wifiScanResult.put(CAPABILITIES_TAG, mXmlPullParser.nextText());
                    break;
                case FREQUENCY_TAG:
                    wifiScanResult.put(FREQUENCY_TAG, mXmlPullParser.nextText());
                    break;
                default:
                    break;
            }
            eventType = mXmlPullParser.next();
            tagName = mXmlPullParser.getName();
        }
        return wifiScanResult;
    }
}