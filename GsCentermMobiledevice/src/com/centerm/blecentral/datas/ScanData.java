package com.centerm.blecentral.datas;

/**
 * Created by jqj on 2016/5/24.
 *
 */
public final class ScanData {
    private String deviceName;
    private String address;

    public ScanData(String deviceName, String address) {
        this.deviceName = deviceName;
        this.address = address;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getAddress() {
        return address;
    }
}
