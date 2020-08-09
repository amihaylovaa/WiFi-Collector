package com.example.wi_ficollector.http;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpRequest {

    private static final String URL = "localhost:8281/wifi/locations";
    private static final String REQUEST_METHOD = "POST";


    private void send() throws IOException {
        URL url = new URL(URL);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        urlConnection.setRequestMethod(REQUEST_METHOD);
        urlConnection.setChunkedStreamingMode(0);
        urlConnection.setRequestProperty("Content-type", "application/json");
    }

}
