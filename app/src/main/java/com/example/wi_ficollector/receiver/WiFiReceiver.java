package com.example.wi_ficollector.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import static com.example.wi_ficollector.utils.Constants.FILE_NAME;
import static com.example.wi_ficollector.utils.Constants.isAlreadyScanned;

public class WiFiReceiver extends BroadcastReceiver {

    private WifiManager mWifiManager;
    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        boolean hasSuccess = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);

        Toast.makeText(context, "Entered receiver", Toast.LENGTH_SHORT).show();

        if (!isAlreadyScanned) {
            if (hasSuccess) {
                Toast.makeText(context, "Scanning ready", Toast.LENGTH_SHORT).show();
                scanningSuccess(context);
            } else {
                Toast.makeText(context, "Error receiving scanning results", Toast.LENGTH_SHORT).show();
            }
        }
        isAlreadyScanned = true;
    }

    void scanningSuccess(Context context) {
        List<ScanResult> results = mWifiManager.getScanResults();

        int num = results.size();

        if (num == 0) {
            Toast.makeText(context, "Nothing found", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "Recording", Toast.LENGTH_LONG).show();
            for (ScanResult result : results) {
                showIt(result);
            }
        }
    }

    public void showIt(ScanResult result) {
        String fileContent = "\nSSID: :" + result.SSID + "\nFrequency : " + result.frequency + "\n";

        try {
            OutputStreamWriter osw = new OutputStreamWriter(context.openFileOutput(FILE_NAME, Context.MODE_APPEND));
            try {
                osw.write("\nWi-Fi");
                osw.write(fileContent);
                osw.write("\n");

            } catch (IOException e) {
                e.printStackTrace();
            }
            osw.flush();
            osw.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}