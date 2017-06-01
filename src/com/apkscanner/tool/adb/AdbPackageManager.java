package com.apkscanner.tool.adb;

public class AdbPackageManager {

	public static PackageInfo getPackageInfo(String device, String pkgName)
	{
		/*
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
				verCode = Integer.parseInt(selectString(dumpSys,"versionCode"));
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
		 */
		return null;//new PackageInfo(pkgName, apkPath, codePath, verName, verCode, installer, dumpSys, null);
	}
/*
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
	*/
}
