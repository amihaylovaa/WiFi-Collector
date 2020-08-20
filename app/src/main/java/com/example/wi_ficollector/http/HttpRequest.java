package com.example.wi_ficollector.http;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;

import java.io.IOException;

import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import lombok.Getter;
import lombok.Setter;

import static com.example.wi_ficollector.utility.Constants.ACCEPT;
import static com.example.wi_ficollector.utility.Constants.CONTENT_TYPE;
import static com.example.wi_ficollector.utility.Constants.HOST;
import static com.example.wi_ficollector.utility.Constants.IO_EXCEPTION_THROWN_MESSAGE;
import static com.example.wi_ficollector.utility.Constants.IO_EXCEPTION_THROWN_TAG;
import static com.example.wi_ficollector.utility.Constants.PATH;
import static com.example.wi_ficollector.utility.Constants.PORT;
import static com.example.wi_ficollector.utility.Constants.PROTOCOL;
import static com.example.wi_ficollector.utility.Constants.REQUEST_METHOD;
import static com.example.wi_ficollector.utility.Constants.TYPE;

@Getter
@Setter
public class HttpRequest {

    private int responseCode;
    private Handler mHandler;

    public HttpRequest() {
        mHandler = new Handler(Looper.getMainLooper());
    }

    public int send(JSONArray jsonArray) {
        URL url;
        OutputStream os = null;
        HttpURLConnection urlConnection = null;
        String strings = jsonArray.toString();
        byte[] bytes = strings.getBytes();

        try {
            url = new URL(PROTOCOL, HOST, PORT, PATH);
        } catch (MalformedURLException e) {
            // may be bad request
            return HttpURLConnection.HTTP_NOT_FOUND;
        }

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            //
            return HttpURLConnection.HTTP_NOT_FOUND;
        }

        urlConnection.setDoOutput(true);
        urlConnection.setFixedLengthStreamingMode(bytes.length);
        urlConnection.setRequestProperty(CONTENT_TYPE, TYPE);
        urlConnection.setRequestProperty(ACCEPT, TYPE);

        try {
            os = urlConnection.getOutputStream();
        } catch (IOException e) {
            urlConnection.disconnect();
            return HttpURLConnection.HTTP_UNAVAILABLE;
        }

        try {
            os.write(bytes);
        } catch (IOException e) {
            urlConnection.disconnect();
            // lost internet connection
            return HttpURLConnection.HTTP_UNAVAILABLE;
        }

        try {
            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                os.close();
                urlConnection.disconnect();
                setResponseCode(HttpURLConnection.HTTP_OK);
            }
        } catch (IOException e) {
            Log.d(IO_EXCEPTION_THROWN_TAG, IO_EXCEPTION_THROWN_MESSAGE);
        }
        return responseCode;
    }
}