package com.example.wi_ficollector;

import android.app.Activity;
import android.content.Context;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

public class ScanActivityLifecycleObserver implements LifecycleObserver {

    Context context;

    public ScanActivityLifecycleObserver() {

    }

    public ScanActivityLifecycleObserver(Activity activity) {
        context = activity;
    }

}
