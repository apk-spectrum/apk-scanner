package com.ApkInfo.Core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import com.ApkInfo.UIUtil.StandardButton;

public class MyDeviceInfo
{
	public String strAppinfo;
	public String strDeviceinfo;
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
		strDeviceList = exc(cmd,true);

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
	
	public void PushApk(Device device, String sourcePath, StandardButton btnInstall)
	{
		System.out.println("sourcePath : " + sourcePath + "DeviceADBNumber: " + device.strADBDeviceNumber);

		startCore = new MyCoreThead(INSTALL_TYPE.PUSH, device, sourcePath, btnInstall);
		startCore.start();	
	}
	
	public void InstallApk(Device device, String sourcePath, StandardButton btnInstall)
	{
		System.out.println("sourcePath : " + sourcePath + "DeviceADBNumber: " + device.strADBDeviceNumber);

		startCore = new MyCoreThead(INSTALL_TYPE.INSTALL, device, sourcePath, btnInstall);
		startCore.start();
	}
	
	class MyCoreThead extends Thread
	{
		INSTALL_TYPE type;
		String DeviceADBNumber;
		String sourcePath;
		StandardButton btnInstall;

		MyCoreThead(INSTALL_TYPE type, Device device, String sourcePath, StandardButton btnInstall)
		{
			this.type = type;
			this.DeviceADBNumber = device.strADBDeviceNumber;
			this.sourcePath = sourcePath;
			this.btnInstall = btnInstall;
		}
		
		public void run()
		{
			String[] result;
			if(type == INSTALL_TYPE.INSTALL) {
				String[] cmd = {adbCmd, "-s", this.DeviceADBNumber, "install", "-d","-r", this.sourcePath};
				result = exc(cmd,true);
			} else {
				String[] cmd1 = {adbCmd, "-s", this.DeviceADBNumber, "remount"};
				result = exc(cmd1,true);
				String[] cmd2 = {adbCmd, "-s", this.DeviceADBNumber, "root"};
				result = exc(cmd2,true);
				String[] cmd3 = {adbCmd, "-s", this.DeviceADBNumber, "shell", "su", "-c", "setenforce", "0"};
				result = exc(cmd3,true);
				String[] cmd4 = {adbCmd, "-s", this.DeviceADBNumber, "push", this.sourcePath, ""};
				result = exc(cmd4,true);
				String[] cmd5 = {adbCmd, "-s", this.DeviceADBNumber, "reboot"};
				result = exc(cmd5,true);
			}
			if(this.btnInstall != null) {
				this.btnInstall.setEnabled(true);
			}
			JOptionPane.showMessageDialog(null, result[2]);			
		}
	}
	
	static String[] exc(String[] cmd, boolean showLog)
	{
		String s = "";
		List<String> buffer = new ArrayList<String>(); 

		try {
			Process oProcess = new ProcessBuilder(cmd).redirectErrorStream(true).start();
		    BufferedReader stdOut   = new BufferedReader(new InputStreamReader(oProcess.getInputStream()));
		    
		    while ((s = stdOut.readLine()) != null) {
		    	if(showLog) System.out.println(s);
		    	buffer.add(s);
		    }
		} catch (IOException e) { // 에러 처리
		      System.err.println("에러! 외부 명령 실행에 실패했습니다.\n" + e.getMessage());		      
		      System.exit(-1);
	    }

		return buffer.toArray(new String[0]);
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
			TargetInfo = exc(cmd,false);
			
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

			TargetInfo = exc(cmd,true);
			return TargetInfo[0];
		}
	}
}
