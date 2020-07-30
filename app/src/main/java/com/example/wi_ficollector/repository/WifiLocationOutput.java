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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static android.content.Context.MODE_APPEND;
import static com.example.wi_ficollector.utils.Constants.*;

public class WifiLocationOutput implements OutputOperation {

    private FileOutputStream mFileOutputStream;
    private boolean isOutputSet;
    private XmlSerializer serializer;
    private Context mContext;
    private Executor mExecutor;
    private boolean areBasicTagsAdded;
    private int numOfWifiLocations;

    public WifiLocationOutput(Context mContext) {
        this.mContext = mContext;
        this.mExecutor = Executors.newSingleThreadExecutor();
        this.serializer = Xml.newSerializer();
        this.isOutputSet = false;
        this.numOfWifiLocations = 0;
        openFileOutputStream();
    }

    @Override
    public void write(WifiLocation wifiLocation, List<ScanResult> scanResults) {
        mExecutor.execute(() -> {

            double latitude = wifiLocation.getLatitude();
            double longitude = wifiLocation.getLongitude();

            try {
                prepareWrite();
                writeTrackPoint(latitude, longitude);
                writeExtensions(scanResults);
                wifiLocation.clearResults();
            } catch (IOException IOException) {
                Log.d(IO_EXCEPTION_THROWN_TAG, IO_EXCEPTION_THROWN_MESSAGE);
            }
        });
    }

    private void prepareWrite() throws IOException {
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
        serializer.flush();
    }

    private void writeTrackPoint(double latitude, double longitude) throws IOException {
        String time = LocalDateTime.now().toString();

        serializer.startTag(NO_NAMESPACE, TRACK_POINT_TAG)
                .attribute(NO_NAMESPACE, LATITUDE, String.valueOf(latitude))
                .attribute(NO_NAMESPACE, LONGITUDE, String.valueOf(longitude))
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
        serializer.endTag(NO_NAMESPACE, EXTENSIONS_TAG);
        serializer.endTag(NO_NAMESPACE, TRACK_POINT_TAG);
        serializer.flush();
    }

    // todo think for other way
    public void openFileOutputStream() {
        try {
            mFileOutputStream = mContext.openFileOutput(FILE_NAME, MODE_APPEND);
        } catch (FileNotFoundException exception) {
            Log.d(FILE_NOT_FOUND_EXCEPTION_TAG, FILE_NOT_FOUND_EXCEPTION_MSG);
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

    public int getNumOfWifiLocations() {
        return numOfWifiLocations;
    }
}