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

    private FileOutputStream mFileOutputStream;
    private boolean isOutputSet;
    private XmlSerializer serializer;
    private Context mContext;
    private ExecutorService mExecutorService;
    private boolean areBasicTagsAdded;
    private int numOfWifiLocations;

    public WifiLocationOutput(Context mContext) {
        this.mContext = mContext;
        this.mExecutorService = Executors.newSingleThreadExecutor();
        this.serializer = Xml.newSerializer();
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
            serializer.endDocument();
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
        if (!areBasicTagsAdded) {
            serializer.startTag(NO_NAMESPACE, TRACK_TAG)
                    .startTag(NO_NAMESPACE, TRACK_SEGMENT_TAG);

            areBasicTagsAdded = true;
        }
    }

    private void setOutput() {
        try {
            serializer.setOutput(mFileOutputStream, null);
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
        serializer.setOutput(mFileOutputStream, ENCODING);
        serializer.startDocument(ENCODING, false);
        serializer.startTag(NO_NAMESPACE, GPX_TAG)
                .attribute(NO_NAMESPACE, XMLNS_ATTRIBUTE, GPX_NAMESPACE)
                .attribute(NO_NAMESPACE, XMLNS_XSI, XML_INSTANCE)
                .attribute(NO_NAMESPACE, XSI_SCHEMA_LOCATION, SCHEMA_LOCATION);
        serializer.flush();
    }

    private void writeTrackPoint(double latitude, double longitude) throws IOException {
        String time = LocalDateTime.now().toString();

        serializer
                .startTag(NO_NAMESPACE, TRACK_POINT_TAG)
                .attribute(NO_NAMESPACE, LATITUDE_ATTRIBUTE, String.valueOf(latitude))
                .attribute(NO_NAMESPACE, LONGITUDE_ATTRIBUTE, String.valueOf(longitude))
                .startTag(NO_NAMESPACE, TIME_TAG)
                .text(time)
                .endTag(NO_NAMESPACE, TIME_TAG);
    }

    private void writeExtensions(List<ScanResult> scanResults) throws IOException {
        serializer.startTag(NO_NAMESPACE, EXTENSIONS_TAG);
        if (scanResults != null) {
            numOfWifiLocations += scanResults.size();

            for (ScanResult scanResult : scanResults) {
                serializer.startTag(NO_NAMESPACE, WIFI_TAG);
                serializer.startTag(NO_NAMESPACE, BSSID_TAG)
                        .text(scanResult.BSSID)
                        .endTag(NO_NAMESPACE, BSSID_TAG)
                        .startTag(NO_NAMESPACE, RSSI_TAG)
                        .text(String.valueOf(scanResult.level))
                        .endTag(NO_NAMESPACE, RSSI_TAG)
                        .startTag(NO_NAMESPACE, SSID_TAG)
                        .text(scanResult.SSID)
                        .endTag(NO_NAMESPACE, SSID_TAG)
                        .startTag(NO_NAMESPACE, CAPABILITIES_TAG)
                        .text(scanResult.capabilities)
                        .endTag(NO_NAMESPACE, CAPABILITIES_TAG)
                        .startTag(NO_NAMESPACE, FREQUENCY_TAG)
                        .text(String.valueOf(scanResult.frequency))
                        .endTag(NO_NAMESPACE, FREQUENCY_TAG)
                        .endTag(NO_NAMESPACE, WIFI_TAG);
            }
        }
        serializer.endTag(NO_NAMESPACE, EXTENSIONS_TAG);
        serializer.endTag(NO_NAMESPACE, TRACK_POINT_TAG);
        serializer.flush();
    }
}