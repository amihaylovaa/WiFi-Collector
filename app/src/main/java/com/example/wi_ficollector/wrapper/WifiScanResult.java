package com.example.wi_ficollector.wrapper;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WifiScanResult {

    private String bssid;
    private String ssid;
    private String rssi;
    private String capabilities;
    private int frequency;
}
