package com.example.wi_ficollector.repository;

import android.location.Location;

import org.alternativevision.gpx.GPXParser;
import org.alternativevision.gpx.beans.GPX;
import org.alternativevision.gpx.beans.Track;
import org.alternativevision.gpx.beans.Waypoint;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public class WiFiLocationRepository {

    GPX gpx;

    public WiFiLocationRepository() {
        gpx = new GPX();
    }

    public void saveData(Location location, FileOutputStream fileOutputStream) throws IOException, TransformerException, ParserConfigurationException {
        Track track = new Track();
        Waypoint waypoint = new Waypoint();
        ArrayList<Waypoint> wayPoints = new ArrayList<>();
        GPXParser gpxParser = new GPXParser();
        Date date = new Date();
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        waypoint.setTime(date);
        waypoint.setLatitude(latitude);
        waypoint.setLongitude(longitude);
        wayPoints.add(waypoint);
        track.setTrackPoints(wayPoints);
        gpx.addTrack(track);
        gpx.addExtensionData("wifi","Ogi");
        gpxParser.writeGPX(gpx, fileOutputStream);
    }
}
