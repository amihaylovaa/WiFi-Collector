package com.example.wi_ficollector.repository;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.util.Log;
import android.util.Xml;


import com.example.wi_ficollector.wrapper.WifiLocation;

import org.xmlpull.v1.XmlSerializer;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.content.Context.MODE_APPEND;
import static com.example.wi_ficollector.utility.Constants.*;

public class WifiLocationOutput  {

    private int numOfWifiLocations;
    private Context mContext;
    private FileOutputStream mFileOutputStream;
    private XmlSerializer mXmlSerializer;
    private ExecutorService mExecutorService;
    private static final String XMLNS_ATTRIBUTE;
    private static final String GPX_NAMESPACE;
    private static final String XSI_SCHEMA_LOCATION;
    private static final String GPX_SCHEMA_LOCATION;
    private static final String XMLNS_XSI;
    private static final String XML_INSTANCE;
    private static final String WIFI_NAMESPACE_PREFIX;
    private static final String WIFI_SCHEMA;
    private static final String GPX_VERSION;
    private static final String VERSION;
    private static final String CREATOR;
    private static final String CREATOR_NAME;

    static {
        XMLNS_ATTRIBUTE = "xmlns";
        GPX_NAMESPACE = "http://www.topografix.com/GPX/1/1";
        XSI_SCHEMA_LOCATION = "xsi:schemaLocation";
        GPX_SCHEMA_LOCATION = "http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd src/main/res/xml/wifi_schema.xsd";
        XMLNS_XSI = "xmlns:xsi";
        WIFI_SCHEMA = "src/main/res/xml/wifi_schema.xsd";
        WIFI_NAMESPACE_PREFIX = "wifi";
        GPX_VERSION = "1.1";
        CREATOR_NAME = "Unknown";
        CREATOR = "creator";
        VERSION = "version";
        XML_INSTANCE = "http://www.w3.org/2001/XMLSchema-instance";
    }

    public WifiLocationOutput(Context mContext) {
        this.mContext = mContext;
        mExecutorService = Executors.newSingleThreadExecutor();
        mXmlSerializer = Xml.newSerializer();
        numOfWifiLocations = 0;

        prepareWriting();
    }

    private void prepareWriting() {
        openFileOutputStream();
        addRequiredGPXDescription();
    }

    public void write(WifiLocation wifiLocation) {
        mExecutorService.execute(() -> {

            double latitude = wifiLocation.getLatitude();
            double longitude = wifiLocation.getLongitude();

            try {
                writeTrackPoint(latitude, longitude);
                writeExtensions(wifiLocation.getScanResults());
                wifiLocation.clearResults();
            } catch (IOException ioException) {
                Log.d(IO_EXCEPTION_THROWN_TAG, IO_EXCEPTION_THROWN_MESSAGE);
                return;
            }
        });
    }

    private void writeTrackPoint(double latitude, double longitude) throws IOException {
        String time = LocalDateTime.now().toString();

        mXmlSerializer
                .startTag(EMPTY_STRING, TRACK_POINT_TAG)
                .attribute(EMPTY_STRING, LATITUDE_ATTRIBUTE, String.valueOf(latitude))
                .attribute(EMPTY_STRING, LONGITUDE_ATTRIBUTE, String.valueOf(longitude))
                .startTag(EMPTY_STRING, TIME_TAG)
                .text(time)
                .endTag(EMPTY_STRING, TIME_TAG);
    }

    private void writeExtensions(List<ScanResult> scanResults) throws IOException {
        mXmlSerializer.startTag(EMPTY_STRING, EXTENSIONS_TAG);
        if (scanResults != null) {
            numOfWifiLocations += scanResults.size();

            for (ScanResult scanResult : scanResults) {
                mXmlSerializer.setPrefix(WIFI_NAMESPACE_PREFIX, WIFI_SCHEMA);
                mXmlSerializer.startTag(WIFI_SCHEMA, WIFI_TAG);
                mXmlSerializer.startTag(WIFI_SCHEMA, BSSID_TAG)
                        .text(scanResult.BSSID)
                        .endTag(WIFI_SCHEMA, BSSID_TAG)
                        .startTag(WIFI_SCHEMA, RSSI_TAG)
                        .text(String.valueOf(scanResult.level))
                        .endTag(WIFI_SCHEMA, RSSI_TAG)
                        .startTag(WIFI_SCHEMA, SSID_TAG)
                        .text(scanResult.SSID)
                        .endTag(WIFI_SCHEMA, SSID_TAG)
                        .startTag(WIFI_SCHEMA, CAPABILITIES_TAG)
                        .text(scanResult.capabilities)
                        .endTag(WIFI_SCHEMA, CAPABILITIES_TAG)
                        .startTag(WIFI_SCHEMA, FREQUENCY_TAG)
                        .text(String.valueOf(scanResult.frequency))
                        .endTag(WIFI_SCHEMA, FREQUENCY_TAG)
                        .endTag(WIFI_SCHEMA, WIFI_TAG);
            }
        }
        mXmlSerializer.endTag(EMPTY_STRING, EXTENSIONS_TAG);
        mXmlSerializer.endTag(EMPTY_STRING, TRACK_POINT_TAG);
    }

    public void openFileOutputStream() {
        try {
            mFileOutputStream = mContext.openFileOutput(FILE_NAME, MODE_APPEND);
        } catch (FileNotFoundException e) {
            Log.d(FILE_NOT_FOUND_EXCEPTION_TAG, FILE_NOT_FOUND_EXCEPTION_OUTPUT_MSG);
        }
    }

    public void closeFileOutputStream() {
        try {
            mXmlSerializer.flush();
            mXmlSerializer.endDocument();
            mFileOutputStream.close();
        } catch (IOException e) {
            Log.d(IO_EXCEPTION_THROWN_TAG, IO_EXCEPTION_THROWN_MESSAGE);
        }
    }

    public void stopExecutorService() {
        mExecutorService.shutdown();
    }

    public int getNumOfWifiLocations() {
        return numOfWifiLocations;
    }

    private void addRequiredGPXDescription() {
        try {
            mXmlSerializer.setOutput(mFileOutputStream, null);
            mXmlSerializer.startDocument(ENCODING, false);
            mXmlSerializer.setPrefix(WIFI_NAMESPACE_PREFIX, WIFI_SCHEMA);
            mXmlSerializer.startTag(EMPTY_STRING, GPX_TAG)
                    .attribute(EMPTY_STRING, XMLNS_ATTRIBUTE, GPX_NAMESPACE)
                    .attribute(EMPTY_STRING, XMLNS_XSI, XML_INSTANCE)
                    .attribute(EMPTY_STRING, VERSION, GPX_VERSION)
                    .attribute(EMPTY_STRING, CREATOR, CREATOR_NAME)
                    .attribute(EMPTY_STRING, XSI_SCHEMA_LOCATION, GPX_SCHEMA_LOCATION)
                    .text(EMPTY_STRING);
            mXmlSerializer.startTag(EMPTY_STRING, TRACK_TAG);
            mXmlSerializer.startTag(EMPTY_STRING, TRACK_SEGMENT_TAG);
        } catch (IOException e) {
            Log.d(IO_EXCEPTION_THROWN_TAG, IO_EXCEPTION_INPUT_MSG);
        }
    }
}