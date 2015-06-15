package com.ApkInfo.Core;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import com.ApkInfo.UI.MainUI;
import com.ApkInfo.UIUtil.StandardButton;

public class MyDeviceInfo
{
	public String strAppinfo;
	public String strDeviceinfo;
	public JTextArea LogTextArea;
	String adbCmd;
	
	enum INSTALL_TYPE {
		INSTALL,
		PUSH
	}

	static MyCoreThead startCore;
	
	public MyDeviceInfo()
	{
		adbCmd = CoreApkTool.GetUTF8Path() + File.separator + "adb";

		if(adbCmd.matches("^C:.*")) {
			adbCmd += ".exe";
		}
		if(!(new File(adbCmd)).exists()) {
			System.out.println("adb tool이 존재 하지 않습니다 :" + adbCmd);
			adbCmd = null;
		}
		
		System.out.println(adbCmd);
	}
	
	public ArrayList<Device> scanDevices()
	{
		ArrayList<Device> DeviceList = new ArrayList<Device>();
		String[] strDeviceList;

		DeviceList.clear();
		
		if(adbCmd == null) {
			return DeviceList;
		}

		String[] cmd = {adbCmd, "devices"};
		strDeviceList = MyConsolCmd.exc(cmd,true,null);

		for(int i=0; i<strDeviceList.length; i++) {
			if(strDeviceList[i].matches("^.*\\s*device\\s*$")){
				String name = strDeviceList[i].replaceAll("^\\s*([^\\s]*)\\s*device\\s*$", "$1");
				Device dev = new Device(name);
				dev.dump();
				DeviceList.add(dev);
			}
		}
		return DeviceList;
	}
	
	public boolean PushApk(Device device, String sourcePath, JTextArea LogTextArea)
	{
		System.out.println("sourcePath : " + sourcePath + "DeviceADBNumber: " + device.strADBDeviceNumber);
		if(adbCmd == null || adbCmd.isEmpty() 
				|| device.strADBDeviceNumber == null || device.strADBDeviceNumber.isEmpty()
				|| sourcePath == null || sourcePath.isEmpty()
				|| device.strApkPath == null || device.strApkPath.isEmpty()) {
			return false;
		}
		
		startCore = new MyCoreThead(INSTALL_TYPE.PUSH, device, sourcePath, LogTextArea);
		startCore.start();

		return true;
	}
	
	public boolean InstallApk(Device device, String sourcePath, JTextArea LogTextArea)
	{
		System.out.println("sourcePath : " + sourcePath + "DeviceADBNumber: " + device.strADBDeviceNumber);
		if(adbCmd == null || adbCmd.isEmpty() 
			|| device.strADBDeviceNumber == null || device.strADBDeviceNumber.isEmpty()
			|| sourcePath == null || sourcePath.isEmpty()) {
			return false;
		}
		
		startCore = new MyCoreThead(INSTALL_TYPE.INSTALL, device, sourcePath, LogTextArea);
		startCore.start();
		
		return true;
	}
	
	class MyCoreThead extends Thread
	{
		INSTALL_TYPE type;
		String DeviceADBNumber;
		String sourcePath;
		StandardButton btnInstall;
		Device device;
		JTextArea LogTextArea;

		MyCoreThead(INSTALL_TYPE type, Device device, String sourcePath, StandardButton btnInstall)
		{
			this.type = type;
			this.device = device;
			this.DeviceADBNumber = device.strADBDeviceNumber;
			this.sourcePath = sourcePath;
			this.btnInstall = btnInstall;
		}
		
		MyCoreThead(INSTALL_TYPE type, Device device, String sourcePath, JTextArea LogTextArea)
		{
			this.type = type;
			this.DeviceADBNumber = device.strADBDeviceNumber;
			this.sourcePath = sourcePath;
			this.LogTextArea = LogTextArea;
		}
		
		public void run()
		{
			if(type == INSTALL_TYPE.INSTALL) {
				String[] result;
				String[] cmd = {adbCmd, "-s", this.DeviceADBNumber, "install", "-d","-r", this.sourcePath};
				
				LogTextArea.append(adbCmd + "\n");
				result = MyConsolCmd.exc(cmd,true,new MyConsolCmd.OutputObserver() {
					@Override
					public boolean ConsolOutput(String output) {
						LogTextArea.append(output + "\n");
						return true;
					}
				});
				JOptionPane.showMessageDialog(null, result[3]);	
			} else {
				String[][] result;
				List<String[]> cmd = new ArrayList<String[]>();
				cmd.add(new String[] {adbCmd, "-s", this.DeviceADBNumber, "remount"});
				cmd.add(new String[] {adbCmd, "-s", this.DeviceADBNumber, "root"});
				cmd.add(new String[] {adbCmd, "-s", this.DeviceADBNumber, "shell", "su", "-c", "setenforce", "0"});
				cmd.add(new String[] {adbCmd, "-s", this.DeviceADBNumber, "push", this.sourcePath, device.strApkPath});
				System.out.println(this.sourcePath + " to " + device.strApkPath);

				String LibSourcePath = MainUI.GetMyApkInfo().strWorkAPKPath + File.separator + "lib" + File.separator;
				Iterator<String> libPaths = MainUI.GetMyApkInfo().LibPathList.iterator();
				while(libPaths.hasNext()) {
					String path = libPaths.next();
					if(path.matches(LibSourcePath.replace("\\", "\\\\")+"arm64.*")) {
						if(device.isAbi64) {
							if((new File(path)).exists()) {
								cmd.add(new String[] {adbCmd, "-s", this.DeviceADBNumber, "push", path, "/system/lib64/"});
								System.out.println("push " + path + " " + "/system/lib64/");
							} else {
								System.out.println("no such file : " + path);
							}
						} else {
							System.out.println("ignored lib64 : " + path);
						}
					} else {
						if(!device.isAbi64) {
							if((new File(path)).exists()) {
								cmd.add(new String[] {adbCmd, "-s", this.DeviceADBNumber, "push", path, "/system/lib/"});
								System.out.println("push " + path + " " + "/system/lib/");
							} else {
								System.out.println("no such file : " + path);
							}
						} else {
							System.out.println("ignored lib32 path : " + path);
						}
					}
				}
				cmd.add(new String[] {adbCmd, "-s", this.DeviceADBNumber, "reboot"});
				
				result = MyConsolCmd.exc(cmd.toArray(new String[0][0]),true,new MyConsolCmd.OutputObserver() {
					@Override
					public boolean ConsolOutput(String output) {
				    	if(output.equals("* failed to start daemon *")
				    		|| output.equals("error: device not found")
				    	) {
				    		return false;
				    	}
				    	return true;
					}
				});
				JOptionPane.showMessageDialog(null, result);	
			}
			if(this.btnInstall != null) {
				this.btnInstall.setEnabled(true);
			}
		}
	}

