package com.ApkInfo.Core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

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

		if(workingDir.matches("^C:.*")) {
			workingDir += ".exe";
		}
		if(!(new File(workingDir)).exists()) {
			System.out.println("adb tool이 존재 하지 않습니다 :" + workingDir);
			workingDir = "";
		}
		
		System.out.println(workingDir);
		
		Refresh();
		//newDivceInfo.strVersion = TargetInfo(TargetInfo.("versionName=")
		//adb shell getprop ro.build.version.release
		
	}
	
	public Boolean Refresh()
	{
		String[] strDeviceList;
		String[] cmd = {workingDir, "devices"};
		strDeviceList = exc(cmd,true);
		//adb shell dumpsys package my.package | grep versionName

		DeviceList.clear();

		for(int i=0; i<strDeviceList.length; i++) {
			if(strDeviceList[i].matches("^.*\\s*device\\s*$")){
				Device temp = new Device();
				temp.strADBDeviceNumber = strDeviceList[i].replaceAll("^\\s*([^\\s]*)\\s*device\\s*$", "$1");
				System.out.println("device number : '" + temp.strADBDeviceNumber + "'");

				SetTarget(temp, temp.strADBDeviceNumber, MainUI.GetMyApkInfo().strPackageName);
				
				DeviceList.add(temp);
			}
		}
		
		return true;
	}
	
	public void SetTarget(Device Devicetemp, String DeviceADBNumber, String PackageName)
	{
		String[] TargetInfo;
		

		Devicetemp.strLabelText = "-Target Apk\n";
		
		String[] cmd1 = {workingDir,"-s",DeviceADBNumber, "shell", "dumpsys","package",PackageName};
		TargetInfo = exc(cmd1,false);
		
		Devicetemp.strVersion = selectString(TargetInfo,"versionName=");
		Devicetemp.strVersionCode = selectString(TargetInfo,"versionCode=");
		Devicetemp.strCodeFolderPath = selectString(TargetInfo,"codePath=");
		//Devicetemp.str = selectString(TargetInfo,"legacyNativeLibraryDir=");		

		System.out.println(Devicetemp.strVersion);
		System.out.println(Devicetemp.strVersionCode);
		System.out.println(Devicetemp.strCodeFolderPath);
		//System.out.println(selectString(TargetInfo,"legacyNativeLibraryDir="));		

		Devicetemp.strLabelText += "Pakage : " + PackageName +"\n";
		Devicetemp.strLabelText += "VersionName : " + Devicetemp.strVersion +"\n";
		Devicetemp.strLabelText += "VersionCode : " + Devicetemp.strVersionCode +"\n";
		Devicetemp.strLabelText += "CodePath : " + Devicetemp.strCodeFolderPath +"\n";
		
		
		String[] cmd2 = {workingDir, "-s",DeviceADBNumber, "shell", "getprop","ro.factory.model"}; //SGH-N045
		TargetInfo = exc(cmd2,true);
		Devicetemp.strDeviceName = TargetInfo[0];
		System.out.println("model : " + TargetInfo[0]);
		
		
		String[] cmd3 = {workingDir, "-s",DeviceADBNumber, "shell", "getprop","ro.build.PDA"}; //SC04EOMEFOF1
		TargetInfo = exc(cmd3,true);
		System.out.println("model : " + TargetInfo[0]);
		
		String[] cmd4 = {workingDir, "-s",DeviceADBNumber, "shell", "getprop","ro.build.tags"}; //test-keys
		TargetInfo = exc(cmd4,true);
		System.out.println("model : " + TargetInfo[0]);
		
		String[] cmd5 = {workingDir, "-s",DeviceADBNumber, "shell", "getprop","ro.build.type"};//eng
		TargetInfo = exc(cmd5,true);
		System.out.println("model : " + TargetInfo[0]);
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
			String[] cmd6 = {workingDir, "-s", this.DeviceADBNumber,"install", "-d","-r", 	this.sourcePath};
			exc(cmd6,true);
		}
	}
	
	public String selectString(String[] source, String key)
	{
		String temp = null;
		
		for(int i=0; i < source.length; i++) {
			if(source[i].matches("^\\s*"+key+".*$")) {
				temp = source[i].replaceAll("^\\s*"+key+"\\s*(.*)\\s*$", "$1");
				break;
			}
		}
		return temp;
	}
	
	
	static String[] exc(String[] cmd, boolean showLog) {
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
