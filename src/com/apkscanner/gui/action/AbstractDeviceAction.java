package com.apkscanner.gui.action;

import java.util.ArrayList;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.apkscanner.resource.RProp;
import com.apkscanner.tool.adb.AdbServerMonitor;
import com.apkscanner.tool.adb.PackageInfo;
import com.apkscanner.tool.adb.PackageManager;
import com.apkscanner.util.Log;

@SuppressWarnings("serial")
public abstract class AbstractDeviceAction extends AbstractApkScannerAction
{
	public AbstractDeviceAction() { }

	public AbstractDeviceAction(ActionEventHandler h) { super(h); }

	protected IDevice[] getInstalledDevice(String packageName) {
		IDevice[] devices = null;
		if(RProp.B.ADB_DEVICE_MONITORING.get()) {
			devices = PackageManager.getInstalledDevices(packageName);
		} else {
			AndroidDebugBridge adb = AdbServerMonitor.getAndroidDebugBridge();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			devices = adb.getDevices();
			Log.i("devices size : " + devices.length);

			ArrayList<IDevice> deviceList = new ArrayList<IDevice>();
			for(IDevice dev: devices) {
				PackageInfo info = getPackageInfo(dev, packageName);
				if(info != null) {
					deviceList.add(dev);
				}
			}
			devices = deviceList.toArray(new IDevice[deviceList.size()]);
		}
		return devices;
	}

	protected PackageInfo getPackageInfo(IDevice device, String packageName) {
		return PackageManager.getPackageInfo(device, packageName);
	}
}
