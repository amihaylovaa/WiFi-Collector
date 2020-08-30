package com.example.wi_ficollector.http;

import android.util.Log;

import org.json.JSONArray;

import java.io.IOException;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;

import static com.example.wi_ficollector.utility.Constants.ACCEPT;
import static com.example.wi_ficollector.utility.Constants.CONNECTION_RESET_EXCEPTION_MESSAGE;
import static com.example.wi_ficollector.utility.Constants.CONTENT_TYPE;
import static com.example.wi_ficollector.utility.Constants.HOST;
import static com.example.wi_ficollector.utility.Constants.ZERO_INTEGER;
import static com.example.wi_ficollector.utility.Constants.IO_EXCEPTION_THROWN_MESSAGE;
import static com.example.wi_ficollector.utility.Constants.IO_EXCEPTION_THROWN_TAG;
import static com.example.wi_ficollector.utility.Constants.NEGATIVE_INTEGER;
import static com.example.wi_ficollector.utility.Constants.PATH;
import static com.example.wi_ficollector.utility.Constants.PORT;
import static com.example.wi_ficollector.utility.Constants.PROTOCOL;
import static com.example.wi_ficollector.utility.Constants.TEN_SECONDS;
import static com.example.wi_ficollector.utility.Constants.TYPE;

public class HttpRequest {

    private OutputStream mOutputStream;
    private HttpURLConnection mHttpUrlConnection;

    public int send(JSONArray wifiLocations) {
        byte[] bytes = wifiLocations.toString().getBytes();
        URL mURL;

        try {
            mURL = new URL(PROTOCOL, HOST, PORT, PATH);
        } catch (MalformedURLException e) {
            return HttpURLConnection.HTTP_NOT_FOUND;
        }

        try {
            mHttpUrlConnection = (HttpURLConnection) mURL.openConnection();
        } catch (IOException e) {
            return HttpURLConnection.HTTP_NOT_FOUND;
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
            mOutputStream.flush();
        } catch (SocketException e) {
            mHttpUrlConnection.disconnect();
            return NEGATIVE_INTEGER;
        } catch (IOException e) {
            return NEGATIVE_INTEGER;
        }

        return getResponseCode();
    }

    public int getResponseCode() {
        int responseCode;

        try {
            responseCode = mHttpUrlConnection.getResponseCode();
        } catch (SocketException e) {
            if (e.getMessage().equals(CONNECTION_RESET_EXCEPTION_MESSAGE)) {
                mHttpUrlConnection.disconnect();
                return HttpURLConnection.HTTP_INTERNAL_ERROR;
            } else {
                mHttpUrlConnection.disconnect();
                return ZERO_INTEGER;
            }
        } catch (IOException e) {
            mHttpUrlConnection.disconnect();
            return ZERO_INTEGER;
        }

        if (responseCode == HttpURLConnection.HTTP_OK) {
            try {
                mOutputStream.close();
                mHttpUrlConnection.disconnect();
            } catch (IOException e) {
                Log.d(IO_EXCEPTION_THROWN_TAG, IO_EXCEPTION_THROWN_MESSAGE);
            }
        } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
            return HttpURLConnection.HTTP_NOT_FOUND;
        } else {
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        }
        return responseCode;
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