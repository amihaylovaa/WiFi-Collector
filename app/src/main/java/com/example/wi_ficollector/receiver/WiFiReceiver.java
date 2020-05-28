package com.example.wi_ficollector.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.widget.Toast;

import com.example.wi_ficollector.repository.WiFiLocationRepository;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import static com.example.wi_ficollector.utils.Constants.FILE_NAME;
import static com.example.wi_ficollector.utils.Constants.isAlreadyScanned;
import static com.example.wi_ficollector.utils.Constants.out;

public class WiFiReceiver extends BroadcastReceiver {

    private WifiManager mWifiManager;
    private Context context;
    private WiFiLocationRepository wiFiLocationRepository;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wiFiLocationRepository = new WiFiLocationRepository();

        boolean hasSuccess = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);

        Toast.makeText(context, "Entered receiver", Toast.LENGTH_SHORT).show();

        if (!isAlreadyScanned) {
            if (hasSuccess) {
                Toast.makeText(context, "Scanning ready", Toast.LENGTH_SHORT).show();
                try {
                    scanningSuccess(context);
                } catch (FileNotFoundException | TransformerException | ParserConfigurationException e) {

                }
            } else {
                Toast.makeText(context, "Error receiving scanning results", Toast.LENGTH_SHORT).show();
            }
        }
        isAlreadyScanned = true;
    }

    void scanningSuccess(Context context) throws FileNotFoundException, TransformerException, ParserConfigurationException {
        List<ScanResult> results = mWifiManager.getScanResults();

        int num = results.size();

        if (num == 0) {
            Toast.makeText(context, "Nothing found", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "Recording", Toast.LENGTH_LONG).show();
            out = context.openFileOutput(FILE_NAME, Context.MODE_APPEND);
            wiFiLocationRepository.saveWifi(results, out);
        }
    }
}