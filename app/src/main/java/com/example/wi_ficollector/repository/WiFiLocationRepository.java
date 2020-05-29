package com.example.wi_ficollector.repository;

import android.location.Location;
import android.net.wifi.ScanResult;

import com.example.wi_ficollector.wrapper.WiFiLocation;

import org.alternativevision.gpx.GPXParser;
import org.alternativevision.gpx.beans.GPX;
import org.alternativevision.gpx.beans.Track;
import org.alternativevision.gpx.beans.Waypoint;


import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public class WiFiLocationRepository {

    private static WiFiLocationRepository wiFiLocationRepository;
    private GPX gpx;
    private GPXParser gpxParser;
    private ExtensionParser extensionParser;

    public WiFiLocationRepository() {
        gpx = new GPX();
        gpxParser = new GPXParser();
        extensionParser = new ExtensionParser();
    }

    public void saveLocation(FileOutputStream out) throws IOException, TransformerException, ParserConfigurationException {
        Track track = new Track();
        Waypoint waypoint = new Waypoint();
        ArrayList<Waypoint> wayPoints = new ArrayList<>();
        TimeZone.setDefault(new SimpleTimeZone(0, "UTC"));
        Date date = new Date();
        double latitude = WiFiLocation.getLatitude();
        double longitude = WiFiLocation.getLongitude();
       // HashMap<String, Object> results = new HashMap<>();
       // if (WiFiLocation.getScanResults() != null) {
         //  for (ScanResult scanResult : WiFiLocation.getScanResults()) {
           //     results.put("SSID", scanResult.SSID);
             //   results.put("RSSI", scanResult.level);
               // results.put("BSSID", scanResult.BSSID);
               // results.put("capabilities", scanResult.capabilities);
           // }
       // }
       // waypoint.setExtensionData(results);
 //       waypoint.setTime(date);
        waypoint.setLatitude(latitude);
        waypoint.setLongitude(longitude);
        wayPoints.add(waypoint);
        track.setTrackPoints(wayPoints);
        gpx.addTrack(track);
        gpxParser.addExtensionParser(extensionParser);
        gpxParser.writeGPX(gpx, out);
    }
}