package com.ApkInfo.Core;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.ApkInfo.Resource.Resource;

public class AdbWrapper
{
	static private final String adbCmd = getAdbCmd();
	static private final String adbVersion = getVersion();
	
	static public interface AdbWrapperListener
	{
		public void OnMessage(String msg);
		public void OnError();
		public void OnSuccess();
		public void OnCompleted();
	}

	enum INSTALL_TYPE {
		INSTALL,
		PUSH,
		PULL
	}
	
	static public class DeviceStatus
	{
		public final String name;
		public final String status;
		public final String usb;
		public final String product;
		public final String model;
		public final String device;

		public DeviceStatus(String name, String status, String usb, String product, String model, String device)
		{
			this.name = name.trim();
			this.status = status.trim();
			this.usb = usb.trim();
			this.product = product.trim();
			this.model = model.trim();
			this.device = device.trim();
		}
		
		public String getSummary()
		{
			String s = "-Device info\n";
			s += "name : " + name + "\n";
			s += "status : " + status + "\n";
			s += "product : " + product + "\n";
			s += "model : " + model + "\n";
			s += "device : " + device + "\n";
			return s;
		}
	}

	static public class PackageListObject {
		public String label;
		public String pacakge;
		public String codePath;
		
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
		
		public PackageInfo(String pkgName, String apkPath, String codePath, String versionName, String versionCode, boolean isSystemApp)
		{
			this.pkgName = pkgName;
			this.apkPath = apkPath;
			this.codePath = codePath;
			this.versionName = versionName;
			this.versionCode = versionCode;
			this.isSystemApp = isSystemApp;
		}
		
