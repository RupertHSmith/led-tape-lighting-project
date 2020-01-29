package com.rgbtape.app;

public interface DeviceStateListener {
    void onDeviceStateReceived(boolean standby, int speed, int intensity, String type, String customId, int r, int g, int b);
}
