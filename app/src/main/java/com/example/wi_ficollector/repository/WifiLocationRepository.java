package com.example.wi_ficollector.repository;

import android.content.Context;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.util.Log;
import android.util.Xml;

import com.example.wi_ficollector.wrapper.WifiLocation;

import org.alternativevision.gpx.GPXParser;
import org.alternativevision.gpx.beans.GPX;
import org.alternativevision.gpx.beans.Track;
import org.alternativevision.gpx.beans.Waypoint;
import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import static android.content.Context.MODE_APPEND;
import static com.example.wi_ficollector.utils.Constants.FILE_NAME;
import static com.example.wi_ficollector.utils.Constants.FILE_NOT_FOUND_EXCEPTION_MESSAGE;
import static com.example.wi_ficollector.utils.Constants.FILE_NOT_FOUND_EXCEPTION_TAG;
import static com.example.wi_ficollector.utils.Constants.IO_EXCEPTION_THROWN_MESSAGE;
import static com.example.wi_ficollector.utils.Constants.IO_EXCEPTION_THROWN_TAG;

public class WifiLocationRepository {

    private FileOutputStream mFileOutputStream;
    private WifiLocation mWifiLocation;
    private Context mContext;

    public WifiLocationRepository(WifiLocation mWifiLocation) {
        this.mWifiLocation = WifiLocation.getWifiLocation();
    }

    public synchronized void saveWiFiLocation(Context context) throws IOException {
        mContext = context;
        openFileOutputStream();
        Location location = mWifiLocation.getLocation();
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        List<ScanResult> scanResults = mWifiLocation.getScanResults();
        FileInputStream fIn = mContext.openFileInput(FILE_NAME);
        FileChannel channel = fIn.getChannel();
        if (channel.size() == 0) {
            fIn.close();
            addTags();
        } else {
            addData(latitude, longitude, scanResults);
        }

        mWifiLocation.clearFields();
        closeFileOutputStream();
    }

    private void addTags() throws IOException {
        XmlSerializer serializer = Xml.newSerializer();
        serializer.setOutput(mFileOutputStream, "UTF-8");
        serializer.startDocument("UTF-8", null);
        serializer.startTag("", "gpx")
                .attribute("", "xmlns", "http://www.topografix.com/GPX/1/1")
                .attribute("", "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance")
                .attribute("", "xsi:schemaLocation", "http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd");
        serializer.endTag("", "gpx");
        serializer.endDocument();
    }

    private void addData(double latitude, double longitude, List<ScanResult> scanResults) throws IOException {
        XmlSerializer serializer = Xml.newSerializer();
        serializer.setOutput(mFileOutputStream, "UTF-8");
        serializer.startDocument("", null);
        serializer.startTag("", "trk");
        serializer.startTag("", "trkseg");
        serializer.startTag("", "trkpt")
                .attribute("", "lat", String.valueOf(latitude))
                .attribute("", "lon", String.valueOf(longitude));
        serializer.endTag("", "trkpt");
        serializer.endTag("", "trkseg");
        serializer.endTag("", "trk");
        serializer.endTag("", "gpx");
        serializer.endDocument();
    }

    private HashMap<String, Object> createWiFiScanResultsMap() {
        HashMap<String, Object> results = new HashMap<>();

        if (mWifiLocation.getScanResults() != null) {
            int id = 0;
            for (ScanResult scanResult : mWifiLocation.getScanResults()) {
                results.put("SSID" + id, scanResult.SSID);
                results.put("RSSI" + id, scanResult.level);
                results.put("BSSID" + id, scanResult.BSSID);
                results.put("capabilities" + id, scanResult.capabilities);
                ++id;
            }
        }
        Log.d("Storing data", String.valueOf(results.size()));
        return results;
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