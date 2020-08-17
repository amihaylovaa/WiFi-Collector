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

import static com.example.wi_ficollector.utility.Constants.BSSID_TAG;
import static com.example.wi_ficollector.utility.Constants.CAPABILITIES_TAG;
import static com.example.wi_ficollector.utility.Constants.DATE_TIME;
import static com.example.wi_ficollector.utility.Constants.ENCODING;
import static com.example.wi_ficollector.utility.Constants.EXTENSIONS_TAG;
import static com.example.wi_ficollector.utility.Constants.FILE_NAME;
import static com.example.wi_ficollector.utility.Constants.FILE_NOT_FOUND_EXCEPTION_MSG;
import static com.example.wi_ficollector.utility.Constants.FILE_NOT_FOUND_EXCEPTION_TAG;
import static com.example.wi_ficollector.utility.Constants.FREQUENCY_TAG;
import static com.example.wi_ficollector.utility.Constants.IO_EXCEPTION_THROWN_MESSAGE;
import static com.example.wi_ficollector.utility.Constants.IO_EXCEPTION_THROWN_TAG;
import static com.example.wi_ficollector.utility.Constants.JSON_EXCEPTION_MESSAGE;
import static com.example.wi_ficollector.utility.Constants.JSON_EXCEPTION_TAG;
import static com.example.wi_ficollector.utility.Constants.LATITUDE;
import static com.example.wi_ficollector.utility.Constants.LATITUDE_ATTRIBUTE;
import static com.example.wi_ficollector.utility.Constants.LONGITUDE;
import static com.example.wi_ficollector.utility.Constants.LONGITUDE_ATTRIBUTE;
import static com.example.wi_ficollector.utility.Constants.RSSI_TAG;
import static com.example.wi_ficollector.utility.Constants.SSID_TAG;
import static com.example.wi_ficollector.utility.Constants.TIME_TAG;
import static com.example.wi_ficollector.utility.Constants.TRACK_POINT_TAG;
import static com.example.wi_ficollector.utility.Constants.WIFI_SCAN_RESULTS;
import static com.example.wi_ficollector.utility.Constants.XML_PULL_PARSER_EXCEPTION_MESSAGE;
import static com.example.wi_ficollector.utility.Constants.XML_PULL_PARSER_EXCEPTION_TAG;

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
    public JSONArray read() {
        try {
            openFileInputStream();
            if (mFileInputStream != null) {
                prepareReading();
                int eventType = xpp.getEventType();
                String tagName = xpp.getName();

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    readGpx(eventType, tagName);

                    eventType = xpp.next();
                    tagName = xpp.getName();
                }
            }
        } catch (IOException e) {
            Log.d(IO_EXCEPTION_THROWN_TAG, IO_EXCEPTION_THROWN_MESSAGE);
        } catch (XmlPullParserException e) {
            Log.d(XML_PULL_PARSER_EXCEPTION_TAG, XML_PULL_PARSER_EXCEPTION_MESSAGE);
        }
        return wifiLocations;
    }

    private void readGpx(int eventType, String tagName) {
        if (eventType == XmlPullParser.START_TAG) {

            if (tagName.equals(TRACK_POINT_TAG)) {
                readLocation();
            }
            if (tagName.equals(TIME_TAG)) {
                readLocalDateTime();
            }
            if (tagName.equals(EXTENSIONS_TAG)) {
                readExtensions(eventType);
            }
        }
    }

    private void prepareReading() {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);

            xpp = factory.newPullParser();
            xpp.setInput(mFileInputStream, ENCODING);
        } catch (XmlPullParserException e) {
            Log.d(XML_PULL_PARSER_EXCEPTION_TAG, XML_PULL_PARSER_EXCEPTION_MESSAGE);
        }
    }

    private void readLocation() {
        double latitude = Double.parseDouble(xpp.getAttributeValue(null, LATITUDE_ATTRIBUTE));
        double longitude = Double.parseDouble(xpp.getAttributeValue(null, LONGITUDE_ATTRIBUTE));
        wifiLocation = new JSONObject();

        try {
            wifiLocation.put(LATITUDE, latitude);
            wifiLocation.put(LONGITUDE, longitude);
        } catch (JSONException e) {
            Log.d(JSON_EXCEPTION_TAG, JSON_EXCEPTION_MESSAGE);
        }
    }

    private void readExtensions(int eventType) {
        JSONArray wifiScanResults = getReadWifiScanList(eventType);

        try {
            wifiLocation.put(WIFI_SCAN_RESULTS, wifiScanResults);
            wifiLocations.put(wifiLocation);
        } catch (JSONException e) {
            Log.d(JSON_EXCEPTION_TAG, JSON_EXCEPTION_MESSAGE);
        }
    }

    private void readLocalDateTime() {
        LocalDateTime localDateTime = null;

        try {
            String dateTime = xpp.nextText();
            localDateTime = LocalDateTime.parse(dateTime);
        } catch (IOException e) {
            Log.d(IO_EXCEPTION_THROWN_TAG, IO_EXCEPTION_THROWN_MESSAGE);
        } catch (XmlPullParserException e) {
            Log.d(XML_PULL_PARSER_EXCEPTION_TAG, XML_PULL_PARSER_EXCEPTION_MESSAGE);
        }

        try {
            wifiLocation.put(DATE_TIME, localDateTime);
        } catch (JSONException e) {
            Log.d(JSON_EXCEPTION_TAG, JSON_EXCEPTION_MESSAGE);
        }
    }

    private JSONArray getReadWifiScanList(int eventType) {
        JSONArray wifiScanResults = new JSONArray();

        try {
            xpp.next();
            String tagName = xpp.getName();

            while (eventType != XmlPullParser.END_TAG && !tagName.equals(EXTENSIONS_TAG)) {
                JSONObject wifi = new JSONObject();

                if (tagName.equals(BSSID_TAG)) {
                    wifi.put(BSSID_TAG, xpp.nextText());
                    xpp.next();

                    tagName = xpp.getName();
                }
                if (tagName.equals(RSSI_TAG)) {
                    wifi.put(RSSI_TAG, xpp.nextText());
                    xpp.next();

                    tagName = xpp.getName();
                }
                if (tagName.equals(SSID_TAG)) {
                    wifi.put(SSID_TAG, xpp.nextText());
                    xpp.next();

                    tagName = xpp.getName();
                }
                if (tagName.equals(CAPABILITIES_TAG)) {
                    wifi.put(CAPABILITIES_TAG, xpp.nextText());
                    xpp.next();

                    tagName = xpp.getName();
                }
                if (tagName.equals(FREQUENCY_TAG)) {
                    wifi.put(FREQUENCY_TAG, Integer.parseInt(xpp.nextText()));
                    wifiScanResults.put(wifi);
                    xpp.next();
                }
                eventType = xpp.next();
                tagName = xpp.getName();
            }
        } catch (IOException e) {
            Log.d(IO_EXCEPTION_THROWN_TAG, IO_EXCEPTION_THROWN_MESSAGE);
        } catch (JSONException e) {
            Log.d(JSON_EXCEPTION_TAG, JSON_EXCEPTION_MESSAGE);

        } catch (XmlPullParserException e) {
            Log.d(XML_PULL_PARSER_EXCEPTION_TAG, XML_PULL_PARSER_EXCEPTION_MESSAGE);
        }
        return wifiScanResults;
    }

    public void openFileInputStream() {
        try {
            if (mFileInputStream == null) {
                mFileInputStream = mContext.openFileInput(FILE_NAME);
            }
        } catch (FileNotFoundException e) {
            Log.d(FILE_NOT_FOUND_EXCEPTION_TAG, FILE_NOT_FOUND_EXCEPTION_MSG);
        }
    }

    public void deleteLocalStoredData() {
        File file = new File(mContext.getFilesDir(), FILE_NAME);
        Path path = Paths.get(file.toString());

        if (file.exists()) {
            try {
                Files.delete(path);
            } catch (IOException e) {
                Log.d(IO_EXCEPTION_THROWN_TAG, IO_EXCEPTION_THROWN_MESSAGE);
            }
        }
    }

    public void closeFileInputStream() {
        try {
            mFileInputStream.close();
        } catch (IOException e) {
            Log.d(IO_EXCEPTION_THROWN_TAG, IO_EXCEPTION_THROWN_MESSAGE);
        }
    }
}