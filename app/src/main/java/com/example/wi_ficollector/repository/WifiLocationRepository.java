package com.example.wi_ficollector.repository;

import android.content.Context;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.util.Log;
import android.util.Xml;

import com.example.wi_ficollector.wrapper.WifiLocation;

import org.xmlpull.v1.XmlSerializer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.time.LocalDateTime;

import java.util.List;

import static android.content.Context.MODE_APPEND;
import static com.example.wi_ficollector.utils.Constants.*;

// todo service
// todo finalize
public class WifiLocationRepository {

    private FileOutputStream mFileOutputStream;
    private WifiLocation mWifiLocation;
    private boolean isAppLaunched;
    boolean isSet;
    private boolean isStreamerOpened;
    private XmlSerializer serializer;
    private boolean areTagsAdded;
    private Context mContext;

    public WifiLocationRepository(WifiLocation wifiLocation, boolean isAppLaunched) {
        this.mWifiLocation = wifiLocation;
        this.isAppLaunched = isAppLaunched;
        this.serializer = Xml.newSerializer();
        areTagsAdded = false;
        isSet = false;
        isStreamerOpened = false;
    }

    public WifiLocationRepository() {

    }

    public void saveWiFiLocation(Context context) throws IOException {
        mContext = context;
        if (!isStreamerOpened) {
            openFileOutputStream();
            isStreamerOpened = true;
        }
        Location location = mWifiLocation.getLocation();

        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            List<ScanResult> scanResults = mWifiLocation.getScanResults();

            if (isFileEmpty()) {
                addGPXDeclaration();
                serializer.setOutput(mFileOutputStream, null);
                isSet = true;
                addBasicGPXTags(latitude, longitude, scanResults);
                areTagsAdded = true;
                isAppLaunched = false;
            } else {
                if (isAppLaunched) {
                    closetags();
                    addBasicGPXTags(latitude, longitude, scanResults);
                    isSet = true;
                    areTagsAdded = true;
                    isAppLaunched = false;
                } else {
                    addBasicGPXTags(latitude, longitude, scanResults);
                }
                //      addTrackPoints(latitude, longitude, scanResults);
            }
        }
    }

    private boolean areLocationLatitudeValid(double latitude, double longitude) {
        return (latitude != 0.0 && longitude != 0.0);
    }

    private boolean isFileEmpty() throws IOException {
        FileInputStream fileInputStream = mContext.openFileInput(FILE_NAME);
        FileChannel channel = fileInputStream.getChannel();

        return channel.size() == 0;
    }

    private void addGPXDeclaration() throws IOException {
        serializer.setOutput(mFileOutputStream, ENCODING);
        serializer.startDocument(ENCODING, false);
        serializer.startTag(NO_NAMESPACE, GPX_TAG)
                .attribute(NO_NAMESPACE, XMLNS_ATTRIBUTE, GPX_NAMESPACE)
                .attribute(NO_NAMESPACE, XMLNS_XSI, XML_INSTANCE)
                .attribute(NO_NAMESPACE, XSI_SCHEMA_LOCATION, SCHEMA_LOCATION);
        serializer.endDocument();
    }

    private void addBasicGPXTags(double latitude, double longitude, List<ScanResult> scanResults) throws IOException {
        if (!isSet) {
            serializer.setOutput(mFileOutputStream, null);
        }
        if (!areTagsAdded) {
            serializer.startTag(NO_NAMESPACE, TRACK_TAG);
            serializer.startTag(NO_NAMESPACE, TRACK_SEGMENT_TAG);
        }
        LocalDateTime now = LocalDateTime.now();
        serializer.startTag(NO_NAMESPACE, TRACK_POINT_TAG)
                .attribute(NO_NAMESPACE, LATITUDE, String.valueOf(latitude))
                .attribute(NO_NAMESPACE, LONGITUDE, String.valueOf(longitude));
        serializer.startTag(NO_NAMESPACE, TIME_TAG);
        serializer.text(String.valueOf(now));
        serializer.endTag(NO_NAMESPACE, TIME_TAG);
        serializer.startTag(NO_NAMESPACE, EXTENSIONS_TAG);
        if (scanResults != null) {
            for (ScanResult scanResult : scanResults) {
                serializer.startTag(NO_NAMESPACE, WIFI_TAG);
                serializer.startTag(NO_NAMESPACE, SSID_TAG)
                        .text(scanResult.SSID)
                        .endTag(NO_NAMESPACE, SSID_TAG)
                        .startTag(NO_NAMESPACE, BSSID_TAG)
                        .text(scanResult.BSSID).endTag(NO_NAMESPACE, BSSID_TAG)
                        .startTag(NO_NAMESPACE, RSSI_TAG)
                        .text(String.valueOf(scanResult.level))
                        .endTag(NO_NAMESPACE, RSSI_TAG)
                        .startTag(NO_NAMESPACE, FREQUENCY_TAG)
                        .text(String.valueOf(scanResult.frequency))
                        .endTag(NO_NAMESPACE, FREQUENCY_TAG)
                        .endTag(NO_NAMESPACE, WIFI_TAG);
            }
        }
        serializer.endTag(NO_NAMESPACE, EXTENSIONS_TAG)
                .endTag(NO_NAMESPACE, TRACK_POINT_TAG);
        //  if (areTagsAdded) {
        serializer.flush();
        int i = 0;
        // }
    }

    private void addTrackPoints(double latitude, double longitude, List<ScanResult> scanResults) throws IOException {
        LocalDateTime now = LocalDateTime.now();
        serializer.setOutput(mFileOutputStream, ENCODING);
        serializer.startTag(NO_NAMESPACE, TRACK_POINT_TAG)
                .attribute(NO_NAMESPACE, LATITUDE, String.valueOf(latitude))
                .attribute(NO_NAMESPACE, LONGITUDE, String.valueOf(longitude));
        serializer.startTag(NO_NAMESPACE, TIME_TAG);
        serializer.text(String.valueOf(now));
        serializer.endTag(NO_NAMESPACE, TIME_TAG);
        addExtensionsWifiNetwork(scanResults);
    }

    private void addExtensionsWifiNetwork(List<ScanResult> scanResults) throws IOException {
        serializer.startTag(NO_NAMESPACE, EXTENSIONS_TAG);
        if (scanResults != null) {
            for (ScanResult scanResult : scanResults) {
                serializer.startTag(NO_NAMESPACE, WIFI_TAG);
                serializer.startTag(NO_NAMESPACE, SSID_TAG)
                        .text(scanResult.SSID)
                        .endTag(NO_NAMESPACE, SSID_TAG)
                        .startTag(NO_NAMESPACE, BSSID_TAG)
                        .text(scanResult.BSSID).endTag(NO_NAMESPACE, BSSID_TAG)
                        .startTag(NO_NAMESPACE, RSSI_TAG)
                        .text(String.valueOf(scanResult.level))
                        .endTag(NO_NAMESPACE, RSSI_TAG)
                        .startTag(NO_NAMESPACE, FREQUENCY_TAG)
                        .text(String.valueOf(scanResult.frequency))
                        .endTag(NO_NAMESPACE, FREQUENCY_TAG)
                        .endTag(NO_NAMESPACE, WIFI_TAG);
            }
        }
        serializer.endTag(NO_NAMESPACE, EXTENSIONS_TAG)
                .endTag(NO_NAMESPACE, TRACK_POINT_TAG);
        //serializer.endDocument();
    }

    public void openFileOutputStream() {
        try {
            mFileOutputStream = mContext.openFileOutput(FILE_NAME, MODE_APPEND);
        } catch (FileNotFoundException exception) {
            Log.d(FILE_NOT_FOUND_EXCEPTION_TAG, FILE_NOT_FOUND_EXCEPTION_MESSAGE);
        }
    }

    public void closeFileOutputStream() {
        try {
            mFileOutputStream.close();
        } catch (IOException e) {
            Log.d(IO_EXCEPTION_THROWN_TAG, IO_EXCEPTION_THROWN_MESSAGE);
        }
        isStreamerOpened = false;
    }

    synchronized void closetags() {
        try {
            serializer.setOutput(mFileOutputStream, null);
            serializer.endDocument();
        } catch (IOException i) {

        }
    }
}