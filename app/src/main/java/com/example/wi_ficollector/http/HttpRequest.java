package com.example.wi_ficollector.http;

import android.provider.ContactsContract;
import android.util.Log;

import com.example.wi_ficollector.wrapper.WifiLocation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class HttpRequest {

    private static final String URL = "http://www.xxx.xxx.x.xxx:xxxx/wifi/locations";
    private static final String REQUEST_METHOD = "POST";
    private Executor mExecutor = Executors.newSingleThreadExecutor();

    public void send(List<WifiLocation> wifiLocations) throws IOException {
        mExecutor.execute(() -> {
            try {
                URL url = new URL(URL);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestMethod(REQUEST_METHOD);
                urlConnection.setChunkedStreamingMode(0);
                urlConnection.setRequestProperty("Content-type", "application/json");

                // test data
                JSONObject jsonObject = new JSONObject();

                jsonObject.put("id", "45");
                jsonObject.put("latitude", "24.123");
                jsonObject.put("longitude", "42.456");
                jsonObject.put("wifiScanResults", new JSONArray());
                jsonObject.put("localDateTime", LocalDateTime.now().toString());

                DataOutputStream os = new DataOutputStream(urlConnection.getOutputStream());

                os.writeBytes(jsonObject.toString());
                os.flush();
                os.close();

                urlConnection.disconnect();
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }

        });
    }
}
