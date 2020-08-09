package com.example.wi_ficollector.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.wi_ficollector.R;
import com.example.wi_ficollector.preference.MainPreference;
import com.example.wi_ficollector.repository.WifiLocationInput;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MainPreference mainPreference = new MainPreference(this);
        Button startScanningBtn = findViewById(R.id.start_scanning_button);
        Button sendDataBtn = findViewById(R.id.send_data_button);

        if (mainPreference.isActivityFirstTimeLaunched()) {
            showIntroDialog();
        }

        startScanningBtn.setOnClickListener(this);
        sendDataBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int buttonId = v.getId();

        if (buttonId == R.id.start_scanning_button) {
            Intent intent = new Intent(this, ScanActivity.class);
            startActivity(intent);
        } else {
            WifiLocationInput wifiLocationInput = new WifiLocationInput(this);
            wifiLocationInput.read();
        }
    }

    void showIntroDialog() {
        MainPreference mainPreference = new MainPreference(this);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        alertDialog.setMessage(R.string.intro);
        alertDialog.setPositiveButton(R.string.OK, (dialog, id) -> mainPreference.addIntroKey());
        alertDialog.create().show();
    }
}