package com.example.wi_ficollector.http;

import org.json.JSONArray;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class HttpRequest {


    private final String PROTOCOL = "http";
    private final String HOST = "xxx.xxx.x.xxx";
    private final int PORT = 0000;
    private final String PATH = "/wifi/locations";
    private static final String REQUEST_METHOD = "POST";
    private Executor mExecutor = Executors.newSingleThreadExecutor();

    public void send(JSONArray jsonArray) {
        mExecutor.execute(() -> {
            try {
                URL url = new URL(PROTOCOL, HOST, PORT, PATH);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestMethod(REQUEST_METHOD);
                urlConnection.setChunkedStreamingMode(0);
                urlConnection.setRequestProperty("Content-type", "application/json");

                DataOutputStream os = new DataOutputStream(urlConnection.getOutputStream());

                os.writeBytes(jsonArray.toString());
                int message = urlConnection.getResponseCode();

                os.flush();
                os.close();
                urlConnection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }

        });
    }
}