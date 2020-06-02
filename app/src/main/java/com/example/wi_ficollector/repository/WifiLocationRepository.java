package com.example.wi_ficollector.repository;

import android.net.wifi.ScanResult;

import com.example.wi_ficollector.wrapper.WifiLocation;

import org.alternativevision.gpx.GPXParser;
import org.alternativevision.gpx.beans.GPX;
import org.alternativevision.gpx.beans.Track;
import org.alternativevision.gpx.beans.Waypoint;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public class WifiLocationRepository {

    private GPX gpx;
    private GPXParser gpxParser;
    private ExtensionParser extensionParser;
    private WifiLocation wifiLocation;

    public WifiLocationRepository(WifiLocation wifiLocation) {
        gpx = new GPX();
        gpxParser = new GPXParser();
        extensionParser = new ExtensionParser();
        this.wifiLocation = wifiLocation;
    }

    public void saveWiFiLocation(FileOutputStream fileOutputStream) throws TransformerException, ParserConfigurationException {
        Track track = new Track();
        Waypoint waypoint = new Waypoint();
        ArrayList<Waypoint> wayPoints = new ArrayList<>();
        TimeZone.setDefault(new SimpleTimeZone(0, "UTC"));
        Date date = new Date();
        double latitude = wifiLocation.getLatitude();
        double longitude = wifiLocation.getLongitude();
        HashMap<String, Object> wiFiScanResults = createWiFiScanResultsMap();

        waypoint.setExtensionData(wiFiScanResults);
        waypoint.setTime(date);
        waypoint.setLatitude(latitude);
        waypoint.setLongitude(longitude);
        wayPoints.add(waypoint);
        track.setTrackPoints(wayPoints);
        gpx.addTrack(track);
        gpxParser.addExtensionParser(extensionParser);
        gpxParser.writeGPX(gpx, fileOutputStream);
    }

    private HashMap<String, Object> createWiFiScanResultsMap() {
        HashMap<String, Object> results = new HashMap<>();

        if (wifiLocation.getScanResults() != null) {
            int id = 0;
            for (ScanResult scanResult : wifiLocation.getScanResults()) {
                results.put("SSID" + id, scanResult.SSID);
                results.put("RSSI" + id, scanResult.level);
                results.put("BSSID" + id, scanResult.BSSID);
                results.put("capabilities" + id, scanResult.capabilities);
                ++id;
            }
        }
        return results;
    }
}