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
    private XmlPullParser xpp;
    private JSONArray wifiLocations;
    private JSONObject wifiLocation;

    public WifiLocationInput(Context mContext) {
        this.mContext = mContext;
        wifiLocations = new JSONArray();
    }

    @Override
    public JSONArray read() throws JSONException {

        try {
            mFileInputStream = mContext.openFileInput(FILE_NAME);
        } catch (FileNotFoundException e) {
            Log.d(FILE_NOT_FOUND_EXCEPTION_TAG, FILE_NOT_FOUND_EXCEPTION_MSG);
            return wifiLocations;
        }

        try {
            prepareReading();
        } catch (XmlPullParserException e) {
            Log.d(XML_PULL_PARSER_EXCEPTION_TAG, XML_PULL_PARSER_EXCEPTION_MESSAGE);
            return wifiLocations;
        }

        try {
            int eventType = xpp.getEventType();
            String tagName = xpp.getName();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                readGpx(eventType, tagName);

                eventType = xpp.next();
                tagName = xpp.getName();
            }
        } catch (XmlPullParserException e) {
            Log.d(XML_PULL_PARSER_EXCEPTION_TAG, XML_PULL_PARSER_EXCEPTION_MESSAGE);
        } catch (IOException e) {
            Log.d(IO_EXCEPTION_THROWN_TAG, IO_EXCEPTION_THROWN_MESSAGE);
        }
        return wifiLocations;
    }

    public void deleteLocalStoredData() {
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

    public void readGpx(int eventType, String tagName) throws XmlPullParserException, IOException, JSONException {
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

    public void prepareReading() throws XmlPullParserException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);

        xpp = factory.newPullParser();
        xpp.setInput(mFileInputStream, ENCODING);
    }

    public void readLocation() throws JSONException {
        double latitude = Double.parseDouble(xpp.getAttributeValue(null, LATITUDE_ATTRIBUTE));
        double longitude = Double.parseDouble(xpp.getAttributeValue(null, LONGITUDE_ATTRIBUTE));
        wifiLocation = new JSONObject();

        wifiLocation.put(LATITUDE, latitude);
        wifiLocation.put(LONGITUDE, longitude);
    }

    public void readLocalDateTime() throws JSONException, XmlPullParserException, IOException {
        String dateTime = xpp.nextText();
        LocalDateTime localDateTime = LocalDateTime.parse(dateTime);

        wifiLocation.put(DATE_TIME, localDateTime);
    }

    public void readExtensions() throws JSONException, XmlPullParserException, IOException {
        JSONArray wifiScanResults = getWifiScanResults();

        wifiLocation.put(WIFI_SCAN_RESULTS, wifiScanResults);
        wifiLocations.put(wifiLocation);
    }

    public JSONArray getWifiScanResults() throws JSONException, IOException, XmlPullParserException {
        int eventType = xpp.next();
        String tagName = xpp.getName();
        JSONArray wifiScanResults = new JSONArray();

        while (eventType != XmlPullParser.END_TAG && !tagName.equals(EXTENSIONS_TAG)) {

            if (eventType == XmlPullParser.START_TAG && tagName.equals(WIFI_TAG)) {
                JSONObject wifiScanResult = getWifiScanResult();

                wifiScanResults.put(wifiScanResult);
            }
            eventType = xpp.next();
            tagName = xpp.getName();
        }
        return wifiScanResults;
    }

    public JSONObject getWifiScanResult() throws IOException, XmlPullParserException, JSONException {
        JSONObject wifiScanResult = new JSONObject();
        int eventType = xpp.next();
        String tagName = xpp.getName();

        while (eventType != XmlPullParser.END_TAG && !tagName.equals(WIFI_TAG)) {

            switch (tagName) {
                case BSSID_TAG:
                    wifiScanResult.put(BSSID_TAG, xpp.nextText());
                    break;
                case RSSI_TAG:
                    wifiScanResult.put(RSSI_TAG, xpp.nextText());
                    break;
                case SSID_TAG:
                    wifiScanResult.put(SSID_TAG, xpp.nextText());
                    break;
                case CAPABILITIES_TAG:
                    wifiScanResult.put(CAPABILITIES_TAG, xpp.nextText());
                    break;
                case FREQUENCY_TAG:
                    wifiScanResult.put(FREQUENCY_TAG, xpp.nextText());
                    break;
                default:
                    break;
            }
            eventType = xpp.next();
            tagName = xpp.getName();
        }
        return wifiScanResult;
    }
}