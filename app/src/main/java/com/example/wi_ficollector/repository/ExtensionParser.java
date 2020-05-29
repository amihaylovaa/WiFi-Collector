package com.example.wi_ficollector.repository;

import org.alternativevision.gpx.beans.GPX;
import org.alternativevision.gpx.beans.Route;
import org.alternativevision.gpx.beans.Track;
import org.alternativevision.gpx.beans.Waypoint;
import org.alternativevision.gpx.extensions.IExtensionParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.HashMap;

public class ExtensionParser implements IExtensionParser {
    @Override
    public String getId() {
        return "123";
    }

    @Override
    public Object parseWaypointExtension(Node node) {
        return null;
    }

    @Override
    public Object parseTrackExtension(Node node) {
        return null;
    }

    @Override
    public Object parseGPXExtension(Node node) {
        return null;
    }

    @Override
    public Object parseRouteExtension(Node node) {
        return null;
    }

    @Override
    public void writeGPXExtensionData(Node node, GPX gpx, Document document) {

    }

    @Override
    public void writeWaypointExtensionData(Node node, Waypoint waypoint, Document document) {
        HashMap<String, Object> extensions = waypoint.getExtensionData();
      //   Node parentNode = document.createElement("WAP");
        extensions.forEach((k, v) -> {
            Node child = document.createElement(k);
            child.setTextContent(v.toString());
          //  parentNode.appendChild(child);
            node.appendChild(child);
        });
    }

    @Override
    public void writeTrackExtensionData(Node node, Track track, Document document) {

    }

    @Override
    public void writeRouteExtensionData(Node node, Route route, Document document) {

    }
}
