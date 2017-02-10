package com.apkscanner.tool.adb;

import java.util.ArrayList;

import com.apkscanner.util.ConsolCmd;

public class AdbPackageManager {

	static private final String adbCmd = AdbWrapper.getAdbCmd();
	

	static public class PackageListObject {
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
	
	static public class PackageInfo
	{
		public final String pkgName;
		public final String apkPath;
		public final String codePath;
		public final String versionName;
		public final String versionCode;
		public final boolean isSystemApp;
		public final String installer;
		
		public PackageInfo(String pkgName, String apkPath, String codePath, String versionName, String versionCode, boolean isSystemApp, String installer)
		{
			this.pkgName = pkgName;
			this.apkPath = apkPath;
			this.codePath = codePath;
			this.versionName = versionName;
			this.versionCode = versionCode;
			this.isSystemApp = isSystemApp;
			this.installer = installer;
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
	}
	
	static public ArrayList<PackageListObject> getPackageList(String device)
	{
		ArrayList<PackageListObject> list = new ArrayList<PackageListObject>();
		
		String[] cmd = {adbCmd, "-s", device, "shell", "dumpsys", "package"};
		String[] result = ConsolCmd.exc(cmd, false, null);
		
		cmd = new String[] {adbCmd, "-s", device, "shell", "pm", "list", "packages", "-f", "-i"};
		String[] pmList = ConsolCmd.exc(cmd, false, null);		
		
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
		
		cmd = new String[] {adbCmd, "-s", device, "shell", "ls", "/system/framework/*.apk"};
		result = ConsolCmd.exc(cmd, false, null);
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

	static public PackageInfo getPackageInfo(String device, String pkgName)
	{
		String[] cmd;
		String[] result;
		String[] TargetInfo;
		String verName = null;
		String verCode = null;
		String codePath = null;
		String apkPath = null;
		String installer = null;

		if(pkgName == null) return null;
		
		//Log.i("ckeckPackage() " + pkgName);

		if(!pkgName.matches("/system/framework/.*apk")) {
			cmd = new String[] {adbCmd, "-s", device, "shell", "pm", "list", "packages", "-f", "-i", pkgName};
			result = ConsolCmd.exc(cmd, false, null);		
			for(String output: result) {
		    	if(output.matches("^package:.*=" + pkgName + "\\s*installer=.*")) {
		    		apkPath = output.replaceAll("^package:(.*)=" + pkgName + "\\s*installer=(.*)", "$1");
		    		installer = output.replaceAll("^package:(.*)=" + pkgName + "\\s*installer=(.*)", "$2");
		    	}
			}
			
			cmd = new String[] {adbCmd,"-s", device, "shell", "dumpsys","package", pkgName};
			TargetInfo = ConsolCmd.exc(cmd,false,null);
			
			verName = selectString(TargetInfo,"versionName=");
			verCode = selectString(TargetInfo,"versionCode=");
			codePath = selectString(TargetInfo,"codePath=");
			
			if(installer == null)
				installer = selectString(TargetInfo,"installerPackageName=");

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
		
		if(apkPath == null && codePath != null && !codePath.isEmpty() 
				&& (isSystemApp || (!isSystemApp && AdbWrapper.root(device, null)))) {
			cmd = new String[] {adbCmd, "-s", device, "shell", "ls", codePath};
			result = ConsolCmd.exc(cmd, false, null);
			for(String output: result) {
		    	if(output.matches("^.*apk")) {
		    		apkPath = codePath + "/" + output;
		    	}
			}
		}

		if(apkPath == null) return null;
		
		return new PackageInfo(pkgName, apkPath, codePath, verName, verCode, isSystemApp, installer);
	}
	
	static private String selectString(String[] source, String key)
	{
		String temp = null;
		
		for(int i=0; i < source.length; i++) {
			if(source[i].matches("^\\s*"+key+".*$")) {
				temp = source[i].replaceAll("^\\s*"+key+"\\s*([^\\s]*).*$", "$1");
				break;
			}
		}
		return temp;
	}
}
