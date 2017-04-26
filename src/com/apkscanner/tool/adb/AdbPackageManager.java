package com.apkscanner.tool.adb;

import java.io.IOException;
import java.util.ArrayList;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import com.apkscanner.tool.adb.AdbDeviceHelper.SimpleOutputReceiver;
import com.apkscanner.util.Log;
import com.apkscanner.util.XmlPath;

public class AdbPackageManager {

	public static class PackageListObject {
		public String label;
		public String pacakge;
		public String codePath;
		public String apkPath;
		public String installer;

		@Override
		public String toString() {
			return this.label;
		}
	}

	public static class PackageInfo
	{
		public final String pkgName;
		public final String apkPath;
		public final String codePath;
		public final String versionName;
		public final int versionCode;
		public final String installer;

		public final String[] dumpsys;

		public final String signature;

		public final IDevice device;

		public PackageInfo(IDevice device, String pkgName, String apkPath, String codePath, String installer, String[] dumpsys, String signature)
		{
			this.device = device;
			this.pkgName = pkgName;
			this.apkPath = apkPath;
			this.dumpsys = dumpsys;
			this.signature = signature;
			this.installer = installer;

			this.codePath = codePath;

			versionName = getValue("versionName");
			String vercode = getValue("versionCode");
			if(vercode != null && vercode.matches("\\d+")) {
				this.versionCode = Integer.valueOf(vercode);
			} else {
				this.versionCode = 0;
			}
		}

		public PackageInfo(String pkgName, String apkPath, String codePath, String versionName, int versionCode, String installer, String[] dumpsys, String signature)
		{
			this.pkgName = pkgName;
			this.apkPath = apkPath;
			this.codePath = codePath;
			this.versionName = versionName;
			this.versionCode = versionCode;
			this.installer = installer;
			this.dumpsys = dumpsys;
			this.signature = signature;
			this.device = null;
		}

		@Override
		public String toString()
		{
			String s = "-Installed APK info\n";
			s += "Pakage : " + pkgName +"\n";
			s += "Version : " + versionName + " / " + versionCode +"\n";
			s += "APK Path : " + apkPath +"\n";
			s += "Installer : " + installer +"\n";
			return s;
		}

		public String getValue(String key)
		{
			String value = null;
			for(String line: dumpsys) {
				if(line.indexOf(" " + key + "=") > -1) {
					value = line.replaceAll(".*\\s+" + key + "=(\\d+-\\d+-\\d+\\s+\\d+:\\d+:\\d+|[^\\[][^\\s\\{]*(\\{[^\\}]*\\})?|\\[[^\\]]*\\]).*", "$1");
					if(!line.equals(value)) {
						break;
					}
					value = null;
				}
			}
			return value;
		}

		public boolean isSystemApp() {
			return (apkPath != null && apkPath.matches("^/system/.*")) 
					|| (codePath != null && codePath.matches("^/system/.*")); 
		}
	}

