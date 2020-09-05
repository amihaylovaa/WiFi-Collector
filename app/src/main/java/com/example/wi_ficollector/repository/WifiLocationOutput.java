package com.example.wi_ficollector.repository;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.util.Log;
import android.util.Xml;

import com.example.wi_ficollector.wrapper.WifiLocation;

import org.xmlpull.v1.XmlSerializer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.content.Context.MODE_APPEND;
import static com.example.wi_ficollector.utility.Constants.*;

public class WifiLocationOutput implements OutputOperation {

    private boolean isOutputSet;
    private boolean areTrackTagsAdded;
    private int numOfWifiLocations;
    private Context mContext;
    private FileOutputStream mFileOutputStream;
    private XmlSerializer mXmlSerializer;
    private ExecutorService mExecutorService;
    private static final String XMLNS_ATTRIBUTE;
    private static final String GPX_NAMESPACE;
    private static final String XSI_SCHEMA_LOCATION;
    private static final String SCHEMA_LOCATION;
    private static final String XMLNS_XSI;
    private static final String XML_INSTANCE;
    private static final String WIFI_NAMESPACE_PREFIX;
    private static final String WIFI_NAMESPACE;

    static {
        XMLNS_ATTRIBUTE = "xmlns";
        GPX_NAMESPACE = "http://www.topografix.com/GPX/1/1";
        XSI_SCHEMA_LOCATION = "xsi:schemaLocation";
        SCHEMA_LOCATION = "http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd src/main/res/xml/wifi_schema.xsd";
        XMLNS_XSI = "xmlns:xsi";
        WIFI_NAMESPACE = "src/main/res/xml/wifi_schema.xsd";
        WIFI_NAMESPACE_PREFIX = "wifi";
        XML_INSTANCE = "http://www.w3.org/2001/XMLSchema-instance";
    }

    public WifiLocationOutput(Context mContext) {
        this.mContext = mContext;
        this.mExecutorService = Executors.newSingleThreadExecutor();
        this.mXmlSerializer = Xml.newSerializer();
        this.isOutputSet = false;
        this.numOfWifiLocations = 0;

        openFileOutputStream();
    }

    @Override
    public void write(WifiLocation wifiLocation, List<ScanResult> scanResults) {
        mExecutorService.execute(() -> {

            double latitude = wifiLocation.getLatitude();
            double longitude = wifiLocation.getLongitude();

            try {
                prepareWriting();
                writeTrackPoint(latitude, longitude);
                writeExtensions(scanResults);
                wifiLocation.clearResults();
            } catch (IOException IOException) {
                Log.d(IO_EXCEPTION_THROWN_TAG, IO_EXCEPTION_THROWN_MESSAGE);

                return;
            }
        });
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
            // TODO fix bug
        //    mXmlSerializer.endTag(EMPTY_STRING, GPX_TAG);
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

    private void prepareWriting() throws IOException {
        if (isFileEmpty()) {
            addGPXDeclaration();
            setOutput();
        }
        if (!isOutputSet) {
            setOutput();
        }
        if (!areTrackTagsAdded) {
            mXmlSerializer.startTag(EMPTY_STRING, TRACK_TAG)
                    .startTag(EMPTY_STRING, TRACK_SEGMENT_TAG);

            areTrackTagsAdded = true;
        }
    }

    private void setOutput() {
        try {
            mXmlSerializer.setOutput(mFileOutputStream, null);
        } catch (IOException e) {
            isOutputSet = false;
            return;
        }
        isOutputSet = true;
    }

    private boolean isFileEmpty() throws IOException {
        FileInputStream fileInputStream;

        try {
            fileInputStream = mContext.openFileInput(FILE_NAME);
        } catch (FileNotFoundException e) {
            Log.d(FILE_NOT_FOUND_EXCEPTION_TAG, FILE_NOT_FOUND_EXCEPTION_MSG);
            return true;
        }
        FileChannel channel = fileInputStream.getChannel();

        return channel.size() == 0;
    }

    private void addGPXDeclaration() throws IOException {
        mXmlSerializer.setOutput(mFileOutputStream, ENCODING);
        mXmlSerializer.startDocument(ENCODING, false);
        mXmlSerializer.setPrefix("gpx", GPX_NAMESPACE);
        mXmlSerializer.setPrefix(WIFI_NAMESPACE_PREFIX, WIFI_NAMESPACE);
        mXmlSerializer.startTag(EMPTY_STRING, GPX_TAG)
                .attribute(EMPTY_STRING, XMLNS_XSI, XML_INSTANCE)
                .attribute(EMPTY_STRING, XSI_SCHEMA_LOCATION, SCHEMA_LOCATION);
        mXmlSerializer.flush();
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
                mXmlSerializer.setPrefix(WIFI_NAMESPACE_PREFIX, WIFI_NAMESPACE);
                mXmlSerializer.startTag(WIFI_NAMESPACE, WIFI_TAG);
                mXmlSerializer.startTag(WIFI_NAMESPACE, BSSID_TAG)
                        .text(scanResult.BSSID)
                        .endTag(WIFI_NAMESPACE, BSSID_TAG)
                        .startTag(WIFI_NAMESPACE, RSSI_TAG)
                        .text(String.valueOf(scanResult.level))
                        .endTag(WIFI_NAMESPACE, RSSI_TAG)
                        .startTag(WIFI_NAMESPACE, SSID_TAG)
                        .text(scanResult.SSID)
                        .endTag(WIFI_NAMESPACE, SSID_TAG)
                        .startTag(WIFI_NAMESPACE, CAPABILITIES_TAG)
                        .text(scanResult.capabilities)
                        .endTag(WIFI_NAMESPACE, CAPABILITIES_TAG)
                        .startTag(WIFI_NAMESPACE, FREQUENCY_TAG)
                        .text(String.valueOf(scanResult.frequency))
                        .endTag(WIFI_NAMESPACE, FREQUENCY_TAG)
                        .endTag(WIFI_NAMESPACE, WIFI_TAG);
            }
        }
        mXmlSerializer.endTag(EMPTY_STRING, EXTENSIONS_TAG);
        mXmlSerializer.endTag(EMPTY_STRING, TRACK_POINT_TAG);
        mXmlSerializer.flush();
    }
}