package com.ApkInfo.Core;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.ApkInfo.Resource.Resource;

public class AdbWrapper
{
	static private final String adbCmd = getAdbCmd();
	
	static public interface AdbWrapperListener
	{
		public void OnMessage(String msg);
		public void OnError();
		public void OnSuccess();
		public void OnCompleted();
	}

	enum INSTALL_TYPE {
		INSTALL,
		PUSH
	}
	
	static public class DeviceStatus
	{
		public final String name;
		public final String status;

		public DeviceStatus(String name, String status)
		{
			this.name = name;
			this.status = status;
		}
	}
	
	static public class PackageInfo
	{
		public final String pkgName;
		public final String apkName;
		public final String codePath;
		public final String versionName;
		public final String versionCode;
		public final boolean isSystemApp;
		
		public PackageInfo(String pkgName, String apkName, String codePath, String versionName, String versionCode, boolean isSystemApp)
		{
			this.pkgName = pkgName;
			this.apkName = apkName;
			this.codePath = codePath;
			this.versionName = versionName;
			this.versionCode = versionCode;
			this.isSystemApp = isSystemApp;
		}
		
		public String getSummary()
		{
			return null;
		}
	}
	
	static public class DeviceInfo
	{
		public final String serialNumber;
		public final String deviceName;
		public final String modelName;
		public final String osVersion;
		public final String buildVersion;
		public final String sdkVersion;
		public final String buildType;
		public final boolean isAbi64;
		public final boolean isRoot;
		
		public DeviceInfo(
				String serialNumber,
				String deviceName,
				String modelName,
				String osVersion,
				String buildVersion,
				String sdkVersion,
				String buildType,
				boolean isAbi64,
				boolean isRoot)
		{
			this.serialNumber = serialNumber;
			this.deviceName = deviceName;
			this.modelName = modelName;
			this.osVersion = osVersion;
			this.buildVersion = buildVersion;
			this.sdkVersion = sdkVersion;
			this.buildType = buildType;
			this.isAbi64 = isAbi64;
			this.isRoot = isRoot;
		}
		
		public String getSummary()
		{
			return null;
		}
	}

	static private String getAdbCmd()
	{
		String cmd = adbCmd;
		if(cmd == null) {
			cmd = Resource.BIN_ADB_LNX.getPath();
			if(cmd.matches("^[A-Z]:.*")) {
				cmd = Resource.BIN_ADB_WIN.getPath();
			}
	
			if(!(new File(cmd)).exists()) {
				System.out.println("no such adb tool" + adbCmd);
				cmd = null;
			}
		}

		return cmd;
	}

	static public boolean ckeckAdbTool()
	{
		System.out.println("ckeckAdbTool()");
		if(adbCmd == null) return false;

		MyConsolCmd.exc(new String[] {adbCmd, "kill-server"}, false, null);
		String[] result = MyConsolCmd.exc(new String[] {adbCmd, "start-server"}, false, null);
		return result[1].matches(".*daemon started successfully.*");
	}

	static public ArrayList<DeviceStatus> scanDevices()
	{
		String[] cmdResult;
		ArrayList<DeviceStatus> deviceList = new ArrayList<DeviceStatus>();

		if(adbCmd == null) return null;

		String[] cmd = {adbCmd, "devices"};
		cmdResult = MyConsolCmd.exc(cmd,true,null);
		
		for(String output: cmdResult) {
			if(output.matches("^.*\\s*device\\s*$")){
				String name = output.replaceAll("^\\s*([^\\s]*)\\s*device\\s*$", "$1");
				deviceList.add(new DeviceStatus(name, "device"));
			}
		}
		return deviceList;
	}

