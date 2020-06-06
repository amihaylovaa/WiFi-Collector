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
import java.nio.channels.FileChannel;
import java.time.LocalDateTime;

import java.util.List;

import static android.content.Context.MODE_APPEND;
import static com.example.wi_ficollector.utils.Constants.*;

public class WifiLocationRepository {

    private FileOutputStream mFileOutputStream;
    private WifiLocation mWifiLocation;
    private boolean isAppLaunched;
    private XmlSerializer serializer;
    private Context mContext;

    public WifiLocationRepository(boolean isAppLaunched) {
        this.mWifiLocation = WifiLocation.getWifiLocation();
        this.isAppLaunched = isAppLaunched;
        serializer = Xml.newSerializer();
    }

    public synchronized void saveWiFiLocation(Context context) throws IOException {
        mContext = context;
        Location location = mWifiLocation.getLocation();

        if (location != null) {
            openFileOutputStream();

            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            List<ScanResult> scanResults = mWifiLocation.getScanResults();

            if (isFileEmpty()) {
                addGPXDeclaration();
                addBasicGPXTags(latitude, longitude, scanResults);
            } else {
                saveWifiLocationInNonEmptyFile(latitude, longitude, scanResults);
            }
            mWifiLocation.clearFields();
            closeFileOutputStream();
        }
    }

    private void saveWifiLocationInNonEmptyFile(double latitude, double longitude, List<ScanResult> scanResults) throws IOException {
        if (isAppLaunched) {
            if (areLocationLatitudeValid(latitude, longitude)) {
                addBasicGPXTags(latitude, longitude, scanResults);
            }
        } else {
            if (areLocationLatitudeValid(latitude, longitude)) {
                addTrackPoints(latitude, longitude, scanResults);
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
                // todo check what is it
                .attribute(NO_NAMESPACE, XML_NAMESPACE, GPX_NAMESPACE)
                .attribute(NO_NAMESPACE, XMLNS_XSI, XML_INSTANCE)
                .attribute(NO_NAMESPACE, XSI_SCHEMA_LOCATION, SCHEMA_LOCATION);
        serializer.endDocument();

        isAppLaunched = false;
    }

    private void addBasicGPXTags(double latitude, double longitude, List<ScanResult> scanResults) throws IOException {
        LocalDateTime now = LocalDateTime.now();

        serializer.setOutput(mFileOutputStream, ENCODING);
        serializer.startTag(NO_NAMESPACE, TRACK_TAG);
        serializer.startTag(NO_NAMESPACE, TRACK_SEGMENT_TAG);
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
                serializer.startTag(NO_NAMESPACE, SSID_TAG).text(scanResult.SSID).endTag(NO_NAMESPACE, SSID_TAG);
                serializer.startTag(NO_NAMESPACE, BSSID_TAG).text(scanResult.BSSID).endTag(NO_NAMESPACE, BSSID_TAG);
                serializer.startTag(NO_NAMESPACE, RSSI_TAG).text(String.valueOf(scanResult.level)).endTag(NO_NAMESPACE, RSSI_TAG);
                serializer.startTag(NO_NAMESPACE, FREQUENCY_TAG).text(String.valueOf(scanResult.frequency)).endTag(NO_NAMESPACE, FREQUENCY_TAG);
                serializer.endTag(NO_NAMESPACE, WIFI_TAG);
            }
        }
        serializer.endTag(NO_NAMESPACE, EXTENSIONS_TAG);
        serializer.endTag(NO_NAMESPACE, TRACK_POINT_TAG);
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
        addWifiNetworks(scanResults);
    }

    private void addWifiNetworks(List<ScanResult> scanResults) throws IOException {
        serializer.startTag(NO_NAMESPACE, EXTENSIONS_TAG);
        if (scanResults != null) {
            for (ScanResult scanResult : scanResults) {
                serializer.startTag(NO_NAMESPACE, WIFI_TAG);
                serializer.startTag(NO_NAMESPACE, SSID_TAG).text(scanResult.SSID).endTag(NO_NAMESPACE, SSID_TAG);
                serializer.startTag(NO_NAMESPACE, BSSID_TAG).text(scanResult.BSSID).endTag(NO_NAMESPACE, BSSID_TAG);
                serializer.startTag(NO_NAMESPACE, RSSI_TAG).text(String.valueOf(scanResult.level)).endTag(NO_NAMESPACE, RSSI_TAG);
                serializer.startTag(NO_NAMESPACE, FREQUENCY_TAG).text(String.valueOf(scanResult.frequency)).endTag(NO_NAMESPACE, FREQUENCY_TAG);
                serializer.endTag(NO_NAMESPACE, WIFI_TAG);
            }
        }
        serializer.endTag(NO_NAMESPACE, EXTENSIONS_TAG);
        serializer.endTag(NO_NAMESPACE, TRACK_POINT_TAG);
        serializer.endDocument();
    }

    public WifiLocation getWifiLocation() {
        return mWifiLocation;
    }

    private void openFileOutputStream() {
        try {
            mFileOutputStream = mContext.openFileOutput(FILE_NAME, MODE_APPEND);
        } catch (FileNotFoundException exception) {
            Log.d(FILE_NOT_FOUND_EXCEPTION_TAG, FILE_NOT_FOUND_EXCEPTION_MESSAGE);
        }
    }

    private void closeFileOutputStream() {
        try {
            mFileOutputStream.close();
        } catch (IOException e) {
            Log.d(IO_EXCEPTION_THROWN_TAG, IO_EXCEPTION_THROWN_MESSAGE);
        }
    }
}