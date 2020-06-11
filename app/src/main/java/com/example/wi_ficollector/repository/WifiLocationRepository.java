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

import static android.content.Context.MODE_APPEND;
import static com.example.wi_ficollector.utils.Constants.*;

// todo service
// todo finalize
public class WifiLocationRepository {

    private FileOutputStream mFileOutputStream;
    private WifiLocation mWifiLocation;
    private boolean areBasicTagsAdded;
    boolean isOutputSet;
    private XmlSerializer serializer;
    private Context mContext;

    public WifiLocationRepository(WifiLocation wifiLocation, Context mContext) {
        this.mWifiLocation = wifiLocation;
        this.mContext = mContext;
        this.serializer = Xml.newSerializer();
        this.areBasicTagsAdded = false;
        this.isOutputSet = false;
        openFileOutputStream();
    }

    public void save() throws IOException {
        double latitude = mWifiLocation.getLatitude();
        double longitude = mWifiLocation.getLongitude();
        List<ScanResult> scanResults = mWifiLocation.getScanResults();
        numberFoundWifiNetworks += scanResults.size();

        if (latitude != ZERO && longitude != ZERO) {
            saveValidWifiLocation(latitude, longitude, scanResults);
            mWifiLocation.clearResults();
        }
    }

    private void saveValidWifiLocation(double latitude, double longitude, List<ScanResult> scanResults) throws IOException {
        if (isFileEmpty()) {
            addGPXDeclaration();
        }
        if (!isOutputSet) {
            setOutput();
        }
        saveWifiLocation(latitude, longitude, scanResults);
    }

    private void setOutput() throws IOException {
        serializer.setOutput(mFileOutputStream, null);
        isOutputSet = true;
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

    private void saveWifiLocation(double latitude, double longitude, List<ScanResult> scanResults) throws IOException {
        if (!areBasicTagsAdded) {
            serializer.startTag(NO_NAMESPACE, TRACK_TAG)
                    .startTag(NO_NAMESPACE, TRACK_SEGMENT_TAG);
            areBasicTagsAdded = true;
        }

        saveTrackPoint(latitude, longitude);
        saveExtensions(scanResults);
    }

    private void saveTrackPoint(double latitude, double longitude) throws IOException {
        String time = LocalDateTime.now().toString();

        serializer.startTag(NO_NAMESPACE, TRACK_POINT_TAG)
                .attribute(NO_NAMESPACE, LATITUDE, String.valueOf(latitude))
                .attribute(NO_NAMESPACE, LONGITUDE, String.valueOf(longitude))
                .startTag(NO_NAMESPACE, TIME_TAG)
                .text(time)
                .endTag(NO_NAMESPACE, TIME_TAG);
    }

    private void saveExtensions(List<ScanResult> scanResults) throws IOException {
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
        serializer.flush();
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
    }

  /*  synchronized void closetags() {
        try {
            serializer.setOutput(mFileOutputStream, null);
            serializer.endDocument();
        } catch (IOException i) {

        }
    }*/
}