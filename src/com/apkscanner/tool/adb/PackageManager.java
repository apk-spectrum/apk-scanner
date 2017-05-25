package com.apkscanner.tool.adb;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.IDevice.DeviceState;
import com.android.ddmlib.InstallException;
import com.android.ddmlib.NullOutputReceiver;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import com.apkscanner.core.scanner.ApkScanner;
import com.apkscanner.tool.adb.AdbDeviceHelper.CommandRejectedException;
import com.apkscanner.util.Log;


public class PackageManager {
	private static final ArrayList<IPackageStateListener> sPackageListeners =
			new ArrayList<IPackageStateListener>();

	private static final Object sLock = sPackageListeners;

	private static HashMap<IDevice, HashMap<String, PackageInfo>> packagesMap = new HashMap<IDevice, HashMap<String, PackageInfo>>();
	private static HashMap<String, PackageInfo[]> packageListCache = new HashMap<String, PackageInfo[]>();

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
			HashMap<String, PackageInfo> devicePackagList = packagesMap.get(packageInfo.device);
			if(devicePackagList == null) {
				devicePackagList = new HashMap<String, PackageInfo>();
				packagesMap.put(packageInfo.device, devicePackagList);
			}
			devicePackagList.put(packageInfo.packageName, packageInfo);
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
			HashMap<String, PackageInfo> devicePackagList = packagesMap.get(packageInfo.device);
			if(devicePackagList != null && devicePackagList.containsKey(packageInfo.packageName)) {
				devicePackagList.remove(packageInfo.packageName);
			}
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
		return getPackageInfo(device, packageName, true);
	}

	public static PackageInfo getPackageInfo(IDevice device, String packageName, boolean useCache) {
		if(device == null || !device.isOnline()) {
			Log.e("device is null or no online");
			return null;
		}
		if(packageName == null || packageName.isEmpty()) {
			Log.e("package name is null");
			return null;
		}

		PackageInfo info = null;
		synchronized (sLock) {
			HashMap<String, PackageInfo> devicePackagList = packagesMap.get(device);
			if(devicePackagList == null) {
				devicePackagList = new HashMap<String, PackageInfo>();
				packagesMap.put(device, devicePackagList);
			}
			if(useCache) {
				info = devicePackagList.get(packageName);
			}
			if(info == null) {
				info = new PackageInfo(device, packageName);
				if(info.getApkPath() == null) {
					info = null;
				} else {
					devicePackagList.put(packageName, info);
				}
			}
		}
		return info;
	}

	public static PackageInfo[] getPackageList(IDevice device) {
		return getPackageList(device, true);
	}

	public static PackageInfo[] getPackageList(IDevice device, boolean useCache) {
		if(useCache) {
			synchronized (sLock) {
				PackageInfo[] cache = packageListCache.get(device.getSerialNumber());
				if(cache != null) {
					return cache;
				}
			}
		}

		ArrayList<PackageInfo> list = new ArrayList<PackageInfo>();

		SimpleOutputReceiver outputReceiver = new SimpleOutputReceiver();
		outputReceiver.setTrimLine(false);

		try {
			device.executeShellCommand("pm list packages -f -i -u", outputReceiver);
		} catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException | IOException e1) {
			e1.printStackTrace();
		}
		String[] pmList = outputReceiver.getOutput();

		outputReceiver.clear();
		try {
			device.executeShellCommand("dumpsys package", outputReceiver);
		} catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException | IOException e1) {
			e1.printStackTrace();
		}

		boolean start = false;
		PackageInfo pack = null;
		String verName = null;
		String verCode = null;
		for(String line: outputReceiver.getOutput()) {
			if(!start) {
				if(line.startsWith("Packages:")) {
					start = true;
				}
				continue;
			}
			if(line.matches("^\\s*Package\\s*\\[.*")) {
				if(pack != null) {
					if(pack.apkPath == null) {
						pack.apkPath = pack.codePath;
					}
					pack.label = pack.apkPath.replaceAll(".*/", "") + " - [" + pack.packageName + "] - " + verName + "/" + verCode;
					list.add(pack);
				}
				String packagName = line.replaceAll("^\\s*Package\\s*\\[(.*)\\].*:\\s*$", "$1");
				pack = new PackageInfo(device, packagName);
				verName = null;
				verCode = null;
				for(String output: pmList) {
					if(output.matches("^package:.*=" + packagName + "\\s*installer=.*")) {
						pack.apkPath = output.replaceAll("^package:(.*)=" + packagName + "\\s*installer=(.*)", "$1");
						pack.installer = output.replaceAll("^package:(.*)=" + packagName + "\\s*installer=(.*)", "$2");
					}
				}
			} else if(pack != null && pack.codePath == null && line.matches("^\\s*codePath=.*$")) {
				pack.codePath = line.replaceAll("^\\s*codePath=\\s*([^\\s]*).*$", "$1");
				if(pack.apkPath != null && !pack.apkPath.startsWith(pack.codePath)) {
					pack.apkPath = pack.codePath;
				}
			} else if(verName == null && line.matches("^\\s*versionName=.*$")) {
				verName = line.replaceAll("^\\s*versionName=\\s*([^\\s]*).*$", "$1");
			} else if(verCode == null && line.matches("^\\s*versionCode=.*$")) {
				verCode = line.replaceAll("^\\s*versionCode=\\s*([^\\s]*).*$", "$1");
			}
		}
		outputReceiver.clear();

		if(pack != null) {
			if(pack.apkPath == null) {
				pack.apkPath = pack.codePath;
			}
			pack.label = pack.apkPath.replaceAll(".*/", "") + " - [" + pack.packageName + "] - " + verName + "/" + verCode;
			list.add(pack);
		}

		try {
			device.executeShellCommand("ls /system/framework/*.apk", outputReceiver);
		} catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException | IOException e) {
			e.printStackTrace();
		}
		for(String line: outputReceiver.getOutput()) {
			if(line.equals("/system/framework/framework-res.apk")
					|| !line.endsWith(".apk")) continue;
			String packagName = line.replaceAll(".*/(.*)\\.apk", "$1");
			pack = new PackageInfo(device, packagName);
			pack.apkPath = line;
			pack.codePath = "/system/framework";
			pack.label = pack.apkPath.replaceAll(".*/", "");
			list.add(pack);
		}

		PackageInfo[] packageList = list.toArray(new PackageInfo[list.size()]);
		synchronized (sLock) {
			packageListCache.put(device.getSerialNumber(), packageList);
		}
		return packageList;
	}

	public static void removeListCash(IDevice device) {
		synchronized (sLock) {
			packageListCache.remove(device.getSerialNumber());
		}
	}

	public static void removeCash(IDevice device) {
		synchronized (sLock) {
			if(packagesMap.containsKey(device)) {
				packagesMap.remove(device);
			}
		}
	}

	public static IDevice[] getInstalledDevices(String packageName) {
		ArrayList<IDevice> list = new ArrayList<IDevice>();
		synchronized (sLock) {
			for(Entry<IDevice, HashMap<String, PackageInfo>> entry: packagesMap.entrySet()) {
				if(entry.getKey().isOnline() && entry.getValue().containsKey(packageName)) {
					list.add(entry.getKey());
				}
			}
		}

		return list.toArray(new IDevice[list.size()]);
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
		} else if(packageInfo.device == null) {
			errMessage = "Device is null";
		} else if(packageInfo.device.getState() != DeviceState.ONLINE) {
			errMessage = "Device is no online : " + packageInfo.device.getState();
		} else {
			if(packageInfo.isSystemApp()) {
				if(!AdbDeviceHelper.hasSu(packageInfo.device)) {
					errMessage = "This device was not rooting!\nCan not remove for the system package!";
				} else if(!AdbDeviceHelper.isRoot(packageInfo.device)) {
					errMessage = "No root, retry after change to root mode";
				}
			}

			String apkPath = packageInfo.getApkPath();
			if(apkPath == null || apkPath.isEmpty()) {
				errMessage = "No such apk file";
			} else {
				String newApkPath = packageInfo.getRealApkPath();
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
				packageInfo.device.executeShellCommand("su root setenforce 0", new NullOutputReceiver());
			} catch (TimeoutException | CommandRejectedException | IOException | AdbCommandRejectedException | ShellCommandUnresponsiveException e1) {
				errMessage = e1.getMessage();
				e1.printStackTrace();
			}
		}

		if(errMessage != null) {
			return errMessage; 
		}

		try {
			SimpleOutputReceiver outputReceiver = new SimpleOutputReceiver();
			packageInfo.device.executeShellCommand("rm " + packageInfo.getApkPath(), outputReceiver);
			//packageInfo.device.removeRemotePackage(packageInfo.getApkPath());
			for(String line: outputReceiver.getOutput()) {
				if(!line.isEmpty()) {
					errMessage = line;
					break;
				}
			}

			if(errMessage == null) {
				packageInfo.clear();
				packageUninstalled(packageInfo);
			}
		} catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException | IOException e) {
			errMessage = e.getMessage();
			e.printStackTrace();
		}

		return errMessage;
	}

	public static String[] getRecentlyActivityPackages(IDevice device) {
		SimpleOutputReceiver outputReceiver = new SimpleOutputReceiver();
		outputReceiver.setTrimLine(false);
		try {
			device.executeShellCommand("am stack list", outputReceiver);
		} catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException | IOException e) {
			e.printStackTrace();
		}
		String[] result = outputReceiver.getOutput();
		ArrayList<String> pkgList = new ArrayList<String>();
		boolean isLegacy = false;
		for(String line: result) {
			if(line.startsWith("  taskId=")) {
				String pkg = line.replaceAll("  taskId=[0-9]*:\\s([^/]*)/.*", "$1").trim(); 
				if(pkg != null && !pkg.isEmpty() && !pkg.equals(line)) {
					if(pkg.indexOf(" ") == -1 && !pkgList.contains(pkg)) {
						if(line.indexOf("visible=true") >= 0)
							pkgList.add(0, pkg);
						else
							pkgList.add(pkg);
					} else {
						Log.w("Unknown pkg - " + pkg);
					}
				}
			}
			if(line.startsWith("Error: unknown command 'list'")) {
				isLegacy = true;
				break;
			}
		}

		if(isLegacy) {
			return getRecentlyActivityPackagesLegacy(device);
		}

		return pkgList.toArray(new String[0]);
	}

	private static String[] getRecentlyActivityPackagesLegacy(IDevice device) {
		SimpleOutputReceiver outputReceiver = new SimpleOutputReceiver();
		outputReceiver.setTrimLine(false);
		try {
			device.executeShellCommand("am stack boxes", outputReceiver);
		} catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException | IOException e) {
			e.printStackTrace();
		}
		String[] result = outputReceiver.getOutput();
		ArrayList<String> pkgList = new ArrayList<String>();
		for(String line: result) {
			if(line.startsWith("    taskId=")) {
				String pkg = line.replaceAll("    taskId=[0-9]*:\\s([^/]*)/.*", "$1").trim(); 
				if(pkg != null && !pkg.isEmpty() && !pkg.equals(line)) {
					if(pkg.indexOf(" ") == -1 && !pkgList.contains(pkg)) {
						pkgList.add(0, pkg);
					} else {
						Log.w("Unknown pkg - " + pkg);
					}
				}
			}
		}
		return pkgList.toArray(new String[0]);
	}

	public static String[] getCurrentlyRunningPackages(IDevice device) {
		SimpleOutputReceiver outputReceiver = new SimpleOutputReceiver();
		outputReceiver.setTrimLine(false);
		try {
			device.executeShellCommand("ps", outputReceiver);
		} catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException | IOException e) {
			e.printStackTrace();
		}
		String[] result = outputReceiver.getOutput();
		ArrayList<String> pkgList = new ArrayList<String>();
		for(String line: result) {
			if(!line.startsWith("root")) {
				String pkg = line.replaceAll(".* ([^\\s:]*)(:.*)?$", "$1");
				if(pkg != null && !pkg.isEmpty() && !pkg.equals(line)) {
					if(!pkg.startsWith("/") && !pkgList.contains(pkg)) {
						pkgList.add(pkg);
					}
				}
			}
		}
		if(pkgList.size() > 0 && pkgList.get(0).equals("NAME")) {
			pkgList.remove(0);
		}
		return pkgList.toArray(new String[0]);
	}
}