		public String getSummary()
		{
			String s = "-Installed APK info\n";
			s += "Pakage : " + pkgName +"\n";
			s += "Version : " + versionName + " / " + versionCode +"\n";
			s += "CodePath : " + codePath +"\n";
			return s;
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
			String s = "Model : " + modelName + " / " + deviceName + "\n";
			s += "Version : " + buildVersion + "(" + buildType + ") / ";
			s += "" + osVersion + "(" + sdkVersion + ")\n";
			return s;
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
	
	static public String getVersion()
	{
		if(adbVersion == null) {
			String adb = getAdbCmd();
			if(adb == null) return null;
			String[] result = MyConsolCmd.exc(new String[] {adb, "version"});
			return result[0];
		}
		return adbVersion;
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

		String[] cmd = {adbCmd, "devices", "-l"};
		cmdResult = MyConsolCmd.exc(cmd,true,null);
		
		boolean startList = false;
		for(String output: cmdResult) {
			if(!startList || output.matches("^\\s*$")) {
				if(output.matches("^List.*"))
					startList = true;
				continue;
			}
			output = output.replaceAll("^\\s*([^\\s]*)\\s*([^\\s]*)(\\s*(usb:([^\\s]*))?\\s*product:([^\\s]*)\\s*model:([^\\s]*)\\s*device:([^\\s]*))?\\s*$", "$1 |$2 |$5 |$6 |$7 |$8 ");
			String[] info = output.split("\\|");
			deviceList.add(new DeviceStatus(info[0], info[1], info[2], info[3], info[4], info[5]));
		}
		return deviceList;
	}
	
	static public String getSystemProp(String device, String tag)
	{
		if(adbCmd == null) return null;

		String[] TargetInfo;
		String[] cmd = {adbCmd, "-s", device, "shell", "getprop", tag};

		TargetInfo = MyConsolCmd.exc(cmd,false,null);
		
		return TargetInfo[0];
	}
	
	static public boolean hasRootPermission(String device)
	{
		if(adbCmd == null) return false;
		boolean hasRoot = true;
		String[] cmd = {adbCmd, "-s", device, "root"};
		String[] result = MyConsolCmd.exc(cmd, false, null);
		if(!result[0].matches(".*running as root")) {
			hasRoot = false;
		}
		return hasRoot;
	}
	
	static public void reboot(String device)
	{
		if(adbCmd == null) return;
		MyConsolCmd.exc(new String[] {adbCmd, "-s", device, "reboot"});
	}
	
	static public void PushApk(String name, String srcApkPath, String destApkPath, String libPath, AdbWrapperListener listener)
	{
		System.out.println("PushApk() device : " + name + ", apkPath: " + srcApkPath);
		if(adbCmd == null || name == null || destApkPath == null || srcApkPath == null || srcApkPath.isEmpty()) {
			if(listener != null) {
				listener.OnError();
				listener.OnCompleted();
			}
			return;
		}

		new MyCoreThead(INSTALL_TYPE.PUSH, name, srcApkPath, destApkPath, libPath, listener).start();

		return;
	}
	
	static public void InstallApk(String name, String apkPath, AdbWrapperListener listener)
	{
		System.out.println("InstallApk() device : " + name + ", apkPath: " + apkPath);
		if(adbCmd == null || name == null || apkPath == null || apkPath.isEmpty()) {
			if(listener != null) {
				listener.OnError();
				listener.OnCompleted();
			}
			return;
		}

		new MyCoreThead(INSTALL_TYPE.INSTALL, name, apkPath, null, null, listener).start();
		
		return;
	}
	
	static public void PullApk(String name, String srcApkPath, String destApkPath, AdbWrapperListener listener)
	{
		System.out.println("PullApk() device : " + name + ", apkPath: " + srcApkPath);
		if(adbCmd == null || name == null || destApkPath == null || srcApkPath == null || srcApkPath.isEmpty()) {
			if(listener != null) {
				listener.OnError();
				listener.OnCompleted();
			}
			return;
		}

		new MyCoreThead(INSTALL_TYPE.PULL, name, srcApkPath, destApkPath, null, listener).start();

		return;
	}

	static public DeviceInfo getDeviceInfo(String name)
	{
		if(adbCmd == null) return null;

		String serialNumber = name;
		String deviceName = getSystemProp(name, "ro.product.device");
		String modelName = getSystemProp(name, "ro.product.model");
		String osVersion = getSystemProp(name, "ro.build.version.release");
		String buildVersion = getSystemProp(name, "ro.build.version.incremental");
		String sdkVersion = getSystemProp(name, "ro.build.version.sdk");
		String buildType = getSystemProp(name, "ro.build.type");
		boolean isAbi64 = !getSystemProp(name, "ro.product.cpu.abilist64").isEmpty();
		boolean isRoot = hasRootPermission(name);
		
		return new DeviceInfo(serialNumber, deviceName, modelName, osVersion, buildVersion, sdkVersion, buildType, isAbi64, isRoot);
	}
	
	static public ArrayList<PackageListObject> getPackageList(String device)
	{
		ArrayList<PackageListObject> list = new ArrayList<PackageListObject>();
		
		String[] cmd = {adbCmd, "-s", device, "shell", "dumpsys", "package"};
		String[] result = MyConsolCmd.exc(cmd, false, null);
		
		boolean start = false;
		PackageListObject pack = null;
		String verName = null;
		String verCode = null;
		for(String line: result) {
			if(!start) {
				if(line.matches("^Packages:")) {
					start = true;
				}
				continue;
			}
			if(line.matches("^\\s*Package\\s*\\[.*")) {
				if(pack != null) {
					pack.label = pack.codePath.replaceAll(".*/", "") + "/" + pack.pacakge + " - " + verName + "/" + verCode;
					list.add(pack);
				}
				pack = new PackageListObject();
				verName = null;
				verCode = null;
				pack.pacakge = line.replaceAll("^\\s*Package\\s*\\[(.*)\\].*:\\s*$", "$1");
				pack.codePath = null;
			} else if(pack != null && pack.codePath == null && line.matches("^\\s*codePath=.*$")) {
				pack.codePath = line.replaceAll("^\\s*codePath=\\s*([^\\s]*).*$", "$1");
			} else if(verName == null && line.matches("^\\s*versionName=.*$")) {
				verName = line.replaceAll("^\\s*versionName=\\s*([^\\s]*).*$", "$1");
			} else if(verCode == null && line.matches("^\\s*versionCode=.*$")) {
				verCode = line.replaceAll("^\\s*versionCode=\\s*([^\\s]*).*$", "$1");
			}
		}
		if(pack != null) {
			pack.label = pack.codePath.replaceAll(".*/", "") + "/" + pack.pacakge + " - " + verName + "/" + verCode;
			list.add(pack);
		}

		return list;
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
	
	static class MyCoreThead extends Thread
	{
		private INSTALL_TYPE type;
		private String device;
		private String srcApkPath;
		private String destApkPath;
		private String libPath;
		private AdbWrapperListener listener;
		
		MyCoreThead(INSTALL_TYPE type, String name, String srcApkPath, String destApkPath, String libPath, AdbWrapperListener listener)
		{
			this.type = type;
			this.device = name;
			this.srcApkPath = srcApkPath;
			this.destApkPath = destApkPath;
			this.libPath = libPath;
			this.listener = listener;
		}
		
		public void run()
		{			
			if(type == INSTALL_TYPE.INSTALL) {
				String[] result;
				String[] cmd = {adbCmd, "-s", this.device, "install", "-d","-r", this.srcApkPath};
				
				result = MyConsolCmd.exc(cmd,true,new MyConsolCmd.OutputObserver() {
					@Override
					public boolean ConsolOutput(String output) {
						sendMessage(output.replaceAll("^.*adb(\\.exe)?", "adb"));
						return true;
					}
				});
				
				if(listener != null) {
					listener.OnCompleted();
					if(result.length >= 3 && result[2].equals("Success")) {
						listener.OnSuccess();
					} else {
						listener.OnError();
					}					
				}
			} else if(type == INSTALL_TYPE.PULL) {
				String[] result;
				String[] cmd = {adbCmd, "-s", this.device, "pull", this.srcApkPath, this.destApkPath};

				result = MyConsolCmd.exc(cmd,true,new MyConsolCmd.OutputObserver() {
					@Override
					public boolean ConsolOutput(String output) {
						sendMessage(output.replaceAll("^.*adb(\\.exe)?", "adb"));
						return true;
					}
				});
				
				if(listener != null) {
					listener.OnCompleted();
					if(result[0].matches(".*s\\)")) {
						listener.OnSuccess();
					} else {
						listener.OnError();
					}					
				}
				
			} else if(type == INSTALL_TYPE.PUSH) {
				String[][] result;
				List<String[]> cmd = new ArrayList<String[]>();
				cmd.add(new String[] {adbCmd, "-s", this.device, "root"});
				cmd.add(new String[] {adbCmd, "-s", this.device, "remount"});
				cmd.add(new String[] {adbCmd, "-s", this.device, "shell", "su", "-c", "setenforce", "0"});
				cmd.add(new String[] {adbCmd, "-s", this.device, "push", this.srcApkPath, this.destApkPath});
				System.out.println(this.srcApkPath + " to " + this.destApkPath);
				
				System.out.println("libpath " + libPath);
				if(libPath != null && (new File(libPath)).exists()) {
					String[] selAbi = selectAbi(this.device, libPath);
					String abi32 = selAbi[0];
					String abi64 = selAbi[1];
					
					Iterator<String> libPaths = CoreApkTool.findFiles(new File(libPath), ".so", null).iterator();
					while(libPaths.hasNext()) {
						String path = libPaths.next();
						if(!(new File(path)).exists()) {
							System.out.println("no such file : " + path);
							continue;
						}
						String abi = path.replaceAll(libPath.replace("\\", "\\\\")+"([^\\\\/]*).*","$1");
						System.out.println("abi = " + abi);
						if(abi.equals(abi32)) {
							cmd.add(new String[] {adbCmd, "-s", this.device, "push", path, "/system/lib/"});
							System.out.println("push " + path + " " + "/system/lib/");
						} else if (abi.equals(abi64)) {
							cmd.add(new String[] {adbCmd, "-s", this.device, "push", path, "/system/lib64/"});
							System.out.println("push " + path + " " + "/system/lib64/");						
						} else {
							System.out.println("ignored path : " + path);
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
					listener.OnCompleted();
					if(cmd.size() == result.length) {
						sendMessage("Success...");
						listener.OnSuccess();
					} else {
						sendMessage("Failure...");
						listener.OnError();
					}					
				}
			}
		}
		
		private String[] selectAbi(String device, String LibSourcePath)
		{
			String abiList32 = getSystemProp(device, "ro.product.cpu.abilist32");
			String abiList64 = getSystemProp(device, "ro.product.cpu.abilist64");
			if(!abiList32.isEmpty()) abiList32 += ",";
			if(!abiList64.isEmpty()) abiList64 += ",";
			
			String abi64 = null;
			String abi32 = null;
			for (String s : (new File(LibSourcePath)).list()) {
				if(s.matches("arm64.*")) {
					if(abiList64.matches(".*" + s + ",.*")) {
						System.out.println("device support this abi : " + s);
						if(abi64 == null) {
							abi64 = s;
						} else {
							int old_ver = Integer.parseInt(abi64.replaceAll("arm64[^0-9]*([0-9]*).*", "0$1"));
							int new_ver = Integer.parseInt(s.replaceAll("arm64[^0-9]*([0-9]*).*", "0$1"));
							if(old_ver < new_ver) {
								abi64 = s;
							} else {
								System.out.println("The version is lower than previous versions. : " + s + " < " + abi64);
							}
						}
					} else {
						System.out.println("device not support this abi : " + s);
					}
				} else if(s.matches("armeabi.*")) {
					if(abiList32.matches(".*" + s + ",.*")) {
						System.out.println("device support this abi : " + s);
						if(abi32 == null) {
							abi32 = s;
						} else {
							int old_ver = Integer.parseInt(abi32.replaceAll("armeabi[^0-9]*([0-9]*).*", "0$1"));
							int new_ver = Integer.parseInt(s.replaceAll("armeabi[^0-9]*([0-9]*).*", "0$1"));
							if(old_ver < new_ver) {
								abi32 = s;
							} else {
								System.out.println("The version is lower than previous versions. : " + s + " < " + abi32);
							}
						}
					} else {
						System.out.println("device not support this abi : " + s);
					}
				} else {
					System.out.println("Unknown abi type : " + s);
				}
				System.out.println("LibSourcePath list = " + s.replaceAll("([^-]*)", "$1"));
			}
			System.out.println("abi64 : " + abi64 + ", abi32 : " + abi32);
			return new String[] { abi32, abi64 };
		}
		
		private void sendMessage(String msg) {
			if(listener != null) listener.OnMessage(msg);
		}
	}
}
