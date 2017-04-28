package com.apkscanner.tool.adb;

import java.util.ArrayList;
import java.util.HashMap;

import com.android.ddmlib.IDevice;
import com.apkscanner.data.apkinfo.ActivityAliasInfo;
import com.apkscanner.data.apkinfo.ActivityInfo;
import com.apkscanner.data.apkinfo.ApkInfo;
import com.apkscanner.data.apkinfo.ComponentInfo;
import com.apkscanner.data.apkinfo.ProviderInfo;
import com.apkscanner.data.apkinfo.ReceiverInfo;
import com.apkscanner.data.apkinfo.ServiceInfo;

public class PackageInfo {

	public final String pkgName;
	public final String apkPath;
	public final String codePath;
	public final String versionName;
	public final int versionCode;
	public final String installer;

	public final String[] dumpsys;

	public final String signature;

	public final IDevice device;

	public ActivityInfo[] activity = null;
	public ActivityAliasInfo[] activityAlias = null;
	public ServiceInfo[] service = null;
	public ReceiverInfo[] receiver = null;
	public ProviderInfo[] provider = null;

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

	public String getValue(String block, String key) {
		String value = null;
		String blockRegex = "^(\\s*)" + block + ":$";
		String blockEndRegex = "";
		boolean startInfoBlock = false;
		for(String line: dumpsys) {
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

	public PackageInfo getHiddenSystemPackage() {
		//String pkgName, String apkPath, String codePath, String versionName, int versionCode, String installer, String[] dumpsys, String signature
		String codePath = getHiddenSystemPackageValue("codePath");
		String apkPath = codePath;
		String installer = getHiddenSystemPackageValue("installerPackageName");
		return new PackageInfo(device, pkgName, apkPath, codePath, installer, dumpsys, signature);
	}

	public ComponentInfo[] getLauncherActivityList(boolean includeMain) {
		String blockRegex = "^(\\s*)Activity Resolver Table:\\s*$";
		String blockEndRegex = "";
		boolean startInfoBlock = false;

		HashMap<String, ComponentInfo> components = new HashMap<String, ComponentInfo>();
		ComponentInfo curComp = null;

		for(String line: dumpsys) {
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

			if(line.indexOf(" filter ") > -1) {
				String name = line.replaceAll("\\s*[0-9a-f]+\\s" + pkgName + "/(\\S+)\\sfilter\\s[0-9a-f]+", "$1");
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

			if(line.indexOf("Action: \"android.intent.action.MAIN\"") > -1) {
				curComp.featureFlag |= ApkInfo.APP_FEATURE_MAIN;
			} else if(line.indexOf("Category: \"android.intent.category.LAUNCHER\"") > -1) {
				curComp.featureFlag |= ApkInfo.APP_FEATURE_LAUNCHER;
			} else if(line.indexOf("Category: \"android.intent.category.DEFAULT\"") > -1) {
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
}

