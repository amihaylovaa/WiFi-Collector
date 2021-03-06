package com.thesis.wificollector.http;

import android.util.Log;

import com.thesis.wificollector.utility.Constants;

import org.json.JSONArray;

import java.io.IOException;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;

public class HttpRequest {

    private OutputStream mOutputStream;
    private HttpURLConnection mHttpUrlConnection;
    private static final int PORT;
    private static final String PROTOCOL;
    private static final String HOST;
    private static final String PATH;
    private static final int TEN_SECONDS;
    private static final String CONTENT_TYPE;
    private static final String ACCEPT;
    private static final String TYPE;
    private static final String CONNECTION_RESET_EXCEPTION_MESSAGE;

    static {
        PORT = 8282;
        TEN_SECONDS = 10_000;
        PROTOCOL = "http";
        HOST = "10.0.2.2";
        PATH = "/wifi/locations";
        CONTENT_TYPE = "Content-type";
        ACCEPT = "Accept";
        TYPE = "application/json";
        CONNECTION_RESET_EXCEPTION_MESSAGE = "Connection reset";
    }

    public int send(JSONArray wifiLocations) {
        byte[] bytes = wifiLocations.toString().getBytes();
        URL url;

        try {
            url = new URL(PROTOCOL, HOST, PORT, PATH);
        } catch (MalformedURLException e) {
            return Constants.POSITIVE_INTEGER;
        }

        try {
            mHttpUrlConnection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            return Constants.POSITIVE_INTEGER;
        }

        createRequest(bytes);

        try {
            mOutputStream = mHttpUrlConnection.getOutputStream();
        } catch (IOException e) {
            mHttpUrlConnection.disconnect();
            return HttpURLConnection.HTTP_CLIENT_TIMEOUT;
        }

        try {
            mOutputStream.write(bytes);
        } catch (SocketException e) {
            mHttpUrlConnection.disconnect();
            return Constants.NEGATIVE_INTEGER;
        } catch (IOException e) {
            return Constants.NEGATIVE_INTEGER;
        }

        return getRequestResult();
    }

    public int getRequestResult() {
        int result;

        try {
            result = mHttpUrlConnection.getResponseCode();
        } catch (SocketException e) {
            if (e.getMessage().equals(CONNECTION_RESET_EXCEPTION_MESSAGE)) {
                mHttpUrlConnection.disconnect();
                return HttpURLConnection.HTTP_INTERNAL_ERROR;
            } else {
                mHttpUrlConnection.disconnect();
                return Constants.ZERO_INTEGER;
            }
        } catch (IOException e) {
            mHttpUrlConnection.disconnect();
            return Constants.ZERO_INTEGER;
        }

        if (result == HttpURLConnection.HTTP_OK) {
            try {
                mOutputStream.close();
                mHttpUrlConnection.disconnect();
            } catch (IOException e) {
                Log.d(Constants.IO_EXCEPTION_THROWN_TAG, Constants.IO_EXCEPTION_THROWN_MESSAGE);
            }
        }

        return result;
    }

    private void createRequest(byte[] bytes) {
        mHttpUrlConnection.setDoOutput(true);
        mHttpUrlConnection.setFixedLengthStreamingMode(bytes.length);
        mHttpUrlConnection.setRequestProperty(CONTENT_TYPE, TYPE);
        mHttpUrlConnection.setRequestProperty(ACCEPT, TYPE);
        mHttpUrlConnection.setConnectTimeout(TEN_SECONDS);
        mHttpUrlConnection.setReadTimeout(TEN_SECONDS);
    }
}