	public class Device
	{
		//app--------------------------------------------------------------------------------------------------------
		public String strPakage;
		public String strVersion;
		public String strVersionCode;
		public String strCodeFolderPath;
		public String strApkPath;
		public boolean isSystemApp;

		//device--------------------------------------------------------------------------------------------------------
		public String strADBDeviceNumber;
		public String strDeviceName;
		public String strModelName;
		public String strOsVersion;
		public String strBuildVersion;
		public String strSdkVersion;
		public String strkeys;
		public String strBuildType;
		public String strLabelText;
		public boolean isAbi64;

		public Device() { }
		
		public Device(String deviceName)
		{
			setDeviceInfo(deviceName);
			makeLabel();
		}
		
		public Device(String deviceName, String packageName)
		{
			setDeviceInfo(deviceName);
			ckeckPackage(packageName);
			makeLabel();
		}
		
		public void setDeviceInfo(String deviceName)
		{
			strADBDeviceNumber = deviceName;
			strDeviceName = getSystemProp(deviceName, "ro.product.device");
			strModelName = getSystemProp(deviceName, "ro.product.model");
			strOsVersion = getSystemProp(deviceName, "ro.build.version.release");
			strBuildVersion = getSystemProp(deviceName, "ro.build.version.incremental");
			strSdkVersion = getSystemProp(deviceName, "ro.build.version.sdk");
			strBuildType = getSystemProp(deviceName, "ro.build.type");
			isAbi64 = !getSystemProp(deviceName, "ro.product.cpu.abilist64").isEmpty();
		}
		
		public boolean ckeckPackage(String packageName)
		{
			String[] TargetInfo;

			strPakage = null;
			strVersion = null;
			strVersionCode = null;
			strCodeFolderPath = null;
			isSystemApp = false;
			if(packageName == null) return false;
			
			System.out.println("ckeckPackage() " + packageName);

			String[] cmd = {adbCmd,"-s",strADBDeviceNumber, "shell", "dumpsys","package",packageName};
			TargetInfo = MyConsolCmd.exc(cmd,false,null);
			
			if(TargetInfo.length > 1) {
				strPakage = packageName;
				strVersion = selectString(TargetInfo,"versionName=");
				strVersionCode = selectString(TargetInfo,"versionCode=");
				strCodeFolderPath = selectString(TargetInfo,"codePath=");
				
				if(strCodeFolderPath.matches("^/system/.*")) {
					isSystemApp = true;
				}
				
				strApkPath = null;
				if(strCodeFolderPath != null && !strCodeFolderPath.isEmpty()) {
					String[] cmd2 = {adbCmd,"-s",strADBDeviceNumber, "shell", "ls",strCodeFolderPath};
					MyConsolCmd.exc(cmd2,false,new MyConsolCmd.OutputObserver() {
						@Override
						public boolean ConsolOutput(String output) {
					    	if(output.matches("^.*apk")) {
					    		strApkPath = strCodeFolderPath + "/" + output;
					    	}
					    	return true;
						}
					});
				}
				
				return true;
			}
			return false;
		}
		
		public void makeLabel()
		{
			//Devicetemp.strLabelText = "-Device Info\n";
			strLabelText = "Model : " + strModelName + " / " + strDeviceName + "\n";
			strLabelText += "Version : " + strBuildVersion + "(" + strBuildType + ") / ";
			strLabelText += "" + strOsVersion + "(" + strSdkVersion + ")\n";
			if(strPakage != null) {
				strLabelText += "\n";
				strLabelText += "-Installed APK info\n";
				strLabelText += "Pakage : " + strPakage +"\n";
				strLabelText += "Version : " + strVersion + " / " + strVersionCode +"\n";
				strLabelText += "CodePath : " + strCodeFolderPath +"\n";
			}
		}
		
		public void dump()
		{
			System.out.println("VersionName : " + strVersion);
			System.out.println("VersionCode : " + strVersionCode);
			System.out.println("CodePath : " + strCodeFolderPath);
			System.out.println("Device : " + strDeviceName);
			System.out.println("Model : " + strModelName);
			System.out.println("BuildType : " + strBuildType);
		}
		
		private String selectString(String[] source, String key)
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
		
		private String getSystemProp(String device, String tag)
		{
			String[] TargetInfo;
			String[] cmd = {adbCmd, "-s", device, "shell", "getprop", tag}; //SGH-N045

			TargetInfo = MyConsolCmd.exc(cmd,false,null);
			return TargetInfo[0];
		}
	}
}
