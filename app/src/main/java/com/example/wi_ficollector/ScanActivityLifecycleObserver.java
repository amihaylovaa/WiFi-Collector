package com.example.wi_ficollector;

import android.app.Activity;
import android.content.Context;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.example.wi_ficollector.repository.WifiLocationRepository;

import java.io.IOException;

public class ScanActivityLifecycleObserver implements LifecycleObserver {

    Context context;
    WifiLocationRepository wifiLocationRepository;

    public ScanActivityLifecycleObserver(Activity activity, WifiLocationRepository wifiLocationRepository) {
        context = activity;
        this.wifiLocationRepository = wifiLocationRepository;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onStopActivity() {
        try {
            wifiLocationRepository.endDocument();
        } catch (IOException e) {

        }
    }
}
