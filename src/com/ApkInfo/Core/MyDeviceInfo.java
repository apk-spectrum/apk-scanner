package com.ApkInfo.Core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.ApkInfo.UI.MainUI;

public class MyDeviceInfo {
	public static ArrayList<Device> DeviceList;
	
	public String strAppinfo;
	public String strDeviceinfo;
	public int DeviceCount;
	String workingDir;
	
	static MyCoreThead startCore;
	
	public MyDeviceInfo() {
		DeviceList = new ArrayList<Device>();	
		
		workingDir = CoreApkTool.GetUTF8Path() + File.separator + "adb";
		System.out.println(workingDir);
		
		Refresh();
		//newDivceInfo.strVersion = TargetInfo(TargetInfo.("versionName=")
		//adb shell getprop ro.build.version.release
		
	}
	
	public Boolean Refresh()
	{
		String strDeviceList;
		String[] cmd = {workingDir, "devices"};
		strDeviceList = exc(cmd,true);
		//adb shell dumpsys package my.package | grep versionName
		
		strDeviceList = strDeviceList.substring("List of devices attached ".length());
		
		DeviceList.clear();
		
		System.out.println(strDeviceList.indexOf("device"));
		
		for(; strDeviceList.indexOf("device") !=-1;) {
			Device temp = new Device();
			String strTemp;
			
			strTemp = strDeviceList.substring(0, strDeviceList.indexOf("device"));
			
			strTemp = strTemp.trim();
			
			temp.strADBDeviceNumber =strTemp;
			
			System.out.println(temp.strADBDeviceNumber);
			
			strDeviceList = strDeviceList.substring( strDeviceList.indexOf("device")+ "device".length());
			
			SetTarget(temp, temp.strADBDeviceNumber, MainUI.GetMyApkInfo().strPackageName);
			
			DeviceList.add(temp);
		}
		
		return true;
	}
	
	public void SetTarget(Device Devicetemp, String DeviceADBNumber, String PackageName)
	{
		String TargetInfo = new String();
		

		Devicetemp.strLabelText = "-Target Apk\n";
		
		String[] cmd1 = {workingDir,"-s",DeviceADBNumber, "shell", "dumpsys","package",PackageName};
		TargetInfo = exc(cmd1,false);
		
		System.out.println(selectString(TargetInfo,"versionName="));
		System.out.println(selectString(TargetInfo,"versionCode="));
		System.out.println(selectString(TargetInfo,"codePath="));
		System.out.println(selectString(TargetInfo,"legacyNativeLibraryDir="));		
		
		Devicetemp.strVersion = selectString(TargetInfo,"versionName=");
		Devicetemp.strVersionCode = selectString(TargetInfo,"versionCode=");
		Devicetemp.strCodeFolderPath = selectString(TargetInfo,"codePath=");
		//Devicetemp.str = selectString(TargetInfo,"legacyNativeLibraryDir=");		
		
		Devicetemp.strLabelText += "Pakage : " + PackageName +"\n";
		Devicetemp.strLabelText += "VersionName : " + Devicetemp.strVersion +"\n";
		Devicetemp.strLabelText += "VersionCode : " + Devicetemp.strVersionCode +"\n";
		Devicetemp.strLabelText += "CodePath : " + Devicetemp.strCodeFolderPath +"\n";
		
		
		
		
		
		String[] cmd2 = {workingDir, "-s",DeviceADBNumber, "shell", "getprop","ro.factory.model"}; //SGH-N045
		TargetInfo = exc(cmd2,true);
		Devicetemp.strDeviceName = TargetInfo;
		System.out.println("model : " + TargetInfo);
		
		
		String[] cmd3 = {workingDir, "-s",DeviceADBNumber, "shell", "getprop","ro.build.PDA"}; //SC04EOMEFOF1
		TargetInfo = exc(cmd3,true);
		System.out.println("model : " + TargetInfo);
		
		String[] cmd4 = {workingDir, "-s",DeviceADBNumber, "shell", "getprop","ro.build.tags"}; //test-keys
		TargetInfo = exc(cmd4,true);
		System.out.println("model : " + TargetInfo);
		
		String[] cmd5 = {workingDir, "-s",DeviceADBNumber, "shell", "getprop","ro.build.type"};//eng
		TargetInfo = exc(cmd5,true);
		System.out.println("model : " + TargetInfo);
	}
	
	public void InstallApk(String sourcePath, String DeviceADBNumber) {

		System.out.println("sourcePath : " + sourcePath + "DeviceADBNumber: " + DeviceADBNumber);
		
		startCore = new MyCoreThead(sourcePath,DeviceADBNumber);
		startCore.start();	
		
	}
	
	class MyCoreThead extends Thread {
		
		String DeviceADBNumber;
		String sourcePath;
		
		MyCoreThead(String sourcePath, String DeviceADBNumber) {
			this.DeviceADBNumber = DeviceADBNumber;
			this.sourcePath = sourcePath;
		}
		
		public void run() {
			String TargetInfo = new String();
			String[] cmd6 = {workingDir, "-s", this.DeviceADBNumber,"install", "-d","-r", 	this.sourcePath};
			TargetInfo = exc(cmd6,true);
		}
	}
	
	public String selectString(String source, String key)
	{
		String temp = null;
		
		int startindex = source.indexOf(key);
		temp = source.substring(startindex);

		int endindex = temp.indexOf(" ");
		temp = temp.substring(key.length(),endindex);
		return temp; 

	}
	
	
	
	static String exc(String[] cmd, boolean showLog) {
		try {
			String s = "";
			Process oProcess = new ProcessBuilder(cmd).redirectErrorStream(true).start();
			String buffer = "";
		    BufferedReader stdOut   = new BufferedReader(new InputStreamReader(oProcess.getInputStream()));
		    
		    while ((s = stdOut.readLine()) != null) {
		    	if(showLog) System.out.println(s);
		    	buffer += s;
		    }
		    return buffer;

		    } catch (IOException e) { // 에러 처리
		      System.err.println("에러! 외부 명령 실행에 실패했습니다.\n" + e.getMessage());		      
		      System.exit(-1);
	    }
		return null;
		
	}
	public class Device {
		//app--------------------------------------------------------------------------------------------------------
		public String strPakage;
		public String strVersion;
		public String strVersionCode;
		public String strCodeFolderPath;
				
		//device--------------------------------------------------------------------------------------------------------
		public String strADBDeviceNumber;
		public String strDeviceName;
		public String strkeys;
		public String strBuildType;
		public String strLabelText;
				
	}
}
