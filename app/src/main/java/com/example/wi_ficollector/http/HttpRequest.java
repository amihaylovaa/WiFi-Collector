package com.example.wi_ficollector.http;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import org.json.JSONArray;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

// todo - add response code check
public class HttpRequest {

    private static final String PROTOCOL;
    private static final String HOST;
    private static final int PORT;
    private static final String PATH;
    private static final String REQUEST_METHOD;
    private Executor mExecutor;

    public HttpRequest() {
        mExecutor = Executors.newSingleThreadExecutor();
    }

    static {
        PROTOCOL = "http";
        HOST = "xxx.xxx.x.xxx";
        PORT = 0;
        PATH = "/wifi/locations";
        REQUEST_METHOD = "POST";
    }

    public void send(JSONArray jsonArray, Context context) {
        mExecutor.execute(() -> {
            try {
                URL url = new URL(PROTOCOL, HOST, PORT, PATH);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestMethod(REQUEST_METHOD);
                urlConnection.setChunkedStreamingMode(0);
                urlConnection.setRequestProperty("Content-type", "application/json");

                DataOutputStream os = new DataOutputStream(urlConnection.getOutputStream());

                os.writeBytes(jsonArray.toString());

                os.flush();
                os.close();
                urlConnection.disconnect();

            } catch (IOException e) {
                e.printStackTrace();
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> Toast.makeText(context, "Enable Internet and try again ", Toast.LENGTH_LONG).show());
            }
        });
    }
}