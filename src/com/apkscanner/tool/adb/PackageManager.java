package com.apkscanner.tool.adb;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.IDevice.DeviceState;
import com.android.ddmlib.InstallException;
import com.android.ddmlib.TimeoutException;
import com.apkscanner.core.scanner.ApkScanner;
import com.apkscanner.tool.adb.AdbDeviceHelper.CommandRejectedException;
import com.apkscanner.util.Log;


public class PackageManager {
	private static final ArrayList<IPackageStateListener> sPackageListeners =
			new ArrayList<IPackageStateListener>();

	private static final Object sLock = sPackageListeners;

	public static void addPackageStateListener(IPackageStateListener listener) {
		synchronized (sLock) {
			if (!sPackageListeners.contains(listener)) {
				sPackageListeners.add(listener);
			}
		}
	}

	public static void removePackageStateListener(IPackageStateListener listener) {
		synchronized (sLock) {
			sPackageListeners.remove(listener);
		}
	}

	private static void packageInstalled(PackageInfo packageInfo) {
		IPackageStateListener[] listenersCopy = null;
		synchronized (sLock) {
			listenersCopy = sPackageListeners.toArray(
					new IPackageStateListener[sPackageListeners.size()]);
		}

		for (IPackageStateListener listener : listenersCopy) {
			try {
				listener.packageInstalled(packageInfo);
			} catch (Exception e) {
				Log.e(e.toString());
			}
		}
	}

	private static void packageUninstalled(PackageInfo packageInfo) {
		IPackageStateListener[] listenersCopy = null;
		synchronized (sLock) {
			listenersCopy = sPackageListeners.toArray(
					new IPackageStateListener[sPackageListeners.size()]);
		}

		for (IPackageStateListener listener : listenersCopy) {
			try {
				listener.packageUninstalled(packageInfo);
			} catch (Exception e) {
				Log.e(e.toString());
			}
		}
	}

	public static PackageInfo getPackageInfo(IDevice device, String packageName) {
		if(device == null || !device.isOnline()) {
			Log.e("device is null or no online");
			return null;
		}
		if(packageName == null || packageName.isEmpty()) {
			Log.e("package name is null");
			return null;
		}

		PackageInfo info = new PackageInfo(device, packageName);
		return info.getApkPath() != null ? info : null;
	}

	public static String installPackage(IDevice device, String localApkPath, boolean reinstall, String... extraArgs) {
		String errMessage = null;

		if(device == null) {
			errMessage = "Device is null";
		} else if(device.getState() != DeviceState.ONLINE) {
			errMessage = "Device is no online : " + device.getState();
		} else if(localApkPath == null || localApkPath.isEmpty() 
				|| !new File(localApkPath).isFile()) {
			errMessage = "No Such local apk file : " + localApkPath;
		}

		String packageName = ApkScanner.getPackageName(localApkPath);
		if(packageName == null || packageName.isEmpty()) {
			errMessage = "Invalid APK file. Cannot read package name";
		}

		if(errMessage != null) {
			return errMessage; 
		}

		try {
			errMessage = device.installPackage(localApkPath, reinstall, extraArgs);
			if(errMessage == null || errMessage.isEmpty() || errMessage.indexOf("Success") > -1) {
				packageInstalled(new PackageInfo(device, packageName));
			}
		} catch (InstallException e) {
			errMessage = e.getMessage();
			e.printStackTrace();
		}

		return errMessage;
	}

	public static String uninstallPackage(IDevice device, String packageName) {
		PackageInfo packageInfo = getPackageInfo(device, packageName);
		if(packageInfo == null) {
			return "Unknown package";
		}
		return uninstallPackage(packageInfo);
	}

	public static String uninstallPackage(PackageInfo packageInfo) {
		String errMessage = null;

		if(packageInfo == null || packageInfo.packageName == null) {
			errMessage = "PackageInfo is null";
		} else if(packageInfo.device == null) {
			errMessage = "Device is null";
		} else if(packageInfo.device.getState() != DeviceState.ONLINE) {
			errMessage = "Device is no online : " + packageInfo.device.getState();
		} else if(packageInfo.isSystemApp()) {
			errMessage = "System applications can not be uninstalled.";
		} else {
			String apkPath = packageInfo.getApkPath();
			if(apkPath == null || apkPath.isEmpty()) {
				errMessage = "No such apk file";
			} else {
				packageInfo.clear();
				String newApkPath = packageInfo.getApkPath();
				if(newApkPath == null || newApkPath.isEmpty()) {
					errMessage = "Already uninstalled";
				} else if(!apkPath.equals(newApkPath)) {
					Log.w("Changed apk path '" + apkPath + "' to '" + newApkPath + "'");
				}
			}
		}

		if(errMessage != null) {
			return errMessage; 
		}

		try {
			errMessage = packageInfo.device.uninstallPackage(packageInfo.packageName);
			if(errMessage == null || errMessage.isEmpty()) {
				packageInfo.clear();
				packageUninstalled(packageInfo);
			}
		} catch (InstallException e) {
			errMessage = e.getMessage();
			e.printStackTrace();
		}

		return errMessage;
	}

	public static String removePackage(IDevice device, String packageName) {
		PackageInfo packageInfo = getPackageInfo(device, packageName);
		if(packageInfo == null) {
			return "Unknown package";
		}
		return removePackage(packageInfo);
	}

	public static String removePackage(PackageInfo packageInfo) {
		String errMessage = null;

		if(packageInfo == null || packageInfo.packageName == null) {
			errMessage = "PackageInfo is null";
		} else if(packageInfo.device != null) {
			errMessage = "Device is null";
		} else if(packageInfo.device.getState() != DeviceState.ONLINE) {
			errMessage = "Device is no online : " + packageInfo.device.getState();
		} else {
			if(packageInfo.isSystemApp()) {
				if(!AdbDeviceHelper.hasSu(packageInfo.device)) {
					errMessage = "This device was not rooting!\nSo, can not remove for the system package!";
				} else if(!AdbDeviceHelper.isRoot(packageInfo.device)) {
					errMessage = "no root, retry after change root mode";
				}
			}

			String apkPath = packageInfo.getApkPath();
			if(apkPath == null || apkPath.isEmpty()) {
				errMessage = "No such apk file";
			} else {
				packageInfo.clear();
				String newApkPath = packageInfo.getApkPath();
				if(newApkPath == null || newApkPath.isEmpty()) {
					errMessage = "Already uninstalled";
				} else if(!apkPath.equals(newApkPath)) {
					Log.w("Changed apk path '" + apkPath + "' to '" + newApkPath + "'");
				}
			}
		}

		if(errMessage == null && packageInfo.isSystemApp()) {
			try {
				AdbDeviceHelper.remount(AndroidDebugBridge.getSocketAddress(), packageInfo.device);
			} catch (TimeoutException | CommandRejectedException | IOException e1) {
				errMessage = e1.getMessage();
				e1.printStackTrace();
			}
		}

		if(errMessage != null) {
			return errMessage; 
		}

		try {
			packageInfo.device.removeRemotePackage(packageInfo.getApkPath());
			packageInfo.clear();
			packageUninstalled(packageInfo);
		} catch (InstallException e) {
			errMessage = e.getMessage();
			e.printStackTrace();
		}

		return errMessage;
	}
}
