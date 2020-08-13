package com.example.wi_ficollector.repository;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import java.io.IOException;
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
import static com.example.wi_ficollector.utility.Constants.LATITUDE;
import static com.example.wi_ficollector.utility.Constants.LATITUDE_ATTRIBUTE;
import static com.example.wi_ficollector.utility.Constants.LONGITUDE;
import static com.example.wi_ficollector.utility.Constants.LONGITUDE_ATTRIBUTE;
import static com.example.wi_ficollector.utility.Constants.RSSI_TAG;
import static com.example.wi_ficollector.utility.Constants.SSID_TAG;
import static com.example.wi_ficollector.utility.Constants.TIME_TAG;
import static com.example.wi_ficollector.utility.Constants.TRACK_POINT_TAG;
import static com.example.wi_ficollector.utility.Constants.WIFI_SCAN_RESULTS;

public class WifiLocationInput implements InputOperation {

    private FileInputStream mFileInputStream;
    private Context mContext;
    private XmlPullParser xpp;
    private JSONArray jsonArray;
    private boolean isOutputSet;
    private JSONObject wifiLocation;

    public WifiLocationInput(Context mContext) {
        this.mContext = mContext;
        isOutputSet = false;
        jsonArray = new JSONArray();
        openFileInputStream();
    }

    private void prepareRead() throws XmlPullParserException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        xpp = factory.newPullParser();
        if (!isOutputSet) {
            xpp.setInput(mFileInputStream, ENCODING);
            isOutputSet = true;
        }
    }

    private void readLocation() throws JSONException {
        double latitude = Double.parseDouble(xpp.getAttributeValue(null, LATITUDE_ATTRIBUTE));
        double longitude = Double.parseDouble(xpp.getAttributeValue(null, LONGITUDE_ATTRIBUTE));
        wifiLocation = new JSONObject();

        wifiLocation.put(LATITUDE, latitude);
        wifiLocation.put(LONGITUDE, longitude);
    }

    @Override
    public void read() throws XmlPullParserException, IOException, JSONException {
        prepareRead();
        int eventType = xpp.getEventType();
        String tagName = xpp.getName();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && tagName.equals(TRACK_POINT_TAG)) {
                readLocation();
                eventType = xpp.next();
                tagName = xpp.getName();
            }
            if (eventType == XmlPullParser.START_TAG && tagName.equals(TIME_TAG)) {
                LocalDateTime localDateTime = LocalDateTime.parse(xpp.nextText());
                wifiLocation.put(DATE_TIME, localDateTime);
                eventType = xpp.next();
                tagName = xpp.getName();
            }
            if (eventType == XmlPullParser.START_TAG && tagName.equals(EXTENSIONS_TAG)) {
                eventType = xpp.next();
                tagName = xpp.getName();
                JSONArray wifiScanResults = readWifi(eventType, tagName);
                wifiLocation.put(WIFI_SCAN_RESULTS, wifiScanResults);
                jsonArray.put(wifiLocation);
            }
            eventType = xpp.next();
            tagName = xpp.getName();
        }
    }

    private JSONArray readWifi(int eventType, String tagName) throws IOException, XmlPullParserException, JSONException {
        JSONArray wifiScanResults = new JSONArray();

        while (eventType != XmlPullParser.END_TAG && !tagName.equals(EXTENSIONS_TAG)) {
            JSONObject wifi = new JSONObject();
            xpp.next();
            tagName = xpp.getName();
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
        return wifiScanResults;
    }


    public JSONArray getJsonArray() {
        return jsonArray;
    }

    public void openFileInputStream() {
        try {
            mFileInputStream = mContext.openFileInput(FILE_NAME);
        } catch (FileNotFoundException exception) {
            Log.d(FILE_NOT_FOUND_EXCEPTION_TAG, FILE_NOT_FOUND_EXCEPTION_MSG);
        }
    }
}