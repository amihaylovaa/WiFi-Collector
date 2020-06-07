package com.example.wi_ficollector;

import android.app.Activity;
import android.content.Context;

import androidx.lifecycle.LifecycleObserver;

public class ScanActivityLifecycleObserver implements LifecycleObserver {

    Context context;

    public ScanActivityLifecycleObserver() {

    }

    public ScanActivityLifecycleObserver(Activity activity) {
        context = activity;
    }

}
