package com.example.wi_ficollector.repository;

import android.content.Context;
import android.util.Log;

import com.example.wi_ficollector.http.HttpRequest;

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
import static com.example.wi_ficollector.utility.Constants.ENCODING;
import static com.example.wi_ficollector.utility.Constants.EXTENSIONS_TAG;
import static com.example.wi_ficollector.utility.Constants.FILE_NAME;
import static com.example.wi_ficollector.utility.Constants.FILE_NOT_FOUND_EXCEPTION_MSG;
import static com.example.wi_ficollector.utility.Constants.FILE_NOT_FOUND_EXCEPTION_TAG;
import static com.example.wi_ficollector.utility.Constants.FREQUENCY_TAG;
import static com.example.wi_ficollector.utility.Constants.LATITUDE;
import static com.example.wi_ficollector.utility.Constants.LONGITUDE;
import static com.example.wi_ficollector.utility.Constants.RSSI_TAG;
import static com.example.wi_ficollector.utility.Constants.SSID_TAG;
import static com.example.wi_ficollector.utility.Constants.TIME_TAG;
import static com.example.wi_ficollector.utility.Constants.TRACK_POINT_TAG;

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
        }
    }

    private void readLocation() {
        double latitude = Double.parseDouble(xpp.getAttributeValue(null, LATITUDE));
        double longitude = Double.parseDouble(xpp.getAttributeValue(null, LONGITUDE));
        wifiLocation = new JSONObject();
        try {
            wifiLocation.put("latitude", latitude);
            wifiLocation.put("longitude", longitude);
        } catch (JSONException e) {

        }
    }

    @Override
    public void read(Context context) {
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
                    wifiLocation.put("localDateTime", localDateTime);
                    eventType = xpp.next();
                    tagName = xpp.getName();
                }
                if (eventType == XmlPullParser.START_TAG && tagName.equals(EXTENSIONS_TAG)) {
                    eventType = xpp.next();
                    tagName = xpp.getName();
                    JSONArray wifiScanResults = new JSONArray();
                    while (eventType != XmlPullParser.END_TAG && !tagName.equals(EXTENSIONS_TAG)) {
                        JSONObject wifi = new JSONObject();
                        eventType = xpp.next();
                        tagName = xpp.getName();
                        if (eventType == XmlPullParser.START_TAG && tagName.equals(BSSID_TAG)) {
                            wifi.put(BSSID_TAG, xpp.nextText());
                            eventType = xpp.next();
                            tagName = xpp.getName();
                        }
                        if (eventType == XmlPullParser.START_TAG && tagName.equals(RSSI_TAG)) {
                            wifi.put(RSSI_TAG, xpp.nextText());
                            eventType = xpp.next();
                            tagName = xpp.getName();
                        }
                        if (eventType == XmlPullParser.START_TAG && tagName.equals(SSID_TAG)) {
                            wifi.put(SSID_TAG, xpp.nextText());
                            eventType = xpp.next();
                            tagName = xpp.getName();
                        }
                        if (eventType == XmlPullParser.START_TAG && tagName.equals(CAPABILITIES_TAG)) {
                            wifi.put(CAPABILITIES_TAG, xpp.nextText());
                            eventType = xpp.next();
                            tagName = xpp.getName();
                        }
                        if (eventType == XmlPullParser.START_TAG && tagName.equals(FREQUENCY_TAG)) {
                            wifi.put(FREQUENCY_TAG, Integer.parseInt(xpp.nextText()));
                            wifiScanResults.put(wifi);
                            eventType = xpp.next();
                            tagName = xpp.getName();
                        }
                        eventType = xpp.next();
                        tagName = xpp.getName();
                    }
                    wifiLocation.put("wifiScanResults", wifiScanResults);
                    jsonArray.put(wifiLocation);
                }
                eventType = xpp.next();
                tagName = xpp.getName();
            }
        } catch (XmlPullParserException | IOException | JSONException e) {

        }
        HttpRequest httpRequest = new HttpRequest();
        httpRequest.send(jsonArray, mContext);
    }

    public void openFileInputStream() {
        try {
            mFileInputStream = mContext.openFileInput(FILE_NAME);
        } catch (FileNotFoundException exception) {
            Log.d(FILE_NOT_FOUND_EXCEPTION_TAG, FILE_NOT_FOUND_EXCEPTION_MSG);
        }
    }
}