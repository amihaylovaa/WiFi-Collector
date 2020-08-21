package com.example.wi_ficollector.http;

import android.util.Log;

import org.json.JSONArray;

import java.io.IOException;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;

import lombok.Getter;
import lombok.Setter;

import static com.example.wi_ficollector.utility.Constants.ACCEPT;
import static com.example.wi_ficollector.utility.Constants.CONTENT_TYPE;
import static com.example.wi_ficollector.utility.Constants.HOST;
import static com.example.wi_ficollector.utility.Constants.IO_EXCEPTION_THROWN_MESSAGE;
import static com.example.wi_ficollector.utility.Constants.IO_EXCEPTION_THROWN_TAG;
import static com.example.wi_ficollector.utility.Constants.NEGATIVE_ONE;
import static com.example.wi_ficollector.utility.Constants.PATH;
import static com.example.wi_ficollector.utility.Constants.PORT;
import static com.example.wi_ficollector.utility.Constants.PROTOCOL;
import static com.example.wi_ficollector.utility.Constants.TEN_SECONDS;
import static com.example.wi_ficollector.utility.Constants.TYPE;

@Getter
@Setter
public class HttpRequest {

    private OutputStream mOutputStream;
    private HttpURLConnection mHttpUrlConnection;
    private URL mURL;
    private int responseCode;

    public int send(JSONArray jsonArray) {
        String strings = jsonArray.toString();
        byte[] bytes = strings.getBytes();

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

        mHttpUrlConnection.setDoOutput(true);
        mHttpUrlConnection.setFixedLengthStreamingMode(bytes.length);
        mHttpUrlConnection.setRequestProperty(CONTENT_TYPE, TYPE);
        mHttpUrlConnection.setRequestProperty(ACCEPT, TYPE);
        mHttpUrlConnection.setConnectTimeout(TEN_SECONDS);
        mHttpUrlConnection.setReadTimeout(TEN_SECONDS);

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
            return NEGATIVE_ONE;
        } catch (IOException e) {
            return NEGATIVE_ONE;
        }

        try {
            responseCode = mHttpUrlConnection.getResponseCode();
        } catch (IOException e) {
            mHttpUrlConnection.disconnect();
            return NEGATIVE_ONE;
        }

        if (responseCode == HttpURLConnection.HTTP_OK) {
            try {
                mOutputStream.close();
                mHttpUrlConnection.disconnect();
            } catch (IOException e) {
                Log.d(IO_EXCEPTION_THROWN_TAG, IO_EXCEPTION_THROWN_MESSAGE);
            }
        } else {
            return HttpURLConnection.HTTP_NOT_FOUND;
        }
        return responseCode;
    }
}