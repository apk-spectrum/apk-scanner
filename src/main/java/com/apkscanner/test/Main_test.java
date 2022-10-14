package com.apkscanner.test;

import java.io.IOException;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;
import com.android.ddmlib.IDevice;

public class Main_test {

    public static void main(String[] args) throws IOException {
        AndroidDebugBridge.init(false);

        AndroidDebugBridge debugBridge = AndroidDebugBridge.createBridge("/home/leejinhyeong/Desktop/android-sdk/platform-tools/adb", true);
        if (debugBridge == null) {
            System.err.println("Invalid ADB location.");
            System.exit(1);
        }

        AndroidDebugBridge.addDeviceChangeListener(new IDeviceChangeListener() {

            @Override
            public void deviceChanged(IDevice device, int arg1) {
                // not implement
            }

            @Override
            public void deviceConnected(IDevice device) {
                System.out.println(String.format("%s connected", device.getSerialNumber()));
            }

            @Override
            public void deviceDisconnected(IDevice device) {
                System.out.println(String.format("%s disconnected", device.getSerialNumber()));

            }

        });

        System.out.println("Press enter to exit.");
        System.in.read();
    }
}
