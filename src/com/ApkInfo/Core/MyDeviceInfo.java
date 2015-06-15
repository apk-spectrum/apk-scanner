package com.ApkInfo.Core;

import java.io.File;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import com.ApkInfo.UI.MainUI;
import com.ApkInfo.UIUtil.StandardButton;

public class MyDeviceInfo implements MyConsolCmd.OutputObserver
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
			adbCmd = "";
		}
		
		System.out.println(adbCmd);
	}
	
	public ArrayList<Device> scanDevices()
	{
		ArrayList<Device> DeviceList = new ArrayList<Device>();
		String[] strDeviceList;

		DeviceList.clear();

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
	
	public void PushApk(Device device, String sourcePath, JTextArea LogTextArea)
	{
		System.out.println("sourcePath : " + sourcePath + "DeviceADBNumber: " + device.strADBDeviceNumber);

		startCore = new MyCoreThead(INSTALL_TYPE.PUSH, device, sourcePath, LogTextArea);
		startCore.start();	
	}
	
	public void InstallApk(Device device, String sourcePath, JTextArea LogTextArea)
	{
		System.out.println("sourcePath : " + sourcePath + "DeviceADBNumber: " + device.strADBDeviceNumber);

		startCore = new MyCoreThead(INSTALL_TYPE.INSTALL, device, sourcePath, LogTextArea);
		startCore.start();
	}

	@Override
	public void ConsolOutput(String output) {

	}
	
	class MyCoreThead extends Thread implements MyConsolCmd.OutputObserver
	{
		INSTALL_TYPE type;
		String DeviceADBNumber;
		String sourcePath;
		StandardButton btnInstall;
		JTextArea LogTextArea;
		
		MyCoreThead(INSTALL_TYPE type, Device device, String sourcePath, StandardButton btnInstall)
		{
			this.type = type;
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
			String[] result;
			if(type == INSTALL_TYPE.INSTALL) {
				String[] cmd = {adbCmd, "-s", this.DeviceADBNumber, "install", "-d","-r", this.sourcePath};
				result = MyConsolCmd.exc(cmd, true, new MyConsolCmd.OutputObserver() {
					@Override
					public void ConsolOutput(String output) {
						LogTextArea.append(output + "\n");				    	
					}
				});
				JOptionPane.showMessageDialog(null, result[2]);
				
			} else {
				String[] cmd1 = {adbCmd, "-s", this.DeviceADBNumber, "remount"};
				result = MyConsolCmd.exc(cmd1,true,null);
				String[] cmd2 = {adbCmd, "-s", this.DeviceADBNumber, "root"};
				result = MyConsolCmd.exc(cmd2,true,null);
				String[] cmd3 = {adbCmd, "-s", this.DeviceADBNumber, "shell", "su", "-c", "setenforce", "0"};
				result = MyConsolCmd.exc(cmd3,true,null);
				String[] cmd4 = {adbCmd, "-s", this.DeviceADBNumber, "push", this.sourcePath, ""};
				result = MyConsolCmd.exc(cmd4,true,null);
				String[] cmd5 = {adbCmd, "-s", this.DeviceADBNumber, "reboot"};
				result = MyConsolCmd.exc(cmd5,true,null);
				
				JOptionPane.showMessageDialog(null, result[2]);
			}
			if(this.btnInstall != null) {
				this.btnInstall.setEnabled(true);
			}
			
			if(this.LogTextArea != null) {
				
			}
			
		}

		@Override
		public void ConsolOutput(String output) {

		}
	}

	public class Device
	{
		//app--------------------------------------------------------------------------------------------------------
		public String strPakage;
		public String strVersion;
		public String strVersionCode;
		public String strCodeFolderPath;
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