	public static String[] getRecentlyActivityPackages(String device) {
		String[] result = AdbWrapper.shell(device, new String[] {"am", "stack", "list"}, null);
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

	private static String[] getRecentlyActivityPackagesLegacy(String device) {
		String[] result = AdbWrapper.shell(device, new String[] {"am", "stack", "boxes"}, null);
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

	public static String[] getCurrentlyRunningPackages(String device) {
		String[] result = AdbWrapper.shell(device, new String[] {"ps"}, null);
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

	public static ArrayList<PackageListObject> getPackageList(String device)
	{
		ArrayList<PackageListObject> list = new ArrayList<PackageListObject>();

		String[] result = AdbWrapper.shell(device, new String[] {"dumpsys", "package"}, null);
		String[] pmList = AdbWrapper.shell(device, new String[] {"pm", "list", "packages", "-f", "-i", "-u"}, null);

		boolean start = false;
		PackageListObject pack = null;
		String verName = null;
		String verCode = null;
		for(String line: result) {
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
					pack.label = pack.apkPath.replaceAll(".*/", "") + " - [" + pack.pacakge + "] - " + verName + "/" + verCode;
					list.add(pack);
				}
				pack = new PackageListObject();
				verName = null;
				verCode = null;
				pack.pacakge = line.replaceAll("^\\s*Package\\s*\\[(.*)\\].*:\\s*$", "$1");
				pack.codePath = null;
				pack.apkPath = null;
				for(String output: pmList) {
					if(output.matches("^package:.*=" + pack.pacakge + "\\s*installer=.*")) {
						pack.apkPath = output.replaceAll("^package:(.*)=" + pack.pacakge + "\\s*installer=(.*)", "$1");
						//pack.installer = output.replaceAll("^package:(.*)=" + pack.pacakge + "\\s*installer=(.*)", "$2");
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
		if(pack != null) {
			if(pack.apkPath == null) {
				pack.apkPath = pack.codePath;
			}
			pack.label = pack.apkPath.replaceAll(".*/", "") + " - [" + pack.pacakge + "] - " + verName + "/" + verCode;
			list.add(pack);
		}

		result = AdbWrapper.shell(device, new String[] {"ls", "/system/framework/*.apk"}, null);
		for(String line: result) {
			if(line.equals("/system/framework/framework-res.apk")
					|| !line.endsWith(".apk")) continue;
			pack = new PackageListObject();
			pack.apkPath = line;
			pack.codePath = "/system/framework";
			pack.pacakge = pack.apkPath.replaceAll(".*/(.*)\\.apk", "$1");
			pack.label = pack.apkPath.replaceAll(".*/", "");
			list.add(pack);
		}

		return list;
	}

	public static PackageInfo getPackageInfo(String device, String pkgName)
	{
		String[] result;
		String[] dumpSys = null;
		String verName = null;
		int verCode = 0;
		String codePath = null;
		String apkPath = null;
		String installer = null;

		if(pkgName == null) return null;

		//Log.i("ckeckPackage() " + pkgName);

		if(!pkgName.matches("/system/framework/.*apk")) {
			result = AdbWrapper.shell(device, new String[] {"pm", "list", "packages", "-f", "-i", "-u", pkgName}, null);
			for(String output: result) {
				if(output.matches("^package:.*=" + pkgName + "\\s*installer=.*")) {
					apkPath = output.replaceAll("^package:(.*)=" + pkgName + "\\s*installer=(.*)", "$1");
					installer = output.replaceAll("^package:(.*)=" + pkgName + "\\s*installer=(.*)", "$2");
				}
			}

			dumpSys = AdbWrapper.shell(device, new String[] {"dumpsys","package", pkgName}, null);

			verName = selectString(dumpSys,"versionName");
			String vercode = selectString(dumpSys,"versionCode");
			if(vercode != null && vercode.matches("\\d+")) {
				verCode = Integer.valueOf(selectString(dumpSys,"versionCode"));
			}
			codePath = selectString(dumpSys,"codePath");

			if(installer == null) {
				installer = selectString(dumpSys,"installerPackageName");
			}

			if(installer != null && installer.equalsIgnoreCase("null")) {
				installer = null;
			}			
		} else {
			codePath = pkgName;
			apkPath = pkgName;
		}

		boolean isSystemApp = false;
		if((apkPath != null && apkPath.matches("^/system/.*"))
				|| (codePath != null && codePath.matches("^/system/.*"))) {
			isSystemApp = true;
		}

		if(apkPath == null && codePath != null && !codePath.isEmpty() 
				&& (isSystemApp || (!isSystemApp && AdbWrapper.root(device, null)))) {
			result = AdbWrapper.shell(device, new String[] {"ls", codePath}, null);
			for(String output: result) {
				if(output.matches("^.*apk")) {
					apkPath = codePath + "/" + output;
				}
			}
		}

		if(apkPath == null) return null;

		return new PackageInfo(pkgName, apkPath, codePath, verName, verCode, installer, dumpSys, null);
	}

	public static PackageInfo getPackageInfo(IDevice device, String pkgName)
	{
		String[] result;
		String[] dumpSys = null;
		String codePath = null;
		String apkPath = null;
		String installer = null;

		SimpleOutputReceiver outputReceiver = new SimpleOutputReceiver();
		outputReceiver.setTrimLine(false);

		if(pkgName == null) return null;

		//Log.i("ckeckPackage() " + pkgName);

		if(!pkgName.matches("/system/framework/.*apk")) {
			try {
				device.executeShellCommand("pm list packages -f -i -u " + pkgName, outputReceiver);
			} catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException
					| IOException e) {
				e.printStackTrace();
			}
			result = outputReceiver.getOutput();
			for(String output: result) {
				if(output.matches("^package:.*=" + pkgName + "\\s*installer=.*")) {
					apkPath = output.replaceAll("^package:(.*)=" + pkgName + "\\s*installer=(.*)", "$1");
					installer = output.replaceAll("^package:(.*)=" + pkgName + "\\s*installer=(.*)", "$2");
				}
			}

			outputReceiver.clear();
			try {
				device.executeShellCommand("dumpsys package " + pkgName, outputReceiver);
			} catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException
					| IOException e) {
				e.printStackTrace();
			}
			dumpSys = outputReceiver.getOutput();

			codePath = selectString(dumpSys,"codePath=");

			if(installer == null)
				installer = selectString(dumpSys,"installerPackageName=");

			if(installer != null && installer.equals("null"))
				installer = null;
		} else {
			codePath = pkgName;
			apkPath = pkgName;
		}

		boolean isSystemApp = false;
		if((apkPath != null && apkPath.matches("^/system/.*"))
				|| (codePath != null && codePath.matches("^/system/.*"))) {
			isSystemApp = true;
		}

		if(apkPath == null && codePath != null && !codePath.isEmpty()) {
			boolean isRoot = false;
			if(!isSystemApp){
				outputReceiver.clear();
				try {
					device.executeShellCommand("id", outputReceiver);
				} catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException
						| IOException e) {
					e.printStackTrace();
				}
				result = outputReceiver.getOutput();
				for(String output: result) {
					if(output.indexOf("uid=0") > -1) {
						isRoot = true;
					}
				}
			}
			if(isSystemApp || isRoot) {
				outputReceiver.clear();
				try {
					device.executeShellCommand("ls " + codePath, outputReceiver);
				} catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException
						| IOException e) {
					e.printStackTrace();
				}
				result = outputReceiver.getOutput();
				for(String output: result) {
					if(output.matches("^.*apk")) {
						apkPath = codePath + "/" + output;
					}
				}
			}
		}


		Log.e(">>>>>>>>>>>>>>> check sign");
		outputReceiver.clear();
		try {
			device.executeShellCommand("cat /data/system/packages.xml", outputReceiver);
		} catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException
				| IOException e) {
			e.printStackTrace();
		}
		StringBuilder xmlContent = new StringBuilder();
		for(String s: outputReceiver.getOutput()) {
			xmlContent.append(s);
		}

		String sig = "";
		if(xmlContent.indexOf("Permission denied") <= -1) { 
			XmlPath packagesXml = new XmlPath(xmlContent.toString());

			packagesXml.getNodeList("/packages/package[@name='" + pkgName + "']/sigs/cert");

			ArrayList<String> sigsList = new ArrayList<String>(); 
			int signCount = packagesXml.getLength();
			for(int i = 0; i < signCount; i++) {
				String key = packagesXml.getAttributes(i, "key");

				if(key == null || key.isEmpty()) {
					XmlPath keyPath = new XmlPath(packagesXml);
					String index = packagesXml.getAttributes(i, "index");
					keyPath.getNodeList("/packages/package/sigs/cert[@index='"+index+"' and @key]");
					int keyCount = keyPath.getLength();
					for(int j=0; j < keyCount; j++) {
						key = keyPath.getAttributes(j, "key");
						if(key == null || key.isEmpty()) {
							continue;
						}
						sigsList.add(key);
					}
				} else {
					sigsList.add(key);
				}
			}
			if(!sigsList.isEmpty()) {
				sig = sigsList.get(0);
			}
		} else {
			sig = "Permission denied";
		}

		return new PackageInfo(device, pkgName, apkPath, codePath, installer, dumpSys, sig);
	}

	private static String selectString(String[] source, String key)
	{
		String temp = null;
		for(String line: source) {
			if(line.indexOf(" " + key + "=") > -1) {
				temp = line.replaceAll(".*\\s+" + key + "=(\\d+-\\d+-\\d+\\s+\\d+:\\d+:\\d+|[^\\[][^\\s\\{]*(\\{[^\\}]*\\})?|\\[[^\\]]*\\]).*", "$1");
				if(!line.equals(temp)) {
					break;
				}
			}
		}
		return temp;
	}
}
