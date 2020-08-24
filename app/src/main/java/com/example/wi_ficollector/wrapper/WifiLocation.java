package com.example.wi_ficollector.wrapper;


import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WifiLocation {

    private double latitude;
    private double longitude;
    private LocalDateTime localDateTime;
    private List<WifiScanResult> wifiScanResults;

    public void clearResults() {
        latitude = 0.00;
        longitude = 0.00;
        if (wifiScanResults != null) {
            wifiScanResults.clear();
        }
        localDateTime = null;
    }
}