package com.apkscanner.tool.adb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.data.apkinfo.ComponentInfo;
import com.apkscanner.tool.adb.SimpleOutputReceiver;
import com.apkscanner.util.Log;
import com.apkscanner.util.XmlPath;

public class PackageInfo {

	public final IDevice device;
	public final String packageName;

	String apkPath;
	String codePath;
	String versionName;
	int versionCode;
	String installer;
	String label;

	String[] dumpsys;
	String signature;

	PackageInfo(IDevice device, String pkgName)
	{
		this.device = device;
		this.packageName = pkgName;
	}

	public IDevice getDevice() {
		return device;
	}

	public String getPakcageName() {
		return packageName;
	}

	public String getApkPath() {
		if(apkPath != null) return apkPath;

		String[] result;
		SimpleOutputReceiver outputReceiver = new SimpleOutputReceiver();

		if(!packageName.matches("/system/framework/.*apk")) {
			try {
				device.executeShellCommand("pm list packages -f -i -u " + packageName, outputReceiver);
			} catch (TimeoutException | ShellCommandUnresponsiveException
					| IOException e) {
				e.printStackTrace();
			} catch (AdbCommandRejectedException e1) {
				Log.w(e1.getMessage());
			}
			result = outputReceiver.getOutput();
			for(String output: result) {
				if(output.matches("^package:.*=" + packageName + "\\s*installer=.*")) {
					apkPath = output.replaceAll("^package:(.*)=" + packageName + "\\s*installer=(.*)", "$1");
					installer = output.replaceAll("^package:(.*)=" + packageName + "\\s*installer=(.*)", "$2");
				}
			}
		} else {
			codePath = packageName;
			apkPath = packageName;
		}

		if(apkPath == null && getCodePath() != null) {
			boolean isSystemApp = (apkPath != null && apkPath.matches("^/system/.*"))
					|| (codePath != null && codePath.matches("^/system/.*"));

			if(isSystemApp || AdbDeviceHelper.isRoot(device)) {
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

		return apkPath;
	}

	public String getRealApkPath() {
		String realPath = apkPath;
		if(!realPath.endsWith(".apk")) {
			Log.i("No apk file path : " + realPath);
			realPath += "/*.apk";
		}

		SimpleOutputReceiver outputReceiver = new SimpleOutputReceiver();
		try {
			device.executeShellCommand("ls " + realPath, outputReceiver);
		} catch (TimeoutException | ShellCommandUnresponsiveException | IOException e) {
			e.printStackTrace();
		} catch (AdbCommandRejectedException e1) {
			Log.w(e1.getMessage());
		}
		String[] result = outputReceiver.getOutput();
		if(result.length == 0 || !result[0].endsWith(".apk")) {
			Log.e("No such apk file : " + realPath);
			return null;
		}
		realPath = result[0];
		Log.i("Cahnge target apk path to " + realPath);

		return realPath;
	}

	public String getCodePath() {
		if(codePath != null) return codePath;
		if(packageName.matches("/system/framework/.*apk")) {
			apkPath = packageName;
			codePath = packageName;
		} else {
			codePath = getValue("codePath");
		}
		if(codePath != null && codePath.isEmpty()) {
			codePath = null;
		}
		return codePath;
	}

	public String getInstaller() {
		if(installer != null) return installer;

		getApkPath();
		if(installer != null) return installer;

		installer = getValue("installerPackageName");

		if(installer != null && installer.equalsIgnoreCase("null")) {
			installer = null;
		}

		return installer;
	}

	public String getVersionName() {
		return getValue("versionName");
	}

	public int getVersionCode() {
		int versionCode = 0;
		String tmp = getValue("versionCode");
		if(tmp != null && tmp.matches("\\d+")) {
			versionCode = Integer.parseInt(tmp);
		}
		return versionCode;
	}

	public String getLabel() {
		return label;
	}

	public boolean isEnabled() {
		String statue = getValue("enabled");
		return statue.equals("0") || statue.equals("1");
	}

	public String getEnabledStateToString() {
		int state = -1;
		String value = getValue("enabled");
		if(value != null && value.matches("\\d+")) {
			state = Integer.parseInt(value);
		}

		return getEnabledStateToString(state);
	}

	public static String getEnabledStateToString(int state) {
		switch(state) {
		case 0: return "DEFAULT";
		case 1: return "ENABLED";
		case 2: return "DISABLED";
		case 3: return "DISABLED_USER";
		case 4: return "DISABLED_UNTIL_USED";
		default: return "UNKNOWN_" + state;
		}
	}

	public String[] getDumpsys() {
		if(dumpsys != null) return dumpsys;

		SimpleOutputReceiver outputReceiver = new SimpleOutputReceiver();
		outputReceiver.setTrimLine(false);
		try {
			device.executeShellCommand("dumpsys package " + packageName, outputReceiver);
		} catch (TimeoutException | ShellCommandUnresponsiveException
				| IOException e) {
			e.printStackTrace();
		} catch (AdbCommandRejectedException e1) {
			Log.w(e1.getMessage());
		}
		dumpsys = outputReceiver.getOutput();

		return dumpsys;
	}

	public String getValue(String block, String key) {
		String value = null;
		String blockRegex = "^(\\s*)" + block + ":$";
		String blockEndRegex = "";
		boolean startInfoBlock = false;
		for(String line: getDumpsys()) {
			if(!startInfoBlock) {
				if(line.matches(blockRegex)) {
					startInfoBlock = true;
					blockEndRegex = "^" + line.replaceAll(blockRegex, "$1") + "\\S.*";
				}
				continue;
			} else {
				if(line.matches(blockEndRegex)) {
					break;
				}
			}
			if(line.contains(" " + key + "=")) {
				value = line.replaceAll(".*\\s+" + key + "=(\\d+-\\d+-\\d+\\s+\\d+:\\d+:\\d+|[^\\[][^\\s\\{]*(\\{[^\\}]*\\})?|\\[[^\\]]*\\]).*", "$1");
				if(!line.equals(value)) {
					value = value.trim();
					break;
				}
				value = null;
			} else if(line.contains(" " + key + ":")) {
				value = line.replaceAll(".*\\s+" + key + ":\\s*(.*)", "$1");
				if(!line.equals(value)) {
					value = value.trim();
					break;
				}
				value = null;
			}
		}
		return value;
	}

	public String getValue(String key) {
		return getValue("Packages", key);
	}

	public String getHiddenSystemPackageValue(String key) {
		return getValue("Hidden system packages", key);
	}

	public boolean isSystemApp() {
		return (apkPath != null && apkPath.matches("^/system/.*"))
				|| (codePath != null && codePath.matches("^/system/.*"));
	}

	public boolean hasLauncher() {
		ComponentInfo[] comp = getLauncherActivityList(false);
		return (comp != null && comp.length > 0);
	}

	public ComponentInfo[] getLauncherActivityList(boolean includeMain) {
		String blockRegex = "^(\\s*)Activity Resolver Table:\\s*$";
		String blockEndRegex = "";
		boolean startInfoBlock = false;

		HashMap<String, ComponentInfo> components = new HashMap<String, ComponentInfo>();
		ComponentInfo curComp = null;

		for(String line: getDumpsys()) {
			if(!startInfoBlock) {
				if(line.matches(blockRegex)) {
					startInfoBlock = true;
					blockEndRegex = "^" + line.replaceAll(blockRegex, "$1") + "\\S.*";
				}
				continue;
			} else {
				if(line.matches(blockEndRegex)) {
					break;
				}
			}

			if(line.contains(" filter ")) {
				String name = line.replaceAll("\\s*[0-9a-f]+\\s" + packageName + "/(\\S+)\\sfilter\\s[0-9a-f]+", "$1");
				if(!line.equals(name)) {
					curComp = components.get(name);
					if(curComp == null) {
						curComp = new ComponentInfo();
						curComp.name = name;
						components.put(name, curComp);
					}
				}
			}
			if(curComp == null) {
				continue;
			}

			if(line.contains("Action: \"android.intent.action.MAIN\"")) {
				curComp.featureFlag |= ApkInfo.APP_FEATURE_MAIN;
			} else if(line.contains("Category: \"android.intent.category.LAUNCHER\"")) {
				curComp.featureFlag |= ApkInfo.APP_FEATURE_LAUNCHER;
			} else if(line.contains("Category: \"android.intent.category.DEFAULT\"")) {
				curComp.featureFlag |= ApkInfo.APP_FEATURE_DEFAULT;
			}
		}

		ArrayList<ComponentInfo> launcherList = new ArrayList<ComponentInfo>();
		ArrayList<ComponentInfo> mainList = new ArrayList<ComponentInfo>();
		ArrayList<ComponentInfo> defualtList = new ArrayList<ComponentInfo>();

		for(ComponentInfo info: components.values()) {
			if((info.featureFlag & ApkInfo.APP_FEATURE_LAUNCHER) != 0) {
				launcherList.add(info);
			} else if(includeMain) {
				if((info.featureFlag & ApkInfo.APP_FEATURE_MAIN) != 0) {
					mainList.add(info);
				} else if((info.featureFlag & ApkInfo.APP_FEATURE_DEFAULT) != 0) {
					defualtList.add(info);
				}
			}
		}

		if(includeMain) {
			launcherList.addAll(mainList);
			launcherList.addAll(defualtList);
		}

		return launcherList.toArray(new ComponentInfo[launcherList.size()]);
	}

	public String getSignature() {

		if(signature != null) return signature;

		SimpleOutputReceiver outputReceiver = new SimpleOutputReceiver();
		try {
			device.executeShellCommand("cat /data/system/packages.xml", outputReceiver);
		} catch (TimeoutException | ShellCommandUnresponsiveException
				| IOException e) {
			e.printStackTrace();
			return null;
		} catch (AdbCommandRejectedException e1) {
			Log.w(e1.getMessage());
			return null;
		}

		StringBuilder xmlContent = new StringBuilder();
		for(String s: outputReceiver.getOutput()) {
			xmlContent.append(s);
		}

		if(xmlContent.indexOf("Permission denied") <= -1) {
			XmlPath packagesXml = new XmlPath(xmlContent.toString());
			XmlPath certs = packagesXml.getNodeList("/packages/package[@name='" + packageName + "']/sigs/cert");

			ArrayList<String> sigsList = new ArrayList<String>();
			int signCount = certs.getCount();
			for(int i = 0; i < signCount; i++) {
				String key = certs.getAttribute(i, "key");

				if(key == null || key.isEmpty()) {
					String index = certs.getAttribute(i, "index");
					XmlPath keyPath = packagesXml.getNodeList("/packages/package/sigs/cert[@index='"+index+"' and @key]");
					int keyCount = keyPath.getCount();
					for(int j=0; j < keyCount; j++) {
						key = keyPath.getAttribute(j, "key");
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
				signature = sigsList.get(0);
			}
		} else {
			signature = "Permission denied";
		}

		return signature;
	}

	public void clear() {
		apkPath = null;
		codePath = null;
		versionName = null;
		versionCode = 0;
		installer = null;

		dumpsys = null;
		signature = null;
	}

	@Override
	public String toString()
	{
		/*
		String s = "-Installed APK info\n";
		s += "Pakage : " + packageName +"\n";
		s += "Version : " + versionName + " / " + versionCode +"\n";
		s += "APK Path : " + apkPath +"\n";
		s += "Installer : " + installer +"\n";
		return s;
		*/
		return getLabel();
	}
}

