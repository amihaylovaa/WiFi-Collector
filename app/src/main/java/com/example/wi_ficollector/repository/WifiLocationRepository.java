package com.example.wi_ficollector.repository;

import android.content.Context;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.util.Log;

import com.example.wi_ficollector.wrapper.WifiLocation;

import org.alternativevision.gpx.GPXParser;
import org.alternativevision.gpx.beans.GPX;
import org.alternativevision.gpx.beans.Track;
import org.alternativevision.gpx.beans.Waypoint;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import static com.example.wi_ficollector.utils.Constants.FILE_NAME;

public class WifiLocationRepository {

    private GPX mGPX;
    private GPXParser mGPXParser;
    private ExtensionParser mExtensionParser;
    private WifiLocation mWifiLocation;

    public WifiLocationRepository(WifiLocation mWifiLocation) {
        mGPX = new GPX();
        mGPXParser = new GPXParser();
        mExtensionParser = new ExtensionParser();
        this.mWifiLocation = WifiLocation.getWifiLocation();
    }

    public synchronized void saveWiFiLocation(FileOutputStream fileOutputStream, Context context) throws TransformerException, ParserConfigurationException, IOException {
      /*  Track track = new Track();
        Waypoint waypoint = new Waypoint();
        ArrayList<Waypoint> wayPoints = new ArrayList<>();
        TimeZone.setDefault(new SimpleTimeZone(0, "UTC"));
        Date date = new Date();

        HashMap<String, Object> wiFiScanResults = createWiFiScanResultsMap();


        waypoint.setExtensionData(wiFiScanResults);
        waypoint.setTime(date);
        waypoint.setLatitude(latitude);
        waypoint.setLongitude(longitude);
        wayPoints.add(waypoint);
        track.setTrackPoints(wayPoints);
        mGPX.addTrack(track);
        mGPXParser.addExtensionParser(mExtensionParser);
        mGPXParser.writeGPX(mGPX, fileOutputStream);
        mWifiLocation.clearFields();*/
        Location location = mWifiLocation.getLocation();
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        OutputStreamWriter osw = new OutputStreamWriter(context.openFileOutput(FILE_NAME, context.MODE_APPEND));
        String fileContent = "Latitude :" + latitude + "\n Longitude : " + longitude + "\n";
        LocalDateTime localDateTime = LocalDateTime.now();
        try {
            osw.write(fileContent);
            osw.write(String.valueOf(localDateTime));
            osw.write("\n\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        osw.flush();

        Log.d("Storing data", String.valueOf(mWifiLocation.getScanResults().size()));

        for (ScanResult scanResult : mWifiLocation.getScanResults()) {
            String file = "\nSSID: :" + scanResult.SSID + "\nFrequency : " + scanResult.frequency + "\n";
            try {
                osw.write(file);
                osw.write("\n\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
            osw.flush();
        }
        osw.close();
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

    public WifiLocation getmWifiLocation() {
        return mWifiLocation;
    }
}