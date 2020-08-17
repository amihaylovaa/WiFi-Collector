package com.example.wi_ficollector.http;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.example.wi_ficollector.R;

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
import static com.example.wi_ficollector.utility.Constants.PATH;
import static com.example.wi_ficollector.utility.Constants.PORT;
import static com.example.wi_ficollector.utility.Constants.PROTOCOL;
import static com.example.wi_ficollector.utility.Constants.REQUEST_METHOD;
import static com.example.wi_ficollector.utility.Constants.TYPE;

@Getter
@Setter
// TODO  add broadcast receiver to listen for connection changes
public class HttpRequest {

    private int responseCode;
    private Handler mHandler;

    public HttpRequest() {
        responseCode = 0;
        mHandler = new Handler(Looper.getMainLooper());
    }

    public void send(JSONArray jsonArray, Context context) {
        try {
            URL url = new URL(PROTOCOL, HOST, PORT, PATH);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setRequestMethod(REQUEST_METHOD);
            urlConnection.setChunkedStreamingMode(0);
            urlConnection.setRequestProperty(CONTENT_TYPE, TYPE);

            DataOutputStream os = new DataOutputStream(urlConnection.getOutputStream());

            os.writeBytes(jsonArray.toString());
            os.flush();
            os.close();
            setResponseCode(urlConnection.getResponseCode());

            if (urlConnection.getResponseCode() == 200) {
                mHandler.post(() ->
                        Toast.makeText(context, R.string.send_data_success, Toast.LENGTH_LONG).show());
            }
            urlConnection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
            mHandler.post(() ->
                    Toast.makeText(context, R.string.internet_connection_disabled, Toast.LENGTH_LONG).show());
        }
    }
}