	static public DeviceInfo getDeviceInfo(String name)
	{
		if(adbCmd == null) return null;

		String serialNumber = name;
		String deviceName = getSystemProp(name, "ro.product.device");
		String modelName = getSystemProp(name, "ro.product.model");
		String osVersion = null; // getSystemProp(name, "ro.build.version.release");
		String buildVersion = null; // getSystemProp(name, "ro.build.version.incremental");
		String sdkVersion = null; // getSystemProp(name, "ro.build.version.sdk");
		String buildType = null; // getSystemProp(name, "ro.build.type");
		boolean isAbi64 = !getSystemProp(name, "ro.product.cpu.abilist64").isEmpty();
		
		boolean isRoot = true;
		String[] cmd = {adbCmd, "-s", name, "root"};
		String[] result = MyConsolCmd.exc(cmd, false, null);
		if(result[0].equals("adbd cannot run as root in production builds")) {
			isRoot = false;
		}
		
		return new DeviceInfo(serialNumber, deviceName, modelName, osVersion, buildVersion, sdkVersion, buildType, isAbi64, isRoot);
	}
	
	static public String getSystemProp(String device, String tag)
	{
		if(adbCmd == null) return null;

		String[] TargetInfo;
		String[] cmd = {adbCmd, "-s", device, "shell", "getprop", tag};

		TargetInfo = MyConsolCmd.exc(cmd,false,null);
		
		return TargetInfo[0];
	}

