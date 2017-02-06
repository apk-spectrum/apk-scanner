package com.apkscanner.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.apkscanner.resource.Resource;
import com.apkscanner.util.ConsolCmd;
import com.apkscanner.util.FileUtil;
import com.apkscanner.util.Log;

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
		public final String label;

		public DeviceStatus(String name, String status, String usb, String product, String model, String device)
		{
			this.name = name.trim();
			this.status = status.trim();
			this.usb = usb.trim();
			this.product = product.trim();
			this.model = model.trim();
			this.device = device.trim();
			
			String label = this.name;
			if(this.model != null && !this.model.isEmpty()) {
				label += "(" + this.model + ")";
			} else if(this.model != null && !this.model.isEmpty()) {
				label += "(" + this.product + ")";
			} else {
				label += "(Unknown)";
			}
			this.label = label;
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

		@Override
		public String toString() {
		    return this.label;
		}
	}

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
				Log.e("no such adb tool" + adbCmd);
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
			String[] result = ConsolCmd.exc(new String[] {adb, "version"});
			return result[0];
		}
		return adbVersion;
	}

	static public boolean ckeckAdbTool()
	{
		//Log.i("ckeckAdbTool()");
		if(adbCmd == null) return false;

		ConsolCmd.exc(new String[] {adbCmd, "kill-server"}, false, null);
		String[] result = ConsolCmd.exc(new String[] {adbCmd, "start-server"}, false, null);
		return result[1].matches(".*daemon started successfully.*");
	}

	static public ArrayList<DeviceStatus> scanDevices()
	{
		String[] cmdResult;
		ArrayList<DeviceStatus> deviceList = new ArrayList<DeviceStatus>();

		if(adbCmd == null) return null;

		String[] cmd = {adbCmd, "devices", "-l"};
		cmdResult = ConsolCmd.exc(cmd,true,null);
		
		boolean startList = false;
		for(String output: cmdResult) {
			if(!startList || output.matches("^\\s*$")) {
				if(output.startsWith("List"))
					startList = true;
				continue;
			}
			output = output.replaceAll("^\\s*([^\\s]*)\\s*([^\\s]*)(\\s*(usb:([^\\s]*)))?(\\s*product:([^\\s]*)\\s*model:([^\\s]*)\\s*device:([^\\s]*))?\\s*.*$", "$1 |$2 |$5 |$7 |$8 |$9 ");
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

		TargetInfo = ConsolCmd.exc(cmd,false,null);
		
		return TargetInfo[0];
	}
	
	static public boolean hasRootPermission(String device)
	{
		if(adbCmd == null) return false;
		boolean hasRoot = true;
		String[] cmd = {adbCmd, "-s", device, "root"};
		String[] result = ConsolCmd.exc(cmd, false, null);
		if(!result[0].endsWith("running as root")) {
			hasRoot = false;
		}
		return hasRoot;
	}
	
	static public void reboot(String device)
	{
		if(adbCmd == null) return;
		ConsolCmd.exc(new String[] {adbCmd, "-s", device, "reboot"});
	}
	
	static public String getApkPath(String device, String dir)
	{
		String apkPath = dir;
		if(!apkPath.endsWith(".apk")) {
			Log.i("No apk file path : " + apkPath);
			String[] cmd = {AdbWrapper.getAdbCmd(), "-s", device, "shell", "ls", apkPath + "/*.apk"};
			String[] result = ConsolCmd.exc(cmd, true);
			if(result.length == 0 || !result[0].endsWith(".apk")) {
				Log.e("No such apk file : " + apkPath);
				return null;
			}
			apkPath = result[0];
			Log.i("Cahnge target apk path to " + apkPath);
		}
		return apkPath;
	}
	
	static public void uninstallApk(String device, String packageName)
	{
		if(adbCmd == null) return;
		ConsolCmd.exc(new String[] {adbCmd, "-s", device, "uninstall", packageName});
	}
	
	static public void removeApk(String device, String apkPath)
	{
		if(adbCmd == null) return;

		ConsolCmd.exc(new String[] {adbCmd, "-s", device, "root"}, true);
		ConsolCmd.exc(new String[] {adbCmd, "-s", device, "remount"}, true);
		ConsolCmd.exc(new String[] {adbCmd, "-s", device, "shell", "rm", "-rf", apkPath}, true);
	}
	
	static public void PushApk(String name, String srcApkPath, String destApkPath, String libPath, AdbWrapperListener listener)
	{
		//Log.i("PushApk() device : " + name + ", apkPath: " + srcApkPath);
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
		//Log.i("InstallApk() device : " + name + ", apkPath: " + apkPath);
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
		//Log.i("PullApk() device : " + name + ", apkPath: " + srcApkPath);
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
	
	static public boolean PullApk_sync(String name, String srcApkPath, String destApkPath)
	{
		return PullApk_sync(name, srcApkPath, destApkPath, null);
	}
	
	static public boolean PullApk_sync(String name, String srcApkPath, String destApkPath, ConsolCmd.OutputObserver observer)
	{
		//Log.i("PullApk() device : " + name + ", apkPath: " + srcApkPath);
		if(adbCmd == null || name == null || destApkPath == null || srcApkPath == null || srcApkPath.isEmpty()) {
			return false;
		}

		srcApkPath = getApkPath(name, srcApkPath);
		if(srcApkPath == null) {
			return false;
		}

		String[] result;
		String[] cmd = {adbCmd, "-s", name, "pull", srcApkPath, destApkPath};
		result = ConsolCmd.exc(cmd, true, observer);
		
		if(result.length > 0 && result[0].endsWith("s)")) {
			return true;
		} else if(srcApkPath.startsWith("/data/app/") && result[0].trim().endsWith("open failed: Permission denied")){
			Log.w("adb pull permission denied : " + srcApkPath);
			String tmpPath = "/sdcard/tmp";
			
			String[] mk = {adbCmd, "-s", name, "shell", "mkdir", "-p", tmpPath + srcApkPath.substring(0, srcApkPath.lastIndexOf("/"))};
			result = ConsolCmd.exc(mk, true, observer);
			if(result.length > 0) return false;
			

			String[] cp = {adbCmd, "-s", name, "shell", "cp", srcApkPath, tmpPath + srcApkPath};
			result = ConsolCmd.exc(cp, true, observer);
			if(result.length > 0) return false;
			
			cmd[4] = tmpPath + srcApkPath;
			result = ConsolCmd.exc(cmd, true, observer);
			
			if(result.length > 0 && result[0].endsWith("s)")) {
				return true;
			}
		}

		return false;
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
				&& (isSystemApp || (!isSystemApp && hasRootPermission(device)))) {
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
				
				result = ConsolCmd.exc(cmd,true,new ConsolCmd.OutputObserver() {
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
				boolean successed = PullApk_sync(this.device, this.srcApkPath, this.destApkPath, new ConsolCmd.OutputObserver() {
					@Override
					public boolean ConsolOutput(String output) {
						sendMessage(output.replaceAll("^.*adb(\\.exe)?", "adb"));
						return true;
					}
				});

				if(listener != null) {
					listener.OnCompleted();
					if(successed) {
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
				//Log.i(this.srcApkPath + " to " + this.destApkPath);
				
				//Log.i("libpath " + libPath);
				if(libPath != null && (new File(libPath)).exists()) {
					String[] selAbi = selectAbi(this.device, libPath);
					String abi32 = selAbi[0];
					String abi64 = selAbi[1];
					
					Iterator<String> libPaths = FileUtil.findFiles(new File(libPath), ".so", null).iterator();
					while(libPaths.hasNext()) {
						String path = libPaths.next();
						if(!(new File(path)).exists()) {
							Log.w("no such file : " + path);
							continue;
						}
						String abi = path.replaceAll(libPath.replace("\\", "\\\\")+"([^\\\\/]*).*","$1");
						//Log.i("abi = " + abi);
						if(abi.equals(abi32)) {
							cmd.add(new String[] {adbCmd, "-s", this.device, "push", path, "/system/lib/"});
							//Log.i("push " + path + " " + "/system/lib/");
						} else if (abi.equals(abi64)) {
							cmd.add(new String[] {adbCmd, "-s", this.device, "push", path, "/system/lib64/"});
							//Log.i("push " + path + " " + "/system/lib64/");						
						} else {
							//Log.w("ignored path : " + path);
						}
					}
				}
				cmd.add(new String[] {adbCmd, "-s", this.device, "shell", "echo", "Compleated..."});
				
				result = ConsolCmd.exc(cmd.toArray(new String[0][0]),true,new ConsolCmd.OutputObserver() {
					@Override
					public boolean ConsolOutput(String output) {
						sendMessage(output.replaceAll("^.*adb(\\.exe)?", "adb"));
				    	if(output.equals("* failed to start daemon *")
				    		|| output.equals("error: device not found")
				    		|| output.equals("adbd cannot run as root in production builds")
				    		|| output.matches(".*Permission denied.*")
				    	) {
				    		//Log.e(">>>>>>>>>>>> fail : " + output);
				    		return false;
				    	}
				    	return true;
					}
				});

				//Log.i("cmd.size() " + cmd.size() + ", result.length " + result.length);
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
				if(s.startsWith("arm64")) {
					if(abiList64.matches(".*" + s + ",.*")) {
						//Log.i("device support this abi : " + s);
						if(abi64 == null) {
							abi64 = s;
						} else {
							int old_ver = Integer.parseInt(abi64.replaceAll("arm64[^0-9]*([0-9]*).*", "0$1"));
							int new_ver = Integer.parseInt(s.replaceAll("arm64[^0-9]*([0-9]*).*", "0$1"));
							if(old_ver < new_ver) {
								abi64 = s;
							} else {
								//Log.w("The version is lower than previous versions. : " + s + " < " + abi64);
							}
						}
					} else {
						//Log.w("device not support this abi : " + s);
					}
				} else if(s.startsWith("armeabi")) {
					if(abiList32.matches(".*" + s + ",.*")) {
						//Log.i("device support this abi : " + s);
						if(abi32 == null) {
							abi32 = s;
						} else {
							int old_ver = Integer.parseInt(abi32.replaceAll("armeabi[^0-9]*([0-9]*).*", "0$1"));
							int new_ver = Integer.parseInt(s.replaceAll("armeabi[^0-9]*([0-9]*).*", "0$1"));
							if(old_ver < new_ver) {
								abi32 = s;
							} else {
								//Log.w("The version is lower than previous versions. : " + s + " < " + abi32);
							}
						}
					} else {
						//Log.w("device not support this abi : " + s);
					}
				} else {
					//Log.w("Unknown abi type : " + s);
				}
				//Log.i("LibSourcePath list = " + s.replaceAll("([^-]*)", "$1"));
			}
			//Log.i("abi64 : " + abi64 + ", abi32 : " + abi32);
			return new String[] { abi32, abi64 };
		}
		
		private void sendMessage(String msg) {
			if(listener != null) listener.OnMessage(msg);
		}
	}
}
