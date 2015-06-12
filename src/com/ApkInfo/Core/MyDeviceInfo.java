package com.ApkInfo.Core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import com.ApkInfo.UIUtil.StandardButton;

public class MyDeviceInfo
{
	public String strAppinfo;
	public String strDeviceinfo;
	String adbCmd;
	
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
		
		//scanDevices();
		//newDivceInfo.strVersion = TargetInfo(TargetInfo.("versionName=")
		//adb shell getprop ro.build.version.release
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
	
	public void InstallApk(StandardButton btnInstall, String sourcePath, String DeviceADBNumber)
	{
		System.out.println("sourcePath : " + sourcePath + "DeviceADBNumber: " + DeviceADBNumber);

		startCore = new MyCoreThead(sourcePath,DeviceADBNumber,btnInstall);
		startCore.start();	
	}
	
	class MyCoreThead extends Thread
	{
		String DeviceADBNumber;
		String sourcePath;
		StandardButton btnInstall;

		MyCoreThead(String sourcePath, String DeviceADBNumber, StandardButton btnInstall)
		{
			this.DeviceADBNumber = DeviceADBNumber;
			this.sourcePath = sourcePath;
			this.btnInstall = btnInstall;
		}
		
		public void run()
		{
			String[] cmd6 = {adbCmd, "-s", this.DeviceADBNumber,"install", "-d","-r", 	this.sourcePath};
			String[] result;
			result = exc(cmd6,true);
			
			this.btnInstall.setEnabled(true);
			JOptionPane.showMessageDialog(null,
					result[2]);			
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
			if(packageName == null) return false;
			
			System.out.println("ckeckPackage() " + packageName);

			String[] cmd = {adbCmd,"-s",strADBDeviceNumber, "shell", "dumpsys","package",packageName};
			TargetInfo = exc(cmd,false);
			
			if(TargetInfo.length > 1) {
				strPakage = packageName;
				strVersion = selectString(TargetInfo,"versionName=");
				strVersionCode = selectString(TargetInfo,"versionCode=");
				strCodeFolderPath = selectString(TargetInfo,"codePath=");
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
