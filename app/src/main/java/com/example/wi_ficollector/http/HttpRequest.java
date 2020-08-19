package com.example.wi_ficollector.http;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;

import java.io.DataOutputStream;
import java.io.IOException;

import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;

import lombok.Getter;
import lombok.Setter;

import static com.example.wi_ficollector.utility.Constants.CONTENT_TYPE;
import static com.example.wi_ficollector.utility.Constants.HOST;
import static com.example.wi_ficollector.utility.Constants.IO_EXCEPTION_THROWN_MESSAGE;
import static com.example.wi_ficollector.utility.Constants.IO_EXCEPTION_THROWN_TAG;
import static com.example.wi_ficollector.utility.Constants.PATH;
import static com.example.wi_ficollector.utility.Constants.PORT;
import static com.example.wi_ficollector.utility.Constants.PROTOCOL;
import static com.example.wi_ficollector.utility.Constants.REQUEST_METHOD;
import static com.example.wi_ficollector.utility.Constants.SERVER_ERROR_CODE;
import static com.example.wi_ficollector.utility.Constants.TYPE;

@Getter
@Setter
public class HttpRequest {

    private int responseCode;
    private Handler mHandler;

    public HttpRequest() {
        responseCode = 0;
        mHandler = new Handler(Looper.getMainLooper());
    }

    public int send(JSONArray jsonArray) {
        try {
            URL url = new URL(PROTOCOL, HOST, PORT, PATH);
            DataOutputStream os = null;
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setRequestMethod(REQUEST_METHOD);
            urlConnection.setChunkedStreamingMode(0);
            urlConnection.setRequestProperty(CONTENT_TYPE, TYPE);

            try {
                os = new DataOutputStream(urlConnection.getOutputStream());

                os.writeBytes(jsonArray.toString());
                os.flush();
                os.close();
                setResponseCode(urlConnection.getResponseCode());

                urlConnection.disconnect();
            } catch (ConnectException e) {
                setResponseCode(SERVER_ERROR_CODE);
            }
        } catch (IOException e) {
            Log.d(IO_EXCEPTION_THROWN_TAG, IO_EXCEPTION_THROWN_MESSAGE);
        }
        return responseCode;
    }
}