	static public PackageInfo getPackageInfo(String device, String pkgName)
	{
		String[] TargetInfo;

		if(pkgName == null) return null;
		
		System.out.println("ckeckPackage() " + pkgName);

		String[] cmd = {adbCmd,"-s", device, "shell", "dumpsys","package",pkgName};
		TargetInfo = MyConsolCmd.exc(cmd,false,null);
		
		if(TargetInfo.length < 3) return null;
		
		String verName = selectString(TargetInfo,"versionName=");
		String verCode = selectString(TargetInfo,"versionCode=");
		String codePath = selectString(TargetInfo,"codePath=");

		boolean isSystemApp = false;
		if(codePath != null && codePath.matches("^/system/.*")) {
			isSystemApp = true;
		}

		String apkPath = null;
		if(codePath != null && !codePath.isEmpty()) {
			String[] cmd2 = {adbCmd, "-s", device, "shell", "ls", codePath};
			String[] result = MyConsolCmd.exc(cmd2, false, null);
			for(String output: result) {
		    	if(output.matches("^.*apk")) {
		    		apkPath = codePath + "/" + output;
		    	}
			}
		}
		
		return new PackageInfo(pkgName, apkPath, codePath, verName, verCode, isSystemApp);
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
	
	static public void reboot(String device)
	{
		if(adbCmd == null) return;
		MyConsolCmd.exc(new String[] {adbCmd, "-s", device, "reboot"});
	}
	
	static public void PushApk(DeviceInfo devInfo, PackageInfo pkgInfo, String apkPath, String libPath, AdbWrapperListener listener)
	{
		System.out.println("PushApk() device : " + devInfo + ", apkPath: " + apkPath);
		if(adbCmd == null || devInfo == null || pkgInfo == null || apkPath == null || apkPath.isEmpty()) {
			if(listener != null) {
				listener.OnError();
				listener.OnCompleted();
			}
			return;
		}

		new MyCoreThead(INSTALL_TYPE.INSTALL, devInfo, pkgInfo, apkPath, libPath, listener).start();

		return;
	}
	
	static public void InstallApk(DeviceInfo devInfo, String apkPath, AdbWrapperListener listener)
	{
		System.out.println("InstallApk() device : " + devInfo + ", apkPath: " + apkPath);
		if(adbCmd == null || devInfo == null || apkPath == null || apkPath.isEmpty()) {
			if(listener != null) {
				listener.OnError();
				listener.OnCompleted();
			}
			return;
		}

		new MyCoreThead(INSTALL_TYPE.INSTALL, devInfo, null, apkPath, null, listener).start();
		
		return;
	}
	
	static class MyCoreThead extends Thread
	{
		private INSTALL_TYPE type;
		private String device;
		private String apkPath;
		private String libPath;
		private DeviceInfo devInfo;
		private PackageInfo pkgInfo;
		private AdbWrapperListener listener;
		
		MyCoreThead(INSTALL_TYPE type, DeviceInfo devInfo, PackageInfo pkgInfo, String apkPath, String libPath, AdbWrapperListener listener)
		{
			this.type = type;
			this.devInfo = devInfo;
			this.pkgInfo = pkgInfo;
			this.device = devInfo.serialNumber;
			this.apkPath = apkPath;
			this.libPath = libPath;
			this.listener = listener;
		}
		
		public void run()
		{			
			if(type == INSTALL_TYPE.INSTALL) {
				String[] result;
				String[] cmd = {adbCmd, "-s", this.device, "install", "-d","-r", this.apkPath};
				
				result = MyConsolCmd.exc(cmd,true,new MyConsolCmd.OutputObserver() {
					@Override
					public boolean ConsolOutput(String output) {
						sendMessage(output.replaceAll("^.*adb(\\.exe)?", "adb"));
						return true;
					}
				});
				
				if(listener != null) {
					if(result[2].equals("Success")) {
						listener.OnSuccess();
					} else {
						listener.OnError();
					}
					listener.OnCompleted();
				}
			} else {
				String[][] result;
				List<String[]> cmd = new ArrayList<String[]>();
				cmd.add(new String[] {adbCmd, "-s", this.device, "root"});
				cmd.add(new String[] {adbCmd, "-s", this.device, "remount"});
				cmd.add(new String[] {adbCmd, "-s", this.device, "shell", "su", "-c", "setenforce", "0"});
				cmd.add(new String[] {adbCmd, "-s", this.device, "push", this.apkPath, pkgInfo.apkName});
				//System.out.println(this.sourcePath + " to " + device.strApkPath);

				Iterator<String> libPaths = CoreApkTool.findFiles(new File(libPath), ".so", null).iterator();
				while(libPaths.hasNext()) {
					String path = libPaths.next();
					if(!(new File(path)).exists()) {
						System.out.println("no such file : " + path);
						continue;
					}
					if(path.matches(libPath.replace("\\", "\\\\")+"arm64.*")) {
						if(devInfo.isAbi64) {
							cmd.add(new String[] {adbCmd, "-s", this.device, "push", path, "/system/lib64/"});
							System.out.println("push " + path + " " + "/system/lib64/");
						} else {
							System.out.println("ignored lib64 : " + path);
						}
					} else {
						if(!devInfo.isAbi64) {
							cmd.add(new String[] {adbCmd, "-s", this.device, "push", path, "/system/lib/"});
							System.out.println("push " + path + " " + "/system/lib/");
						} else {
							System.out.println("ignored lib32 path : " + path);
						}
					}
				}
				cmd.add(new String[] {adbCmd, "-s", this.device, "shell", "echo", "Compleated..."});
				
				result = MyConsolCmd.exc(cmd.toArray(new String[0][0]),true,new MyConsolCmd.OutputObserver() {
					@Override
					public boolean ConsolOutput(String output) {
						sendMessage(output.replaceAll("^.*adb(\\.exe)?", "adb"));
				    	if(output.equals("* failed to start daemon *")
				    		|| output.equals("error: device not found")
				    		|| output.equals("adbd cannot run as root in production builds")
				    		|| output.matches(".*Permission denied.*")
				    	) {
				    		System.out.println(">>>>>>>>>>>> fail : " + output);
				    		return false;
				    	}
				    	return true;
					}
				});

				System.out.println("cmd.size() " + cmd.size() + ", result.length " + result.length);
				if(listener != null) {
					if(cmd.size() == result.length) {
						sendMessage("Success...");
						listener.OnSuccess();
					} else {
						sendMessage("Failure...");
						listener.OnError();
					}
					listener.OnCompleted();
				}
			}
		}
		
		private void sendMessage(String msg) {
			if(listener != null) listener.OnMessage(msg);
		}
	}
}
